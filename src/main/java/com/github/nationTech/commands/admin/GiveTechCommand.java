package com.github.nationTech.commands.admin;

import com.github.nationTech.NationTech;
import com.github.nationTech.commands.SubCommand;
import com.github.nationTech.database.DatabaseManager;
import com.github.nationTech.utils.MessageManager;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import org.bukkit.command.CommandSender;

public class GiveTechCommand implements SubCommand {

    @Override
    public String getName() {
        return "give";
    }

    @Override
    public String getSyntax() {
        return "/ntca give <id_tecnologia> <nombre_nacion>";
    }

    @Override
    public String getPermission() {
        return "nationtech.admin.give";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageManager.sendMessage(sender, "<red>Argumentos insuficientes. Sintaxis:");
            MessageManager.sendRawMessage(sender, "<gray>" + getSyntax());
            return;
        }

        String techId = args[0];
        String nationName = args[1];

        Nation nation = TownyAPI.getInstance().getNation(nationName);
        if (nation == null) {
            MessageManager.sendMessage(sender, "<red>La nación '<white>" + nationName + "</white>' no existe.");
            return;
        }

        DatabaseManager dbManager = NationTech.getInstance().getDatabaseManager();
        // Añadimos el progreso directamente. No consume recursos ni da recompensa.
        dbManager.addUnlockedTechnology(nation.getUUID(), techId);
        MessageManager.sendMessage(sender, "<green>Tecnología '<white>" + techId + "</white>' otorgada a la nación '<white>" + nation.getName() + "</white>'.");
    }
}