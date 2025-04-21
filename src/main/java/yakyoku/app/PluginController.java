package yakyoku.app;

import net.kyori.adventure.text.Component;
import yakyoku.app.input.IInputHandler;
import yakyoku.app.input.adapters.CommandInputHandler;
import yakyoku.reporting.ReportingService;
import yakyoku.reporting.adapters.InMemoryReportingStorage;
import yakyoku.role.RoleService;
import yakyoku.role.adapters.InMemoryRoleStorage;
import yakyoku.role.models.Role;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginController extends JavaPlugin implements Listener {

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
        InMemoryReportingStorage reportStorage = new InMemoryReportingStorage();
        InMemoryRoleStorage roleStorage = new InMemoryRoleStorage();
        reportingService = new ReportingService(reportStorage);
        roleService = new RoleService(roleStorage);
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        Role knownRole = roleService.getOrCreatePlayer(player.getUniqueId(), player.getName(), Role.PLAYER);
        Bukkit.getScheduler().runTask(this, () -> {
            setPlayerPerms(player, knownRole);
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(Component.text("Hello, " + player.getName() + "!"));
    }

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