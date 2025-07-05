package com.github.nationTech.commands.admin;

import com.github.nationTech.NationTech;
import com.github.nationTech.commands.SubCommand;
import com.github.nationTech.managers.TechnologyManager;
import com.github.nationTech.utils.MessageManager;
import org.bukkit.command.CommandSender;

public class CreateDraftCommand implements SubCommand {

    @Override
    public String getName() {
        return "create";
    }

    // --- CORRECCIÓN AQUÍ ---
    @Override
    public String getSyntax() {
        return "/ntca create <nombreBorrador>";
    }

    @Override
    public String getPermission() {
        return "nationtech.admin.create";
    }
    // --- FIN DE LA CORRECCIÓN ---

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            MessageManager.sendMessage(sender, "<red>Argumentos insuficientes. Sintaxis:");
            MessageManager.sendRawMessage(sender, "<gray>" + getSyntax());
            return;
        }

        String draftName = args[0].toLowerCase();
        if (draftName.equals(TechnologyManager.OFFICIAL_TREE_ID)) {
            MessageManager.sendMessage(sender, "<red>No puedes usar el nombre reservado 'oficial'.");
            return;
        }

        TechnologyManager tm = NationTech.getInstance().getTechnologyManager();
        if (tm.createDraft(draftName)) {
            MessageManager.sendMessage(sender, "<green>Borrador '<white>" + draftName + "</white>' creado. Ahora puedes añadirle tecnologías.");
        } else {
            MessageManager.sendMessage(sender, "<red>El borrador '<white>" + draftName + "</white>' ya existe.");
        }
    }
}