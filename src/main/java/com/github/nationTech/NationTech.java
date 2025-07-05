package com.github.nationTech;

import com.github.nationTech.commands.AdminCommandManager;
import com.github.nationTech.commands.PlayerCommandManager;
import com.github.nationTech.database.DatabaseManager;
import com.github.nationTech.gui.GUIManager;
import com.github.nationTech.listeners.GUIListener;
import com.github.nationTech.listeners.PlayerListener;
import com.github.nationTech.managers.TechnologyManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class NationTech extends JavaPlugin {

    private static NationTech instance;
    private DatabaseManager databaseManager;
    private TechnologyManager technologyManager;
    private GUIManager guiManager;
    private AdminCommandManager adminCommandManager;
    private PlayerCommandManager playerCommandManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        getLogger().info("Inicializando componentes...");
        this.guiManager = new GUIManager(this);
        this.databaseManager = new DatabaseManager(this);
        this.technologyManager = new TechnologyManager(this);

        this.databaseManager.connect();
        // --- CORRECCIÓN AQUÍ ---
        this.technologyManager.loadAllTrees(); // Se cambió el nombre del método

        getLogger().info("Registrando comandos...");
        this.adminCommandManager = new AdminCommandManager();
        this.playerCommandManager = new PlayerCommandManager(this.guiManager);
        this.getCommand("ntca").setExecutor(adminCommandManager);
        this.getCommand("ntc").setExecutor(playerCommandManager);

        getLogger().info("Registrando eventos...");
        getServer().getPluginManager().registerEvents(new GUIListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        getLogger().info("Verificando dependencias...");
        if (getServer().getPluginManager().getPlugin("Towny") == null) {
            getLogger().severe("¡Towny no encontrado! NationTech no puede funcionar sin Towny. Deshabilitando...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("NationTech v" + getDescription().getVersion() + " ha sido habilitado exitosamente.");
    }

    @Override
    public void onDisable() {
        if (this.databaseManager != null) {
            this.databaseManager.disconnect();
        }
        getLogger().info("NationTech ha sido deshabilitado.");
    }

    // --- Getters ---
    public static NationTech getInstance() { return instance; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public TechnologyManager getTechnologyManager() { return technologyManager; }
    public GUIManager getGuiManager() { return guiManager; }
}