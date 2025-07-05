package com.github.nationTech.database;

import com.github.nationTech.NationTech;
import com.github.nationTech.model.Technology;
import com.github.nationTech.model.TechnologyType;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Gestiona la conexión y la estructura de la base de datos SQLite del plugin.
 * Actualizado para soportar múltiples árboles tecnológicos (oficial y borradores).
 */
public class DatabaseManager {

    private final NationTech plugin;
    private Connection connection;
    private final File dbFile;

    public DatabaseManager(NationTech plugin) {
        this.plugin = plugin;
        this.dbFile = new File(plugin.getDataFolder(), "nationtech.db");
    }

    /**
     * Establece la conexión con la base de datos SQLite.
     */
    public void connect() {
        if (!dbFile.getParentFile().exists()) {
            dbFile.getParentFile().mkdirs();
        }
        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            setupTables();
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo conectar a la base de datos SQLite.", e);
        }
    }

    /**
     * Desconecta de la base de datos de forma segura.
     */
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al cerrar la conexión con la base de datos.", e);
        }
    }

    /**
     * Crea o actualiza las tablas de la base de datos para que coincidan con el modelo actual.
     */
    private void setupTables() {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS tecnologias (id TEXT NOT NULL, tree_id TEXT NOT NULL, nombre TEXT NOT NULL, padre_id TEXT, tipo TEXT NOT NULL, row INTEGER NOT NULL, column INTEGER NOT NULL, requisitos TEXT NOT NULL, icono TEXT NOT NULL, recompensa TEXT NOT NULL, PRIMARY KEY (id, tree_id));");
            statement.execute("CREATE TABLE IF NOT EXISTS progreso_naciones (nation_uuid TEXT NOT NULL, tecnologia_id TEXT NOT NULL, PRIMARY KEY (nation_uuid, tecnologia_id));");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudieron crear las tablas de la base de datos.", e);
        }
    }

    /**
     * Carga todas las tecnologías de todos los árboles desde la base de datos.
     * @return Una lista con todos los objetos Technology encontrados.
     */
    public List<Technology> loadAllTechnologies() {
        List<Technology> technologies = new ArrayList<>();
        String sql = "SELECT * FROM tecnologias;";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                technologies.add(new Technology(rs.getString("tree_id"), rs.getString("id"), rs.getInt("row"), rs.getInt("column"), rs.getString("nombre"), rs.getString("padre_id"), TechnologyType.valueOf(rs.getString("tipo")), rs.getString("requisitos"), rs.getString("icono"), rs.getString("recompensa")));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al cargar las tecnologías desde la base de datos.", e);
        }
        return technologies;
    }

    /**
     * Guarda o actualiza una tecnología en la base de datos.
     * @param tech El objeto Technology a guardar, que ya contiene su tree_id.
     */
    public void saveTechnology(Technology tech) {
        String sql = "INSERT OR REPLACE INTO tecnologias (id, tree_id, nombre, padre_id, tipo, row, column, requisitos, icono, recompensa) VALUES(?,?,?,?,?,?,?,?,?,?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, tech.getId());
            pstmt.setString(2, tech.getTreeId());
            pstmt.setString(3, tech.getNombre());
            pstmt.setString(4, tech.getPadreId());
            pstmt.setString(5, tech.getTipo().name());
            pstmt.setInt(6, tech.getRow());
            pstmt.setInt(7, tech.getColumn());
            pstmt.setString(8, tech.getRequisitos());
            pstmt.setString(9, tech.getIcono());
            pstmt.setString(10, tech.getRecompensa());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al guardar la tecnología " + tech.getId() + " en el árbol " + tech.getTreeId(), e);
        }
    }

    /**
     * Elimina una tecnología específica de un árbol específico en la base de datos.
     * @param treeId El ID del árbol del que se eliminará la tecnología.
     * @param technologyId El ID de la tecnología a eliminar.
     */
    public void deleteTechnology(String treeId, String technologyId) {
        String sql = "DELETE FROM tecnologias WHERE tree_id = ? AND id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, treeId);
            pstmt.setString(2, technologyId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al eliminar la tecnología " + technologyId + " del árbol " + treeId, e);
        }
    }

    /**
     * Obtiene todos los IDs de las tecnologías desbloqueadas por una nación.
     * @param nationUUID El UUID de la nación.
     * @return Un Set con los IDs de las tecnologías.
     */
    public Set<String> getNationUnlockedTechs(UUID nationUUID) {
        Set<String> unlockedTechs = new HashSet<>();
        String sql = "SELECT tecnologia_id FROM progreso_naciones WHERE nation_uuid = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, nationUUID.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                unlockedTechs.add(rs.getString("tecnologia_id"));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al cargar el progreso de la nación " + nationUUID, e);
        }
        return unlockedTechs;
    }

    /**
     * Registra una nueva tecnología como desbloqueada para una nación.
     * @param nationUUID El UUID de la nación.
     * @param techId El ID de la tecnología.
     */
    public void addUnlockedTechnology(UUID nationUUID, String techId) {
        String sql = "INSERT INTO progreso_naciones (nation_uuid, tecnologia_id) VALUES(?,?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, nationUUID.toString());
            pstmt.setString(2, techId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al guardar el progreso de la nación " + nationUUID, e);
        }
    }

    /**
     * Elimina una tecnología desbloqueada del progreso de una nación.
     * @param nationUUID El UUID de la nación.
     * @param techId El ID de la tecnología a eliminar.
     */
    public void removeUnlockedTechnology(UUID nationUUID, String techId) {
        String sql = "DELETE FROM progreso_naciones WHERE nation_uuid = ? AND tecnologia_id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, nationUUID.toString());
            pstmt.setString(2, techId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al eliminar el progreso de la tecnología " + techId + " para la nación " + nationUUID, e);
        }
    }
}