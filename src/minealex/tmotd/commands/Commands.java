package minealex.tmotd.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import minealex.tmotd.TMOTD;

public class Commands implements CommandExecutor {

    private final TMOTD plugin;
    private String motdSetSuccessMsg;
    private String motdSetUsageMsg;
    private String motdReloadMsg;
    private String noPermissionMsg;

    public Commands(TMOTD plugin) {
        this.plugin = plugin;

        // Cargar mensajes customizables desde el archivo config.yml
        motdSetSuccessMsg = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.motd_set_success"));
        motdSetUsageMsg = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.motd_set_usage"));
        motdReloadMsg = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.motd_reload"));
        noPermissionMsg = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no_permission_msg"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("motd")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("set") && args.length >= 3) {
                    // Verificar el permiso para ejecutar /motd set
                    if (!sender.hasPermission("tmotd.set")) {
                        sender.sendMessage(noPermissionMsg);
                        return true;
                    }
                    // Código para actualizar el MOTD, igual que antes
                    // ...
                    sender.sendMessage(motdSetSuccessMsg); // Enviar mensaje personalizado
                    return true;
                } else if (args[0].equalsIgnoreCase("reload")) {
                    // Verificar el permiso para ejecutar /motd reload
                    if (!sender.hasPermission("tmotd.reload")) {
                        sender.sendMessage(noPermissionMsg);
                        return true;
                    }
                    plugin.reloadConfig(); // Recarga la configuración del archivo config.yml
                    plugin.loadConfig(); // Vuelve a cargar la configuración en el plugin
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
}
