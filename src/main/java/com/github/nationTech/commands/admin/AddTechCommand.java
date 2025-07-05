package com.github.nationTech.commands.admin;

import com.github.nationTech.NationTech;
import com.github.nationTech.commands.SubCommand;
import com.github.nationTech.managers.TechnologyManager;
import com.github.nationTech.model.TechnologyType;
import com.github.nationTech.utils.MessageManager;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AddTechCommand implements SubCommand {

    private final TechnologyManager technologyManager = NationTech.getInstance().getTechnologyManager();

    @Override
    public String getName() {
        return "addtech";
    }

    @Override
    public String getSyntax() {
        return "/ntca addtech <id> <fila> <col> <nombre> <padreId|null> <F|O> <requisitos> <icono> <beneficio> [nombre_arbol]";
    }

    @Override
    public String getPermission() {
        return "nationtech.admin.addtech";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 9) {
            MessageManager.sendMessage(sender, "<red>Argumentos insuficientes. Sintaxis:");
            MessageManager.sendRawMessage(sender, "<gray>" + getSyntax());
            return;
        }

        try {
            String id = args[0];
            int row = Integer.parseInt(args[1]);
            int column = Integer.parseInt(args[2]);
            String nombre = args[3].replace("_", " ");
            String padreId = args[4].equalsIgnoreCase("null") ? null : args[4];
            TechnologyType tipo = args[5].equalsIgnoreCase("F") ? TechnologyType.FINAL : TechnologyType.OPEN;
            String requisitos = args[6];
            String icono = args[7].toUpperCase();

            String treeId = TechnologyManager.OFFICIAL_TREE_ID;
            int benefitEndIndex = args.length;

            if (args.length > 9) {
                String potentialTreeId = args[args.length - 1].toLowerCase();
                if (technologyManager.getTreeNames().contains(potentialTreeId)) {
                    treeId = potentialTreeId;
                    benefitEndIndex = args.length - 1;
                }
            }

            StringBuilder beneficioBuilder = new StringBuilder();
            for (int i = 8; i < benefitEndIndex; i++) {
                beneficioBuilder.append(args[i]).append(" ");
            }
            String beneficio = beneficioBuilder.toString().trim();

            if (beneficio.isEmpty()) {
                MessageManager.sendMessage(sender, "<red>El comando de beneficio no puede estar vacío.");
                return;
            }

            boolean success = technologyManager.createTechnology(treeId, id, row, column, nombre, padreId, tipo, requisitos, icono, beneficio);

            if (success) {
                MessageManager.sendMessage(sender, "<green>Tecnología '<white>" + nombre + "</white>' creada en el árbol '<white>" + treeId + "</white>'.");
            } else {
                MessageManager.sendMessage(sender, "<red>Ya existe una tecnología con el ID '<white>" + id + "</white>' en el árbol '<white>" + treeId + "</white>'.");
            }

        } catch (NumberFormatException e) {
            MessageManager.sendMessage(sender, "<red>La fila y la columna deben ser números enteros.");
        } catch (Exception e) {
            MessageManager.sendMessage(sender, "<red>Ocurrió un error inesperado al procesar el comando.");
            e.printStackTrace();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        // La sintaxis es: <id> <fila> <col> <nombre> <padreId|null> <F|O> <req> <icono> <beneficio> [nombre_arbol]
        TechnologyManager tm = NationTech.getInstance().getTechnologyManager();
        List<String> completions = new ArrayList<>();
        String input = args[args.length - 1].toLowerCase();

        // Determina en qué árbol estamos trabajando para sugerir padres relevantes.
        String treeId = TechnologyManager.OFFICIAL_TREE_ID;
        if (args.length == 10) { // Si estamos escribiendo el nombre del árbol
            String potentialTreeId = args[8].toLowerCase(); // El argumento antes del que estamos escribiendo
            if (tm.getTreeNames().contains(potentialTreeId)) {
                treeId = potentialTreeId;
            }
        } else if (args.length > 10) { // Si ya hemos escrito el nombre del árbol
            String potentialTreeId = args[args.length - 2].toLowerCase();
            if (tm.getTreeNames().contains(potentialTreeId)) {
                treeId = potentialTreeId;
            }
        }

        switch (args.length) {
            case 5: // Sugerir <padreId|null>
                completions.add("null");
                completions.addAll(tm.getTechnologyTree(treeId).keySet());
                break;
            case 6: // Sugerir <F|O>
                completions.add("F");
                completions.add("O");
                break;
            case 8: // Sugerir <icono>
                for (Material mat : Material.values()) {
                    if (mat.isItem()) {
                        completions.add(mat.name().toLowerCase());
                    }
                }
                break;
            case 10: // Sugerir [nombre_arbol]
                completions.addAll(tm.getTreeNames());
                break;
            default:
                // No hay sugerencias para otros argumentos como id, nombre, etc.
                return new ArrayList<>();
        }

        // Filtra las sugerencias para que coincidan con lo que el usuario ya ha escrito
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(input))
                .collect(Collectors.toList());
    }
}