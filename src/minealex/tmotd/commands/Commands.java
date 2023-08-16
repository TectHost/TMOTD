package minealex.tmotd.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import minealex.tmotd.TMOTD;

public class Commands implements CommandExecutor {

    private final TMOTD plugin;

    public Commands(TMOTD plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("motd")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("set") && args.length >= 3) {
                    if (!sender.hasPermission("tmotd.set")) {
                        sender.sendMessage(plugin.getNoPermissionMsg());
                        return true;
                    }
                    StringBuilder newMotd = new StringBuilder();
                    for (int i = 1; i < args.length; i++) {
                        newMotd.append(args[i]);
                        if (i < args.length - 1) {
                            newMotd.append(" ");
                        }
                    }
                    plugin.setMotd(newMotd.toString());
                    sender.sendMessage(plugin.getMotdSetSuccessMsg());
                    return true;
                } else if (args[0].equalsIgnoreCase("reload")) {
                    if (!sender.hasPermission("tmotd.reload")) {
                        sender.sendMessage(plugin.getNoPermissionMsg());
                        return true;
                    }
                    plugin.reloadConfig();
                    plugin.loadConfig();
                    sender.sendMessage(plugin.getMotdReloadMsg());
                    return true;
                } else if (args[0].equalsIgnoreCase("version")) {
                    String pluginVersionMsg = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.plugin_version_msg"));
                    pluginVersionMsg = pluginVersionMsg.replace("%version%", plugin.getDescription().getVersion());
                    sender.sendMessage(pluginVersionMsg);
                    return true;
                }
            }
            sender.sendMessage(ChatColor.RED + "Usage:");
            sender.sendMessage(plugin.getMotdSetUsageMsg());
            return true;
        }
        return false;
    }
}
