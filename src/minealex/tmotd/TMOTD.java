package minealex.tmotd;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TMOTD extends JavaPlugin implements Listener, CommandExecutor {

    private List<List<String>> motdList;
    private int currentMotd;
    private int changeInterval;
    private String motdSetSuccessMsg;
    private String motdSetUsageMsg;
    private String motdReloadMsg;
    private String noPermissionMsg;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("motd").setExecutor(this);

        int ticks = 20 * changeInterval;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this::changeMotd, ticks, ticks);
    }

    public void loadConfig() {
        motdList = new ArrayList<>();
        if (getConfig().contains("motds.motd_list")) {
            List<String> motdKeys = getConfig().getStringList("motds.motd_list");
            for (String key : motdKeys) {
                List<String> motdLines = getConfig().getStringList("motds." + key + ".lines");
                motdList.add(motdLines);
            }
        } else {
            List<String> motdLines = new ArrayList<>();
            motdLines.add("&aWelcome to the server!");
            motdLines.add("&6Have fun playing!");
            motdList.add(motdLines);
            getConfig().set("motds.motd_list", new ArrayList<>());
            saveConfig();
        }

        currentMotd = getConfig().getInt("motds.current_motd", 0);
        changeInterval = getConfig().getInt("motds.change_interval", 5);

        currentMotd = Math.max(0, Math.min(currentMotd, motdList.size() - 1));

        formatColors();

        motdSetSuccessMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.motd_set_success"));
        motdSetUsageMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.motd_set_usage"));
        motdReloadMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.motd_reload"));
        noPermissionMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.no_permission_msg"));
    }

    private void formatColors() {
        List<String> motdLines = motdList.get(currentMotd);
        for (int i = 0; i < motdLines.size(); i++) {
            motdLines.set(i, ChatColor.translateAlternateColorCodes('&', motdLines.get(i)));
            motdLines.set(i, convertHexColors(motdLines.get(i)));
        }
    }

    private String convertHexColors(String message) {
        Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);

        while (matcher.find()) {
            String hexColor = matcher.group().substring(2);
            ChatColor color = ChatColor.getByChar(hexColor.charAt(0));
            matcher.appendReplacement(buffer, color.toString());
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private void changeMotd() {
        currentMotd = new Random().nextInt(motdList.size());
        formatColors();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("motd")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("set") && args.length >= 3) {
                    if (!sender.hasPermission("tmotd.set")) {
                        sender.sendMessage(noPermissionMsg);
                        return true;
                    }
                    StringBuilder newMotd = new StringBuilder();
                    for (int i = 1; i < args.length; i++) {
                        newMotd.append(args[i]);
                        if (i < args.length - 1) {
                            newMotd.append(" ");
                        }
                    }
                    setMotd(newMotd.toString());
                    sender.sendMessage(motdSetSuccessMsg);
                    return true;
                } else if (args[0].equalsIgnoreCase("reload")) {
                    if (!sender.hasPermission("tmotd.reload")) {
                        sender.sendMessage(noPermissionMsg);
                        return true;
                    }
                    reloadConfig();
                    loadConfig();
                    sender.sendMessage(motdReloadMsg);
                    return true;
                } else if (args[0].equalsIgnoreCase("version")) {
                    if (!sender.hasPermission("tmotd.version")) {
                        sender.sendMessage(noPermissionMsg);
                        return true;
                    }
                    String pluginVersionMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.plugin_version_msg"));
                    pluginVersionMsg = pluginVersionMsg.replace("%version%", getDescription().getVersion());
                    sender.sendMessage(pluginVersionMsg);
                    return true;
                }
            }
            sender.sendMessage(ChatColor.RED + "Usage:");
            sender.sendMessage(motdSetUsageMsg);
            return true;
        }
        return false;
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        List<String> motdLines = motdList.get(currentMotd);
        event.setMotd(String.join("\n", motdLines));
    }

    public void setMotd(String newMotd) {
        List<String> motdLines = new ArrayList<>();
        String[] lines = newMotd.split("\\\\n");
        for (String line : lines) {
            motdLines.add(line);
        }
        motdList.set(currentMotd, motdLines);
        getConfig().set("motds." + "motd" + currentMotd + ".lines", motdLines);
        saveConfig();
    }

    public String getMotdSetSuccessMsg() {
        return motdSetSuccessMsg;
    }

    public String getMotdSetUsageMsg() {
        return motdSetUsageMsg;
    }

    public String getMotdReloadMsg() {
        return motdReloadMsg;
    }

    public String getNoPermissionMsg() {
        return noPermissionMsg;
    }
}
