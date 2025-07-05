package com.github.nationTech.commands.admin;

import com.github.nationTech.NationTech;
import com.github.nationTech.commands.SubCommand;
import com.github.nationTech.utils.MessageManager;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements SubCommand {

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getSyntax() {
        return "/ntca reload";
    }

    @Override
    public String getPermission() {
        return "nationtech.admin.reload";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        NationTech plugin = NationTech.getInstance();

        // Recargamos el config.yml
        plugin.reloadConfig();

        // Recargamos todos los árboles desde la base de datos a la caché.
        plugin.getTechnologyManager().loadAllTrees();

        MessageManager.sendMessage(sender, "<green>¡NationTech recargado con éxito!");
    }
}