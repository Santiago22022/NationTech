package com.github.nationTech.listeners;

import com.github.nationTech.NationTech;
import com.github.nationTech.gui.GUIManager;
import com.github.nationTech.managers.TechnologyManager;
import com.github.nationTech.model.Technology;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public class GUIListener implements Listener {

    private final NationTech plugin = NationTech.getInstance();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getItemMeta() == null) {
            return;
        }
        event.setCancelled(true);
        PersistentDataContainer data = clickedItem.getItemMeta().getPersistentDataContainer();

        // 1. ¿El clic fue en el menú de selección de borradores?
        if (title.contains("Seleccionar Árbol Tecnológico")) {
            if (data.has(GUIManager.TREE_ID_KEY, PersistentDataType.STRING)) {
                String treeId = data.get(GUIManager.TREE_ID_KEY, PersistentDataType.STRING);
                // --- CORRECCIÓN AQUÍ ---
                // Llamamos a la nueva vista de administrador.
                plugin.getGuiManager().openAdminTechGUI(player, treeId);
            }
            return;
        }

        // 2. ¿El clic fue en el menú de un árbol tecnológico para un jugador?
        if (title.contains("Tecnologías de")) {
            if (data.has(GUIManager.TECH_ID_KEY, PersistentDataType.STRING)) {
                String techId = data.get(GUIManager.TECH_ID_KEY, PersistentDataType.STRING);
                handleTechTreeClick(player, techId);
            }
        }

        // 3. Si el clic es en la vista de admin, no hacemos nada (es de solo lectura).
        if (title.contains("Vista de Admin")) {
            // El event.setCancelled(true) ya previene cualquier interacción.
        }
    }

    private void handleTechTreeClick(Player player, String techId) {
        TechnologyManager techManager = plugin.getTechnologyManager();
        Map<String, Technology> officialTree = techManager.getTechnologyTree(TechnologyManager.OFFICIAL_TREE_ID);
        Technology tech = officialTree.get(techId);

        if (tech == null) return;

        Resident resident = TownyAPI.getInstance().getResident(player.getUniqueId());
        if (resident == null || !resident.hasNation()) {
            return;
        }

        try {
            Nation nation = resident.getNation();
            techManager.attemptUnlockTechnology(player, nation, tech);
        } catch (Exception e) {
            player.closeInventory();
        }
    }
}