package com.github.nationTech;

import com.github.nationTech.commands.AdminCommandManager;
import com.github.nationTech.commands.PlayerCommandManager;
import com.github.nationTech.database.DatabaseManager;
import com.github.nationTech.gui.GUIManager;
import com.github.nationTech.listeners.GUIListener;
import com.github.nationTech.listeners.PlayerListener;
import com.github.nationTech.managers.TechnologyManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Clase principal del plugin NationTech.
 * Se encarga de la inicialización y desactivación de todos los componentes del plugin.
 *
 * @version 0.0.2-ALPHA-EXPERIMENTAL
 * @author TuNombre
 */
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

        // Carga y guarda el archivo config.yml por defecto si no existe.
        saveDefaultConfig();

        getLogger().info("Inicializando componentes...");

        // Inicializamos los gestores en el orden de dependencia.
        this.guiManager = new GUIManager(this);
        this.databaseManager = new DatabaseManager(this);
        this.technologyManager = new TechnologyManager(this);

        // Conectamos a la base de datos y cargamos todos los árboles tecnológicos en la caché.
        this.databaseManager.connect();
        this.technologyManager.loadAllTrees();

        getLogger().info("Registrando comandos...");
        this.adminCommandManager = new AdminCommandManager();
        this.playerCommandManager = new PlayerCommandManager(this.guiManager);
        this.getCommand("ntca").setExecutor(adminCommandManager);
        this.getCommand("ntca").setTabCompleter(adminCommandManager); // Registramos el TabCompleter
        this.getCommand("ntc").setExecutor(playerCommandManager);

        getLogger().info("Registrando eventos...");
        // Listener para la interactividad de la GUI.
        getServer().getPluginManager().registerEvents(new GUIListener(), this);
        // Listener para enviar el paquete de recursos a los jugadores al conectarse.
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
        // Se asegura de cerrar la conexión a la base de datos de forma segura al apagar el servidor.
        if (this.databaseManager != null) {
            this.databaseManager.disconnect();
        }
        getLogger().info("NationTech ha sido deshabilitado.");
    }

    /**
     * Permite obtener la instancia principal del plugin desde cualquier otra clase.
     * @return La instancia de NationTech.
     */
    public static NationTech getInstance() {
        return instance;
    }

    /**
     * Permite obtener el gestor de la base de datos.
     * @return La instancia del DatabaseManager.
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * Permite obtener el gestor de tecnologías.
     * @return La instancia del TechnologyManager.
     */
    public TechnologyManager getTechnologyManager() {
        return technologyManager;
    }

    /**
     * Permite obtener el gestor de la interfaz gráfica (GUI).
     * @return La instancia del GUIManager.
     */
    public GUIManager getGuiManager() {
        return guiManager;
    }
}