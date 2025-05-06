package fr.yakyoku.reporting.main.input.adapters;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import fr.yakyoku.reporting.main.ReportingPlugin;
import fr.yakyoku.reporting.main.input.IInputHandler;
import fr.yakyoku.reporting.report.models.IncomingReport;
import fr.yakyoku.reporting.report.models.ReportMotive;
import fr.yakyoku.reporting.report.models.ReportStatus;
import fr.yakyoku.reporting.report.models.StoredReport;
import fr.yakyoku.reporting.report.models.StoredReport.StoredReportPage;

public class CommandInputHandler implements IInputHandler, CommandExecutor {

    private ReportingPlugin controller = null;
    private DateFormat longDf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private DateFormat shortDf = new SimpleDateFormat("dd/MM/yyyy");

    private String getName() {
        return "Input [Commandline]";
    }

    @Override
    public void register(ReportingPlugin controller) {
        this.controller = controller;
        controller.getCommand("report").setExecutor(this);
        controller.getCommand("solve").setExecutor(this);
        controller.getCommand("read").setExecutor(this);
        controller.getCommand("readall").setExecutor(this);
        Logger.getLogger(getName()).warning("Command registered");
    };

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        switch (command.getName().toLowerCase()) {
            case "report":
                return handleReport(sender, args);
            case "solve":
                return handleSolve(sender, args);
            case "read":
                return handleRead(sender, args);
            case "readall":
                return handleReadAll(sender, args);
            default:
                return false;
        }
    }

    private boolean handleReport(CommandSender sender, String[] args) {     // /report <name> <motive?> <comment?>
        UUID sourceUUID = null;
        UUID targetUUID = null;
        ReportMotive motive = ReportMotive.OTHER;
        String comment = "";

        // Get source
        if (!(sender instanceof Player)) { sender.sendMessage("Reporting requires the sender to have an id."); return true; }
        Player senderPlayer = (Player) sender;
        sourceUUID = senderPlayer.getUniqueId();
        // Get target
        if (args.length > 0 && !args[0].isEmpty()) {
            Optional<UUID> result = controller.getRoleService().getIdByName(args[0]);
            if (result.isPresent()) {
                targetUUID = result.get();
            } else {
                sender.sendMessage("Target "+args[0]+" not found.");
                return true;
            }
        }
        // Get motive
        if (args.length > 1 && !args[1].isEmpty()) {
            try {
                motive = ReportMotive.valueOf(args[1].toUpperCase());
            } 
            catch (Exception e) {
                motive = ReportMotive.OTHER;
            }
        }
        // Get optional comment
        if (args.length > 2 && !args[2].isEmpty()) {
            comment = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        }

        // Treat request
        IncomingReport incomingReport = new IncomingReport(sourceUUID, targetUUID, motive, comment);
        controller.getReportingService().reportPlayer(incomingReport);
        return true;
    };
    private boolean handleSolve(CommandSender sender, String[] args) {      // /solve <reportId> <state> <comment?>
        UUID sourceUUID = null;
        UUID reportUUID = null;
        ReportStatus newState = null;
        String comment = "";

        // Get source
        if (!(sender instanceof Player)) return false;
        Player senderPlayer = (Player) sender;
        sourceUUID = senderPlayer.getUniqueId();
        // Get report
        if (args.length > 0 && !args[0].isEmpty()) {
            try {
                reportUUID = UUID.fromString(args[0]);
            } 
            catch (Exception e) {
                return false;
            }
        }
        // Get state
        if (args.length > 1 && !args[1].isEmpty()) {
            try {
                newState = ReportStatus.valueOf(args[1].toUpperCase());
            }
            catch (Exception e) {
                return false;
            }
        }
        // Get optional comment
        if (args.length > 2 && !args[2].isEmpty()) {
            comment = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        }

        // Treat request
        return controller.getReportingService().treatReport(sourceUUID, reportUUID, newState, comment);
    };

    private boolean handleReadAll(CommandSender sender, String[] args) {       // /readAll <page?>
        TextComponent response;

        // Get optional page arg
        int size = 10;
        int selectedPage = 1;
        if (args.length > 0 && !args[0].isEmpty()) {
            try {
                selectedPage = Integer.parseInt(args[0]);
            } 
            catch (Exception e) {
                return false;
            }
        }
        // Build report list
        StoredReportPage page = controller.getReportingService().getReportList(size, selectedPage);
        // Build response
        response = Component.text("ID - Motive - Subject - PostDate - Status");
        for (StoredReport report : page.reports()) {
            String targetName = controller.getRoleService().getNameById(report.target);
            response = response.append(
                Component.text(String.format("\n - %s", report.id.toString().substring(0,6)))
                    .color(NamedTextColor.AQUA)
                    .clickEvent(ClickEvent.runCommand(String.format("/read %s", report.id)))
                    .hoverEvent(Component.text("Show details"))
                    .append(Component.text(String.format(" | %s | %s | %s | %s", report.motive, targetName, shortDf.format(Date.from(report.postedAt)), report.status.toString())))
            );
        };
        response = response.append(Component.text(String.format("\n(%s/%s)", page.pageNb(), page.totalPages())));
        // Send response
        sender.sendMessage(response);
        return true;
    };

    private boolean handleRead(CommandSender sender, String[] args) {          // /read <reportId>
        UUID reportUUID = null;
        TextComponent response;

        // Get report id
        if (args.length > 0 && !args[0].isEmpty()) {
            try {
                reportUUID = UUID.fromString(args[0]);
            } 
            catch (Exception e) {
                return false;
            } 
        } else {
            return false;
        }

        // Get report details
        Optional<StoredReport> optReport = controller.getReportingService().getReport(reportUUID);
        if (!optReport.isPresent()) {response = Component.text(String.format("Cannot find report %s", reportUUID.toString())).color(NamedTextColor.RED);}
        else {
            // Get complementary info
            StoredReport report = optReport.get();
            String sourceName = controller.getRoleService().getNameById(report.source);
            String targetName = controller.getRoleService().getNameById(report.target);
            String solverName = report.solver != null ? controller.getRoleService().getNameById(report.solver) : null;
            // Build response
            response = Component
            .text(String.format(" === Report: %s / %s ===", report.id.toString().substring(0,6), report.status.toString()))
            .append(Component.text(String.format("\nSource: %s", sourceName != null ? sourceName : "N/A")))
            .append(Component.text(String.format("\nTarget: %s", targetName != null ? targetName : "N/A")))
            .append(Component.text(String.format("\nMotive: %s", report.motive.toString())))
            .append(Component.text(String.format("\nContext: %s", report.postComment != null ? report.postComment : "N/A")));
            if(report.status == ReportStatus.PENDING) {
                Component solve = Component.text(" Solve")
                    .color(NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.suggestCommand(String.format("/solve %s %s", report.id.toString(), ReportStatus.SOLVED.toString())))
                    .hoverEvent(Component.text("Signal the report was dealt with. /solve <id> SOLVED <Comment>"));
                Component dismiss = Component.text("Dismiss")
                    .color(NamedTextColor.RED)
                    .clickEvent(ClickEvent.suggestCommand(String.format("/solve %s %s\n", report.id.toString(), ReportStatus.DENIED.toString())))
                    .hoverEvent(Component.text("Signal the report was dismissed. /solve <id> DENIED <Comment>"));
                response = response
                    .append(Component.text("\n === "))
                    .append(solve)
                    .append(Component.text(" / "))
                    .append(dismiss)
                    .append(Component.text(" === "));
            } else {
                response = response
                    .append( Component.text(String.format("\nSolver: %s", solverName != null ? solverName : "N/A")))
                    .append(Component.text(String.format("\nContext: %s", report.solveComment != null ? report.solveComment : "N/A")));
            }
        }
        // Send response
        sender.sendMessage(response);
        return true;
    };
}
