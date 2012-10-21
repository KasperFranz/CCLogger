package me.alrik94.plugins.cclogger;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandLogger implements Listener {

    private CCLogger plugin;
    private Notifier notifier;

    public CommandLogger(final CCLogger plugins) {
        plugins.getServer().getPluginManager().registerEvents(this, plugins);
        plugin = plugins;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) throws IOException {
        //player instance
        Player player = event.getPlayer();
        //player name
        String name = player.getName();
        //player command
        String command = event.getMessage();
        //player Location instance
        Location location = player.getLocation();
        //player X coordinate
        int xLocation = (int) location.getX();
        //player Y coordinate
        int yLocation = (int) location.getY();
        //player Z coordinate
        int zLocation = (int) location.getZ();
        //player World instance
        World world = location.getWorld();
        //player IP address
        String ipAddress = player.getAddress().getAddress().getHostAddress();
        //player world name
        String worldName = world.getName();
        //process date and time
        String date = getDate();
        //Check player
        checkPlayer(name);
        //process ALL the informations
        processInformation(player, name, command, xLocation, yLocation, zLocation, worldName, date, ipAddress);

    }

    public void processInformation(Player player, String playerName, String command, int x, int y, int z, String worldName, String date, String ipAddress) {
        boolean playerCommand = plugin.getConfig().getBoolean("Log.toggle.playerCommands");
        boolean globalCommand = plugin.getConfig().getBoolean("Log.toggle.globalCommands");
        boolean logNotifyCommands = plugin.getConfig().getBoolean("Log.toggle.logNotifyCommands");
        boolean inGameNotifications = plugin.getConfig().getBoolean("Log.toggle.inGameNotifications");
        File playersFolder = new File(plugin.getDataFolder(), "players");
        File commandFile = new File(plugin.getDataFolder(), "commands.log");
        File playerFile = new File(playersFolder, playerName + ".log");
        File notifyCommandFile = new File(plugin.getDataFolder(), "notifyCommands.log");



        if (!checkExemptionList(player)) {
            if (globalCommand) {
                if (!commandCheck(command)) {
                    Writer.writeFile(formatLog(playerName, command, x, y, z, worldName, date, ipAddress), commandFile);
                }
            }
            if (playerCommand) {
                if (!commandCheck(command)) {
                    Writer.writeFile(formatLog(playerName, command, x, y, z, worldName, date, ipAddress), playerFile);
                }
            }
            if (checkNotifyList(command) && logNotifyCommands) {
                Writer.writeFile(formatLog(playerName, command, x, y, z, worldName, date, ipAddress), notifyCommandFile);
            }
            if (checkNotifyList(command) && inGameNotifications) {
                Notifier.notifyPlayer(ChatColor.BLUE + "[" + ChatColor.RED + "CCLogger" + ChatColor.BLUE + "] " + ChatColor.GOLD + playerName + ": " + ChatColor.WHITE + command);
                if (logNotifyCommands) {
                    Notifier.notifyPlayer(ChatColor.BLUE + "[" + ChatColor.RED + "CCLogger" + ChatColor.BLUE + "] " + ChatColor.WHITE + "data has been logged to notifyCommands.log!");
                }
            }
        }
    }

    public String[] formatLog(String playerName, String command, int x, int y, int z, String worldName, String date, String ipAddress) {
        String format = plugin.getConfig().getString("Log.logFormat");
        String log = format;
        if (log.contains("%ip")) {
            log = log.replaceAll("%ip", ipAddress);
        }
        if (log.contains("%date")) {
            log = log.replaceAll("%date", date);
        }
        if (log.contains("%world")) {
            log = log.replaceAll("%world", worldName);
        }
        if (log.contains("%x")) {
            log = log.replaceAll("%x", Integer.toString(x));
        }
        if (log.contains("%y")) {
            log = log.replaceAll("%y", Integer.toString(y));
        }
        if (log.contains("%z")) {
            log = log.replaceAll("%z", Integer.toString(z));
        }
        if (log.contains("%name")) {
            log = log.replaceAll("%name", playerName);
        }
        if (log.contains("%content")) {
            log = log.replaceAll("%content", Matcher.quoteReplacement(command));
        }

        String[] logArray = {log};
        return logArray;
    }

    public boolean commandCheck(String command) {
        List<String> commands = plugin.getConfig().getStringList("Log.commands.blacklist");
        String[] commandsplit = command.split(" ");
        String commandconvert = commandsplit[0];
        for (int i = 0; i < commands.size(); i++) {
            if (commandconvert.matches(commands.get(i))) {
                return true;
            }
        }
        return false;
    }

    public boolean checkNotifyList(String command) {
        List<String> commands = plugin.getConfig().getStringList("Log.notifications.commands");
        String[] commandsplit = command.split(" ");
        String commandconvert = commandsplit[0];
        for (int i = 0; i < commands.size(); i++) {
            if (commandconvert.matches(commands.get(i))) {
                return true;
            }
        }
        return false;
    }

    public void checkPlayer(String name) throws IOException {
        File playersFolder = new File(plugin.getDataFolder(), "players");
        File file = new File(playersFolder, name + ".log");
        if (!file.exists()) {
            file.createNewFile();
        }


    }

    public String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public boolean checkExemptionList(Player player) {
        if (player.hasPermission("cclogger.exempt")) {
            return true;
        }
        return false;
    }
}
