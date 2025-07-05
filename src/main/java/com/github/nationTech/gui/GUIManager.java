package com.github.nationTech.gui;

import com.github.nationTech.NationTech;
import com.github.nationTech.managers.TechnologyManager;
import com.github.nationTech.model.Technology;
import com.github.nationTech.requirements.Requirement;
import com.github.nationTech.requirements.RequirementParser;
import com.palmergames.bukkit.towny.object.Nation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GUIManager {

    private final NationTech plugin;
    public static final NamespacedKey TECH_ID_KEY = new NamespacedKey(NationTech.getInstance(), "technology_id");
    public static final NamespacedKey TREE_ID_KEY = new NamespacedKey(NationTech.getInstance(), "tree_id");


    public GUIManager(NationTech plugin) {
        this.plugin = plugin;
    }

    // ... (openDraftSelectionGUI y openNationTechGUI no cambian) ...
    public void openDraftSelectionGUI(Player player) {
        Component title = MiniMessage.miniMessage().deserialize("<dark_blue>Seleccionar Árbol Tecnológico");
        Set<String> treeNames = plugin.getTechnologyManager().getTreeNames();
        int size = Math.max(9, (int) (Math.ceil(treeNames.size() / 9.0) * 9));
        Inventory gui = Bukkit.createInventory(null, size, title);

        for (String treeId : treeNames) {
            boolean isOfficial = treeId.equals(TechnologyManager.OFFICIAL_TREE_ID);
            Material iconMaterial = isOfficial ? Material.ENCHANTED_BOOK : Material.WRITABLE_BOOK;
            String displayName = isOfficial ? "<gold><b>Árbol Oficial" : "<yellow>Borrador: " + treeId;
            ItemStack icon = new ItemStack(iconMaterial);
            ItemMeta meta = icon.getItemMeta();
            meta.displayName(MiniMessage.miniMessage().deserialize(displayName));
            List<Component> lore = new ArrayList<>();
            lore.add(MiniMessage.miniMessage().deserialize("<gray>Click para visualizar y editar."));
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.getPersistentDataContainer().set(TREE_ID_KEY, PersistentDataType.STRING, treeId);
            icon.setItemMeta(meta);
            gui.addItem(icon);
        }
        player.openInventory(gui);
    }
    public void openNationTechGUI(@NotNull Player player, @NotNull Nation nation, @NotNull String treeId) {
        Component title = MiniMessage.miniMessage().deserialize("<dark_gray>Tecnologías de </dark_gray><gold>" + nation.getName());
        Inventory gui = Bukkit.createInventory(null, 54, title);
        TechnologyManager techManager = plugin.getTechnologyManager();
        Map<String, Technology> techTree = techManager.getTechnologyTree(treeId);
        Set<String> unlockedTechs = plugin.getDatabaseManager().getNationUnlockedTechs(nation.getUUID());
        ItemStack background = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ", 0);
        for(int i = 0; i < gui.getSize(); i++) { gui.setItem(i, background); }
        for (Technology child : techTree.values()) {
            if (child.getPadreId() != null) {
                Technology parent = techTree.get(child.getPadreId());
                if (parent != null) {
                    drawConnection(gui, child, parent, unlockedTechs.contains(child.getId()));
                }
            }
        }
        for (Technology tech : techTree.values()) {
            gui.setItem(tech.getSlot(), createTechIcon(tech, techTree, unlockedTechs));
        }
        player.openInventory(gui);
    }


    /**
     * --- MÉTODO NUEVO ---
     * Abre una vista de solo lectura de un árbol para administradores.
     * No muestra progreso, solo la estructura para facilitar el diseño.
     * @param player El administrador que verá el menú.
     * @param treeId El ID del árbol (oficial o borrador) a visualizar.
     */
    public void openAdminTechGUI(@NotNull Player player, @NotNull String treeId) {
        Component title = MiniMessage.miniMessage().deserialize("<dark_aqua>Vista de Admin: </dark_aqua><yellow>" + treeId);
        Inventory gui = Bukkit.createInventory(null, 54, title);

        TechnologyManager techManager = plugin.getTechnologyManager();
        Map<String, Technology> techTree = techManager.getTechnologyTree(treeId);

        // 1. Dibujar el fondo
        ItemStack background = createGuiItem(Material.BLUE_STAINED_GLASS_PANE, " ", 0);
        for(int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, background);
        }

        // 2. Dibujar las líneas de conexión (siempre en estado "desbloqueado" para verlas bien)
        for (Technology child : techTree.values()) {
            if (child.getPadreId() != null) {
                Technology parent = techTree.get(child.getPadreId());
                if (parent != null) {
                    drawConnection(gui, child, parent, true); // true para que se vean las líneas en color
                }
            }
        }

        // 3. Dibujar los iconos de tecnología en modo admin
        for (Technology tech : techTree.values()) {
            gui.setItem(tech.getSlot(), createAdminTechIcon(tech));
        }

        player.openInventory(gui);
    }

    /**
     * --- MÉTODO NUEVO ---
     * Crea un ícono de tecnología para la vista de administrador, mostrando información de depuración.
     */
    private ItemStack createAdminTechIcon(Technology tech) {
        Material iconMaterial = Material.matchMaterial(tech.getIcono());
        if (iconMaterial == null) iconMaterial = Material.BARRIER;

        ItemStack icon = new ItemStack(iconMaterial);
        ItemMeta meta = icon.getItemMeta();

        meta.displayName(Component.text(tech.getNombre(), NamedTextColor.AQUA, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        lore.add(Component.text("ID: ", NamedTextColor.GRAY).append(Component.text(tech.getId(), NamedTextColor.WHITE)));
        lore.add(Component.text("Árbol: ", NamedTextColor.GRAY).append(Component.text(tech.getTreeId(), NamedTextColor.WHITE)));
        lore.add(Component.text("Padre: ", NamedTextColor.GRAY).append(Component.text(tech.getPadreId() == null ? "Ninguno" : tech.getPadreId(), NamedTextColor.WHITE)));
        lore.add(Component.text("Posición: ", NamedTextColor.GRAY).append(Component.text(tech.getRow() + ", " + tech.getColumn(), NamedTextColor.WHITE)));
        lore.add(Component.text("Tipo: ", NamedTextColor.GRAY).append(Component.text(tech.getTipo().name(), NamedTextColor.WHITE)));

        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        // No añadimos el TECH_ID_KEY porque esta vista es de solo lectura.

        icon.setItemMeta(meta);
        return icon;
    }

    // ... (createTechIcon, drawConnection, setLineItem, createGuiItem no cambian) ...
    private ItemStack createTechIcon(Technology tech, Map<String, Technology> techTree, Set<String> unlockedTechs) {
        Material iconMaterial = Material.matchMaterial(tech.getIcono());
        if (iconMaterial == null) iconMaterial = Material.BARRIER;
        ItemStack icon = new ItemStack(iconMaterial);
        ItemMeta meta = icon.getItemMeta();
        List<Component> lore = new ArrayList<>();
        boolean isUnlocked = unlockedTechs.contains(tech.getId());
        boolean parentUnlocked = tech.getPadreId() == null || unlockedTechs.contains(tech.getPadreId());
        if (isUnlocked) {
            meta.displayName(Component.text(tech.getNombre(), NamedTextColor.GREEN, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("¡Desbloqueado!", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
        } else if (parentUnlocked) {
            meta.displayName(Component.text(tech.getNombre(), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text(""));
            lore.add(Component.text("Requisitos:", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            List<Requirement> requirements = RequirementParser.parse(tech.getRequisitos());
            for (Requirement req : requirements) { lore.add(MiniMessage.miniMessage().deserialize("  <aqua>● " + req.getLoreText())); }
            lore.add(Component.text(""));
            lore.add(Component.text("¡Click para desbloquear!", NamedTextColor.YELLOW, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        } else {
            meta.displayName(Component.text(tech.getNombre(), NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            Technology parentTech = techTree.get(tech.getPadreId());
            String parentName = parentTech != null ? parentTech.getNombre() : "desconocida";
            lore.add(Component.text(""));
            lore.add(Component.text("Bloqueado", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Requiere: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false).append(Component.text(parentName, NamedTextColor.WHITE)));
        }
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(TECH_ID_KEY, PersistentDataType.STRING, tech.getId());
        icon.setItemMeta(meta);
        return icon;
    }
    private void drawConnection(Inventory gui, Technology child, Technology parent, boolean isUnlocked) {
        Material lineMaterial = isUnlocked ? Material.LIME_STAINED_GLASS_PANE : Material.WHITE_STAINED_GLASS_PANE;
        int parentCol = parent.getColumn();
        int parentRow = parent.getRow();
        int childCol = child.getColumn();
        int childRow = child.getRow();
        for (int c = Math.min(parentCol, childCol) + 1; c < Math.max(parentCol, childCol); c++) { setLineItem(gui, parentRow, c, lineMaterial, 1001); }
        for (int r = Math.min(parentRow, childRow) + 1; r < Math.max(parentRow, childRow); r++) { setLineItem(gui, r, childCol, lineMaterial, 1002); }
        int cornerModelData = 1001;
        if (childRow != parentRow) {
            if (childCol > parentCol && childRow > parentRow) cornerModelData = 1004;
            else if (childCol < parentCol && childRow > parentRow) cornerModelData = 1003;
            else if (childCol > parentCol && childRow < parentRow) cornerModelData = 1006;
            else if (childCol < parentCol && childRow < parentRow) cornerModelData = 1005;
        }
        setLineItem(gui, parentRow, childCol, lineMaterial, cornerModelData);
    }
    private void setLineItem(Inventory gui, int row, int col, Material material, int modelData) {
        int slot = col + (row * 9);
        if (slot < 0 || slot >= gui.getSize()) return;
        ItemStack currentItem = gui.getItem(slot);
        if (currentItem != null && currentItem.getType() != Material.GRAY_STAINED_GLASS_PANE && currentItem.getType() != Material.BLUE_STAINED_GLASS_PANE) { return; }
        gui.setItem(slot, createGuiItem(material, " ", modelData));
    }
    private ItemStack createGuiItem(Material material, String name, int modelData) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));
        if (modelData != 0) { meta.setCustomModelData(modelData); }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }
}