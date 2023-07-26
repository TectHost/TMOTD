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

    @Override
    public void onEnable() {
        saveDefaultConfig(); // Guarda el archivo config.yml en caso de que no exista.
        loadConfig(); // Carga la configuración del archivo.
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("motd").setExecutor(this); // Registra el comando personalizado

        // Iniciar el ciclo de cambio de MOTD cada 'changeInterval' segundos
        int ticks = 20 * changeInterval; // Convertir segundos a ticks (20 ticks = 1 segundo)
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this::changeMotd, ticks, ticks);
    }

    public void loadConfig() {
        // Carga los valores de la configuración desde el archivo config.yml
        motdList = new ArrayList<>();
        if (getConfig().contains("motds.motd_list")) {
            List<String> motdKeys = getConfig().getStringList("motds.motd_list");
            for (String key : motdKeys) {
                List<String> motdLines = getConfig().getStringList("motds." + key + ".lines");
                motdList.add(motdLines);
            }
        } else {
            // Crea una lista de MOTDs por defecto si no se encuentra en el archivo config.yml
            List<String> motdLines = new ArrayList<>();
            motdLines.add("&aWelcome to the server!");
            motdLines.add("&6Have fun playing!");
            motdList.add(motdLines);
            getConfig().set("motds.motd_list", new ArrayList<>());
            saveConfig();
        }

        currentMotd = getConfig().getInt("motds.current_motd", 0);
        changeInterval = getConfig().getInt("motds.change_interval", 5);

        // Asegurarnos de que el índice del MOTD actual esté dentro de los límites de la lista
        currentMotd = Math.max(0, Math.min(currentMotd, motdList.size() - 1));

        formatColors(); // Formatea los colores en las líneas del MOTD actual

        // Cargar mensajes customizables
        motdSetSuccessMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.motd_set_success"));
        motdSetUsageMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.motd_set_usage"));
        motdReloadMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.motd_reload"));
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
            String hexColor = matcher.group().substring(2); // Elimina los caracteres "&#" del color hexadecimal
            ChatColor color = ChatColor.getByChar(hexColor.charAt(0)); // Obtiene el color correspondiente
            matcher.appendReplacement(buffer, color.toString());
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private void changeMotd() {
        currentMotd = new Random().nextInt(motdList.size());
        formatColors(); // Formatea los colores en las líneas del nuevo MOTD
    }

    // Métodos del comando /motd...

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("motd")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("set") && args.length >= 3) {
                    // Código para actualizar el MOTD, igual que antes
                    // ...
                    sender.sendMessage(motdSetSuccessMsg); // Enviar mensaje personalizado
                    return true;
                } else if (args[0].equalsIgnoreCase("reload")) {
                    reloadConfig(); // Recarga la configuración del archivo config.yml
                    loadConfig(); // Vuelve a cargar la configuración en el plugin
                    sender.sendMessage(motdReloadMsg); // Enviar mensaje personalizado
                    return true;
                }
            }
            // Mostrar el uso correcto del comando
            sender.sendMessage(ChatColor.RED + "Uso:");
            sender.sendMessage(motdSetUsageMsg); // Enviar mensaje personalizado
            return true;
        }
        return false;
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        List<String> motdLines = motdList.get(currentMotd);
        event.setMotd(String.join("\n", motdLines));
    }
}
