package com.github.nationTech.commands;

import com.github.nationTech.commands.admin.*;
import com.github.nationTech.utils.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminCommandManager implements CommandExecutor {

    private final Map<String, SubCommand> subcommands = new HashMap<>();

    public AdminCommandManager() {
        // --- CORRECCIÓN AQUÍ ---
        // Se cambió ListDraftsCommand por ListTestCommand para que coincida con el nombre del archivo.
        registerSubCommand(new AddTechCommand());
        registerSubCommand(new CreateDraftCommand());
        registerSubCommand(new ListTestCommand()); // Nombre corregido
        registerSubCommand(new SetPackCommand());
        registerSubCommand(new RemoveTechCommand());
        registerSubCommand(new GiveTechCommand());
        registerSubCommand(new QuitTechCommand());
        registerSubCommand(new ReloadCommand());
    }

    private void registerSubCommand(SubCommand subCommand) {
        subcommands.put(subCommand.getName().toLowerCase(), subCommand);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            String availableCmds = subcommands.keySet().stream().sorted().collect(Collectors.joining(", "));
            MessageManager.sendMessage(sender, "Usa /ntca <subcomando> [argumentos...]");
            MessageManager.sendMessage(sender, "Subcomandos: <white>" + availableCmds);
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subcommands.get(subCommandName);

        if (subCommand == null) {
            MessageManager.sendMessage(sender, "<red>Comando desconocido. Usa /ntca para ver los subcomandos.");
            return true;
        }

        if (!sender.hasPermission(subCommand.getPermission())) {
            MessageManager.sendMessage(sender, "<red>No tienes permiso para ejecutar este comando.");
            return true;
        }

        String[] subCommandArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subCommandArgs, 0, args.length - 1);

        subCommand.execute(sender, subCommandArgs);

        return true;
    }
}