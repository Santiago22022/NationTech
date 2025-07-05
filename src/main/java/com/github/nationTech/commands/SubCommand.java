package com.github.nationTech.commands;

import org.bukkit.command.CommandSender;

public interface SubCommand {

    /**
     * Se ejecuta cuando un jugador usa el subcomando.
     * @param sender Quien ejecutó el comando.
     * @param args Los argumentos que siguen al subcomando.
     */
    void execute(CommandSender sender, String[] args);

    /**
     * El nombre del subcomando.
     * @return El nombre.
     */
    String getName();

    /**
     * La sintaxis del subcomando.
     * @return La sintaxis.
     */
    String getSyntax();

    /**
     * El permiso necesario para ejecutar el comando.
     * @return El nodo de permiso.
     */
    String getPermission();
}