package com.hongminh54.neocmd;

import com.hongminh54.neocmd.objects.BaseCommand;
import com.hongminh54.neocmd.objects.SubCommand;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.*;

import java.util.*;

public class CommandManager implements CommandExecutor, TabCompleter {
    private final List<SubCommand> loadedCommands;
    private final Set<String> tabComplete;
    private String wrongArgumentMessage;
    private String noPermsMessage;
    private String noPlayerMessage;
    private final String command;
    private String prefix;
    private final String[] aliases;
    private SubCommand baseCommand;

    public CommandManager(String command, String prefix, String... aliases) {
        this.loadedCommands = new ArrayList<>();
        this.tabComplete = new HashSet<>();
        this.wrongArgumentMessage = "&cSai cú pháp!";
        this.noPermsMessage = "&4Bạn không có quyền làm điều này";
        this.noPlayerMessage = "&cKhông dành cho console";
        this.command = command;
        this.aliases = aliases;
        this.prefix = prefix;
        try {
            Bukkit.getPluginCommand(command).setTabCompleter(this);
            Bukkit.getPluginCommand(command).setExecutor(this);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Có lỗi khi đăng kí tab complete và lệnh");
            e.printStackTrace();
        }
    }

    public void register(SubCommand subCommand) {
        if (subCommand.getClass().isAnnotationPresent(BaseCommand.class)) {
            baseCommand = subCommand;
            return;
        }

        loadedCommands.add(subCommand);
        tabComplete.add(subCommand.getSubCommandId());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 && baseCommand == null) {
            sender.sendMessage(colora(prefix + wrongArgumentMessage));
            return true;
        } else if (args.length == 0) {
            if (!sender.hasPermission(baseCommand.getPermission())) return true;

            baseCommand.execute(sender, args);
            return true;
        }

        SubCommand cmd = getSubCommandFromArgs(args[0]);
        if (cmd == null) {
            sender.sendMessage(colora(prefix + wrongArgumentMessage));
            return true;
        }
        if (args[0].equals(cmd.getSubCommandId()) && args.length >= cmd.minArgs()) {
            if (!cmd.allowedConsole() && sender instanceof ConsoleCommandSender) {
                sender.sendMessage(colora(prefix + noPlayerMessage));
                return true;
            }
            if (sender.hasPermission(cmd.getPermission())) {
                cmd.execute(sender, args);
                return true;
            } else {
                sender.sendMessage(colora(prefix + noPermsMessage));
            }
            return true;
        } else {
            sender.sendMessage(colora(prefix + wrongArgumentMessage));
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equals(this.command) || Arrays.stream(aliases).filter(a -> a.equals(command.getName())).count() == 1) {
            SubCommand subCommand = getSubCommandFromArgs(args[0]);
            //tab complete visibile solo se hai permesso
            if (subCommand != null && !sender.hasPermission(subCommand.getPermission())) return new ArrayList<>();
            if (subCommand != null && args[0].equals(subCommand.getSubCommandId())) {
                if (subCommand.getTabCompleter(sender, command, alias, args) != null) {
                    return subCommand.getTabCompleter(sender, command, alias, args).get(args.length - 1);
                }
            }
            if (args.length == 1) {
                return new ArrayList<>(tabComplete);
            }
        }
        return null;
    }

    private SubCommand getSubCommandFromArgs(String args0) {
        for (SubCommand subCommand : loadedCommands) {
            if (subCommand.getSubCommandId().equals(args0)) {
                return subCommand;
            }
        }
        return null;
    }

    public void setWrongArgumentMessage(String wrongArgumentMessage) {
        this.wrongArgumentMessage = wrongArgumentMessage;
    }

    public void setNoPermsMessage(String noPermsMessage) {
        this.noPermsMessage = noPermsMessage;
    }

    public void setNoPlayerMessage(String noPlayerMessage) {
        this.noPlayerMessage = noPlayerMessage;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    private String colora(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }
}
