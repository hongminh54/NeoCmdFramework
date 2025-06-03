package com.hongminh54.neocmd.objects;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public interface SubCommand {
    String getSubCommandId();

    void execute(CommandSender sender, String[] args);

    String getPermission();

    int minArgs();

    Map<Integer, List<String>> getTabCompleter(CommandSender sender, Command command, String alias, String[] args);

    boolean allowedConsole();
}
