package com.github.nationTech.commands.admin;

import com.github.nationTech.NationTech;
import com.github.nationTech.commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ListTestCommand implements SubCommand {
    @Override
    public String getName() {
        // El nombre del comando que se usa en el juego
        return "listtest";
    }

    @Override
    public String getSyntax() {
        return "/ntca listtest";
    }

    @Override
    public String getPermission() {
        return "nationtech.admin.listtest";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando solo se puede usar en el juego.");
            return;
        }
        Player player = (Player) sender;
        // Llamamos al GUIManager para que abra el menú de selección de árboles.
        NationTech.getInstance().getGuiManager().openDraftSelectionGUI(player);
    }
}