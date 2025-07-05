package com.github.nationTech.model;

/**
 * Representa una única tecnología en el sistema NationTech.
 * Esta clase es inmutable para garantizar la consistencia de los datos.
 */
public final class Technology {

    private final String id;
    private final String treeId; // ID del árbol al que pertenece (ej: "oficial", "test")
    private final String nombre;
    private final int row;
    private final int column;
    private final String padreId;
    private final TechnologyType tipo;
    private final String requisitos;
    private final String icono;
    private final String recompensa;

    /**
     * Constructor para crear un objeto de Tecnología.
     *
     * @param treeId     El ID del árbol al que pertenece la tecnología.
     * @param id         El ID único de la tecnología dentro de su árbol.
     * @param row        La fila en la que se mostrará en la GUI.
     * @param column     La columna en la que se mostrará en la GUI.
     * @param nombre     El nombre que se mostrará en el juego.
     * @param padreId    El ID de la tecnología padre requerida.
     * @param tipo       El tipo de tecnología (FINAL o OPEN).
     * @param requisitos La cadena de texto que define los requisitos.
     * @param icono      El material del ítem que se usará como icono.
     * @param recompensa El comando que se ejecutará como beneficio.
     */
    public Technology(String treeId, String id, int row, int column, String nombre, String padreId, TechnologyType tipo, String requisitos, String icono, String recompensa) {
        this.treeId = treeId;
        this.id = id;
        this.row = row;
        this.column = column;
        this.nombre = nombre;
        this.padreId = padreId;
        this.tipo = tipo;
        this.requisitos = requisitos;
        this.icono = icono;
        this.recompensa = recompensa;
    }

    // --- Getters ---
    // Métodos para acceder a los valores de la tecnología de forma segura.

    public String getId() { return id; }
    public String getTreeId() { return treeId; }
    public String getNombre() { return nombre; }
    public int getRow() { return row; }
    public int getColumn() { return column; }
    public String getPadreId() { return padreId; }
    public TechnologyType getTipo() { return tipo; }
    public String getRequisitos() { return requisitos; }
    public String getIcono() { return icono; }
    public String getRecompensa() { return recompensa; }

    /**
     * Calcula el slot del inventario basado en la fila y columna.
     * @return El número de slot (0-53).
     */
    public int getSlot() {
        return this.column + (this.row * 9);
    }
}