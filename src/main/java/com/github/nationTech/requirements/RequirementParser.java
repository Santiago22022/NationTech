package com.github.nationTech.requirements;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public class RequirementParser {

    /**
     * Analiza la cadena de requisitos de una tecnología.
     * Formato esperado: "tipo:valor:cantidad,tipo2:valor2:cantidad2"
     * Por ahora, solo soporta "item:MATERIAL_NAME:cantidad"
     * @param requirementString La cadena de texto a analizar.
     * @return Una lista de objetos Requirement.
     */
    public static List<Requirement> parse(String requirementString) {
        List<Requirement> requirements = new ArrayList<>();
        if (requirementString == null || requirementString.trim().isEmpty()) {
            return requirements;
        }

        String[] parts = requirementString.split(",");
        for (String part : parts) {
            String[] details = part.split(":");
            if (details.length != 3) {
                // Ignorar formato incorrecto por ahora, podríamos loguear un error aquí.
                continue;
            }

            String type = details[0].toLowerCase();
            if (type.equals("item")) {
                try {
                    Material material = Material.matchMaterial(details[1].toUpperCase());
                    int amount = Integer.parseInt(details[2]);
                    if (material != null) {
                        requirements.add(new ItemRequirement(material, amount));
                    }
                } catch (Exception e) {
                    // Error al parsear material o cantidad
                }
            }
            // Futuro: añadir else if para "block", "economy", "advanceditem", etc.
        }
        return requirements;
    }
}