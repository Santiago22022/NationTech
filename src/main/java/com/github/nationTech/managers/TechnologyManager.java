package com.github.nationTech.managers;

import com.github.nationTech.NationTech;
import com.github.nationTech.model.Technology;
import com.github.nationTech.model.TechnologyType;
import com.github.nationTech.requirements.Requirement;
import com.github.nationTech.requirements.RequirementParser;
import com.github.nationTech.utils.MessageManager;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Gestiona múltiples árboles tecnológicos (el oficial y los borradores).
 * Actúa como la caché central para todas las tecnologías.
 */
public class TechnologyManager {

    private final NationTech plugin;
    public static final String OFFICIAL_TREE_ID = "oficial";

    // Un mapa de árboles. La clave es el tree_id, el valor es el mapa del árbol tecnológico.
    private final Map<String, Map<String, Technology>> techTrees = new ConcurrentHashMap<>();

    public TechnologyManager(NationTech plugin) {
        this.plugin = plugin;
    }

    /**
     * Carga todos los árboles y sus tecnologías desde la base de datos a la caché.
     */
    public void loadAllTrees() {
        techTrees.clear();
        // Agrupamos las tecnologías por su tree_id al cargarlas.
        Map<String, List<Technology>> technologiesByTree = plugin.getDatabaseManager().loadAllTechnologies()
                .stream()
                .collect(Collectors.groupingBy(Technology::getTreeId));

        technologiesByTree.forEach((treeId, techList) -> {
            Map<String, Technology> treeMap = new ConcurrentHashMap<>();
            for (Technology tech : techList) {
                treeMap.put(tech.getId(), tech);
            }
            techTrees.put(treeId, treeMap);
        });

        // Nos aseguramos de que el árbol oficial siempre exista en la caché, aunque esté vacío.
        techTrees.computeIfAbsent(OFFICIAL_TREE_ID, k -> new ConcurrentHashMap<>());
        plugin.getLogger().info(techTrees.size() + " árbol(es) tecnológico(s) cargado(s) en la caché.");
    }

    /**
     * Crea un nuevo borrador (básicamente, inicializa un mapa vacío para él).
     * @param draftName El nombre del borrador a crear.
     * @return true si el borrador se creó, false si ya existía.
     */
    public boolean createDraft(String draftName) {
        if (techTrees.containsKey(draftName)) {
            return false;
        }
        techTrees.put(draftName, new ConcurrentHashMap<>());
        return true;
    }

    /**
     * Devuelve los nombres de todos los árboles cargados (oficial y borradores).
     * @return Un Set con los nombres de los árboles.
     */
    public Set<String> getTreeNames() {
        return techTrees.keySet();
    }

    /**
     * Obtiene el mapa de un árbol tecnológico específico.
     * @param treeId El ID del árbol (ej: "oficial", "test").
     * @return Un mapa no modificable del árbol, o un mapa vacío si no se encuentra.
     */
    public Map<String, Technology> getTechnologyTree(String treeId) {
        return Collections.unmodifiableMap(techTrees.getOrDefault(treeId, Collections.emptyMap()));
    }

    /**
     * Crea una nueva tecnología en un árbol específico.
     *
     * @param treeId El ID del árbol donde se creará la tecnología.
     * @return true si se creó con éxito, false si ya existía.
     */
    public boolean createTechnology(String treeId, String id, int row, int column, String nombre, String padreId, TechnologyType tipo, String requisitos, String icono, String recompensa) {
        Map<String, Technology> tree = techTrees.computeIfAbsent(treeId, k -> new ConcurrentHashMap<>());
        if (tree.containsKey(id)) {
            return false; // Evita sobreescribir
        }

        Technology newTech = new Technology(treeId, id, row, column, nombre, padreId, tipo, requisitos, icono, recompensa);
        plugin.getDatabaseManager().saveTechnology(newTech);
        tree.put(id, newTech);
        return true;
    }

    /**
     * Elimina una tecnología de un árbol específico, tanto de la caché como de la base de datos.
     *
     * @param treeId El ID del árbol del que se eliminará la tecnología.
     * @param techId El ID de la tecnología a eliminar.
     * @return true si se eliminó, false si no se encontró.
     */
    public boolean deleteTechnology(String treeId, String techId) {
        Map<String, Technology> tree = techTrees.get(treeId);
        if (tree == null || !tree.containsKey(techId)) {
            plugin.getLogger().warning("Se intentó eliminar una tecnología que no existe: " + techId + " en el árbol " + treeId);
            return false;
        }

        plugin.getDatabaseManager().deleteTechnology(treeId, techId);
        tree.remove(techId);
        plugin.getLogger().info("Tecnología " + techId + " eliminada del árbol " + treeId + " con éxito.");
        return true;
    }


    /**
     * Intenta desbloquear una tecnología para una nación en el árbol OFICIAL.
     * @param player El jugador que inicia el desbloqueo.
     * @param nation La nación que desbloqueará la tecnología.
     * @param tech La tecnología a desbloquear.
     */
    public void attemptUnlockTechnology(Player player, Nation nation, Technology tech) {
        if (!tech.getTreeId().equals(OFFICIAL_TREE_ID)) {
            MessageManager.sendMessage(player, "<red>Solo se pueden desbloquear tecnologías del árbol oficial.");
            return;
        }

        Resident resident = TownyAPI.getInstance().getResident(player.getUniqueId());
        if (resident == null || !nation.isKing(resident)) {
            MessageManager.sendMessage(player, "<red>Solo el líder de la nación puede desbloquear tecnologías.");
            return;
        }

        Set<String> unlockedTechs = plugin.getDatabaseManager().getNationUnlockedTechs(nation.getUUID());
        if (unlockedTechs.contains(tech.getId())) {
            MessageManager.sendMessage(player, "<yellow>Tu nación ya ha desbloqueado esta tecnología.");
            return;
        }

        if (tech.getPadreId() != null && !unlockedTechs.contains(tech.getPadreId())) {
            MessageManager.sendMessage(player, "<red>Debes desbloquear la tecnología anterior primero.");
            return;
        }

        List<Requirement> requirements = RequirementParser.parse(tech.getRequisitos());
        for (Requirement req : requirements) {
            if (!req.check(player)) {
                MessageManager.sendMessage(player, "<red>No cumples con todos los requisitos. Necesitas: " + req.getLoreText());
                return;
            }
        }

        for (Requirement req : requirements) {
            req.consume(player);
        }

        plugin.getDatabaseManager().addUnlockedTechnology(nation.getUUID(), tech.getId());

        String command = tech.getRecompensa()
                .replace("%player%", player.getName())
                .replace("%nation%", nation.getName());
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

        MessageManager.sendMessage(player, "<green>¡Tu nación ha desbloqueado la tecnología <white>"+tech.getNombre()+"</white>!");

        plugin.getGuiManager().openNationTechGUI(player, nation, OFFICIAL_TREE_ID);
    }
}