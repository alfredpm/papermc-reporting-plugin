package fr.yakyoku.reporting.main;

import net.kyori.adventure.text.Component;
import fr.yakyoku.reporting.main.input.IInputHandler;
import fr.yakyoku.reporting.main.input.adapters.CommandInputHandler;
import fr.yakyoku.reporting.report.ReportingService;
import fr.yakyoku.reporting.report.adapters.memory.InMemoryReportingStorage;
import fr.yakyoku.reporting.report.adapters.sql.SqlReportingStorage;
import fr.yakyoku.reporting.role.RoleService;
import fr.yakyoku.reporting.role.adapters.memory.InMemoryRoleStorage;
import fr.yakyoku.reporting.role.adapters.sql.SqlRoleStorage;
import fr.yakyoku.reporting.role.models.Role;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

public class ReportingPlugin extends JavaPlugin implements Listener {

    static IInputHandler inputHandler;
    static RoleService roleService;
    static ReportingService reportingService;

    public RoleService getRoleService() { return roleService; }
    public ReportingService getReportingService() { return reportingService; }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        // Input methods setup
        inputHandler = new CommandInputHandler();
        inputHandler.register(this);
        PermissionAttachment consoleAttachment = getServer().getConsoleSender().addAttachment(this);
        consoleAttachment.setPermission("yakyoku.reporting.report", true);
        consoleAttachment.setPermission("yakyoku.reporting.solve", true);
        consoleAttachment.setPermission("yakyoku.reporting.read", true);
        // Services setup
        //      In memory
        // InMemoryReportingStorage reportStorage = new InMemoryReportingStorage();
        // InMemoryRoleStorage roleStorage = new InMemoryRoleStorage();
        //      In SQL 
        //          (sqlite)
        Connection connection = InitSqlConnection("jdbc:sqlite:demodb");
        //          (mysql)
        // Connection connection = InitSqlConnection("jdbc:mysql://localhost:3306/iyc", "root", "root");
        SqlRoleStorage roleStorage = new SqlRoleStorage(connection);
        SqlReportingStorage reportStorage = new SqlReportingStorage(connection);
        //      Common
        reportingService = new ReportingService(reportStorage);
        roleService = new RoleService(roleStorage);
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        player.getScheduler().run(this, task -> {
            Role knownRole = roleService.getOrCreatePlayer(player.getUniqueId(), player.getName(), Role.PLAYER);
            Logger.getLogger(getName()).warning("set"+knownRole);
            setPlayerPerms(player, knownRole);
        }, () -> {
            Logger.getLogger(getName()).warning("test");
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(Component.text("Hello, " + player.getName() + "!"));
    }

    private Connection InitSqlConnection(String url) {
        return this.InitSqlConnection(url, null, null);
    }
    private Connection InitSqlConnection(String url, String name, String password)  {
        try {
            var conn = name != null && password != null ? DriverManager.getConnection(url, name, password) : DriverManager.getConnection(url);
            if (conn != null && !conn.isClosed()) {
                var meta = conn.getMetaData();
                Logger.getLogger(getName()).info("The driver name is " + meta.getDriverName());
                Logger.getLogger(getName()).info("A new database has been created.");
                return conn;
            }
        } catch (SQLException e) {
            Logger.getLogger(getName()).severe(String.format("Failure creating connection to %s", url));
            return null;
        }
        return null;
    }

    // TODO : Should probably be separated in a class for clarity
    private final Map<UUID, PermissionAttachment> attachments = new HashMap<>();
    public void setPlayerPerms(Player player, Role role) {
        PermissionAttachment attachment = attachments.computeIfAbsent(player.getUniqueId(), uuid ->
            player.addAttachment(this)
        );
        attachment.getPermissions().keySet().forEach(perm -> attachment.unsetPermission(perm));
        switch (role) {
            case MODERATOR:
                attachment.setPermission("yakyoku.reporting.report", true);
                attachment.setPermission("yakyoku.reporting.solve", true);
                attachment.setPermission("yakyoku.reporting.read", true);
                player.sendMessage("Configured as MODERATOR");
                break;
            case PLAYER:
                attachment.setPermission("yakyoku.reporting.report", true);
                player.sendMessage("Configured as PLAYER");
                break;
            default:
                break;
        }
    }



}