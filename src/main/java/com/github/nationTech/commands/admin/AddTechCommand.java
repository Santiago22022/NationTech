package com.github.nationTech.commands.admin;

import com.github.nationTech.NationTech;
import com.github.nationTech.commands.SubCommand;
import com.github.nationTech.managers.TechnologyManager;
import com.github.nationTech.model.TechnologyType;
import com.github.nationTech.utils.MessageManager;
import org.bukkit.command.CommandSender;

public class AddTechCommand implements SubCommand {

    private final TechnologyManager technologyManager = NationTech.getInstance().getTechnologyManager();

    @Override
    public String getName() {
        return "addtech";
    }

    @Override
    public String getSyntax() {
        // La sintaxis ahora incluye el argumento opcional [nombre_arbol]
        return "/ntca addtech <id> <fila> <col> <nombre> <padreId|null> <F|O> <requisitos> <icono> <beneficio> [nombre_arbol]";
    }

    @Override
    public String getPermission() {
        return "nationtech.admin.addtech";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // El mínimo de argumentos es 9. El décimo (nombre_arbol) es opcional.
        if (args.length < 9) {
            MessageManager.sendMessage(sender, "<red>Argumentos insuficientes. Sintaxis:");
            MessageManager.sendRawMessage(sender, "<gray>" + getSyntax());
            return;
        }

        try {
            // --- Parseo de Argumentos ---
            String id = args[0];
            int row = Integer.parseInt(args[1]);
            int column = Integer.parseInt(args[2]);
            String nombre = args[3].replace("_", " ");
            String padreId = args[4].equalsIgnoreCase("null") ? null : args[4];
            TechnologyType tipo = args[5].equalsIgnoreCase("F") ? TechnologyType.FINAL : TechnologyType.OPEN;
            String requisitos = args[6];
            String icono = args[7];

            // Lógica para determinar el árbol y el beneficio
            String treeId = TechnologyManager.OFFICIAL_TREE_ID;
            int benefitEndIndex = args.length;

            // Si hay 10 o más argumentos, el último PUEDE ser el nombre del árbol.
            if (args.length > 9) {
                // Comprobamos si el último argumento es un nombre de árbol existente.
                // Esta es una forma simple de diferenciarlo de parte del comando de beneficio.
                // Una mejora futura podría ser usar un prefijo, ej: "tree:test".
                String potentialTreeId = args[args.length - 1].toLowerCase();
                if (technologyManager.getTreeNames().contains(potentialTreeId)) {
                    treeId = potentialTreeId;
                    benefitEndIndex = args.length - 1; // El beneficio termina un argumento antes.
                }
            }

            // Unimos los argumentos restantes para formar el comando de beneficio.
            StringBuilder beneficioBuilder = new StringBuilder();
            for (int i = 8; i < benefitEndIndex; i++) {
                beneficioBuilder.append(args[i]).append(" ");
            }
            String beneficio = beneficioBuilder.toString().trim();

            if (beneficio.isEmpty()) {
                MessageManager.sendMessage(sender, "<red>El comando de beneficio no puede estar vacío.");
                return;
            }

            // --- Creación de la Tecnología ---
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
}