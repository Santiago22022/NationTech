package com.github.nationTech.commands.admin;

import com.github.nationTech.NationTech;
import com.github.nationTech.commands.SubCommand;
import com.github.nationTech.managers.TechnologyManager;
import com.github.nationTech.utils.MessageManager;
import org.bukkit.command.CommandSender;

public class RemoveTechCommand implements SubCommand {

    private final TechnologyManager technologyManager = NationTech.getInstance().getTechnologyManager();

    @Override
    public String getName() {
        return "removetech";
    }

    @Override
    public String getSyntax() {
        return "/ntca removetech <id_tecnologia> [nombre_arbol]";
    }

    @Override
    public String getPermission() {
        return "nationtech.admin.removetech";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            MessageManager.sendMessage(sender, "<red>Argumentos insuficientes. Sintaxis:");
            MessageManager.sendRawMessage(sender, "<gray>" + getSyntax());
            return;
        }

        String techId = args[0];
        // Si no se especifica el árbol, se usa el oficial por defecto.
        String treeId = (args.length > 1) ? args[1].toLowerCase() : TechnologyManager.OFFICIAL_TREE_ID;

        if (technologyManager.deleteTechnology(treeId, techId)) {
            MessageManager.sendMessage(sender, "<green>Tecnología '<white>" + techId + "</white>' eliminada del árbol '<white>" + treeId + "</white>' con éxito.");
        } else {
            MessageManager.sendMessage(sender, "<red>No se encontró ninguna tecnología con el ID '<white>" + techId + "</white>' en el árbol '<white>" + treeId + "</white>'.");
        }
    }
}