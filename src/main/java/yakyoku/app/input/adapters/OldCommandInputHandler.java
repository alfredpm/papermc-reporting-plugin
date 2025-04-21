// package yakyoku.app.input.adapters;

// import java.lang.reflect.Field;
// import java.text.DateFormat;
// import java.text.SimpleDateFormat;
// import java.util.Arrays;
// import java.util.Date;
// import java.util.Optional;
// import java.util.UUID;
// import java.util.logging.Logger;

// import org.bukkit.Bukkit;
// import org.bukkit.command.Command;
// import org.bukkit.command.CommandMap;
// import org.bukkit.command.CommandSender;
// import org.bukkit.entity.Player;

// import net.kyori.adventure.text.Component;
// import net.kyori.adventure.text.TextComponent;
// import net.kyori.adventure.text.event.ClickEvent;
// import net.kyori.adventure.text.format.NamedTextColor;
// import yakyoku.app.input.IInputHandler;
// import yakyoku.app.PluginController;
// import yakyoku.reporting.models.IncomingReport;
// import yakyoku.reporting.models.ReportMotive;
// import yakyoku.reporting.models.ReportStatus;
// import yakyoku.reporting.models.StoredReport;
// import yakyoku.reporting.models.StoredReport.StoredReportPage;

// public class OldCommandInputHandler implements IInputHandler {

//     private PluginController controller = null;
//     private DateFormat longDf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//     private DateFormat shortDf = new SimpleDateFormat("dd/MM/yyyy");

//     private String getName() {
//         return "Input [Commandline]";
//     }

//     @Override
//     public void register(PluginController controller) {
//         this.controller = controller;

//         try {
//             Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
//             commandMapField.setAccessible(true);
//             CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

//             reportUserCmd.setPermission("yakyoku.reporting.report");
//             readReportsCmd.setPermission("yakyoku.reporting.read");
//             readReportDetailsCmd.setPermission("yakyoku.reporting.read");
//             solveReportCmd.setPermission("yakyoku.reporting.solve");

//             commandMap.register(this.controller.getName(), reportUserCmd);
//             commandMap.register(this.controller.getName(), readReportsCmd);
//             commandMap.register(this.controller.getName(), readReportDetailsCmd);
//             commandMap.register(this.controller.getName(), solveReportCmd);
//             Logger.getLogger(getName()).warning("Command registered");
//             commandMap.getKnownCommands().entrySet().forEach(entry -> {
//                 Logger.getLogger(getName()).warning(String.format("%s / %s", entry.getKey(), entry.getValue()));
//             });
//         }
//         catch (Exception e) {
//             Logger.getLogger(getName()).warning(e.getMessage());
//         }
//     };

//     private Command reportUserCmd = new Command("report") {     // /report <name> <motive?> <comment?>
//         @Override
//         public boolean execute(CommandSender sender, String commandLabel, String[] args) {
//             UUID sourceUUID = null;
//             UUID targetUUID = null;
//             ReportMotive motive = ReportMotive.OTHER;
//             String comment = "";

//             // Get source
//             if (!(sender instanceof Player)) return false;
//             Player senderPlayer = (Player) sender;
//             sourceUUID = senderPlayer.getUniqueId();
//             // Get target
//             if (args.length > 0 && !args[0].isEmpty()) {
//                 Optional<UUID> result = controller.getRoleService().getIdByName(args[0]);
//                 if (result.isPresent()) {
//                     targetUUID = result.get();
//                 } else {
//                     sender.sendMessage("Target "+args[0]+" not found.");
//                     return false;
//                 }
//             }
//             // Get motive
//             if (args.length > 1 && !args[1].isEmpty()) {
//                 try {
//                     motive = ReportMotive.valueOf(args[1].toUpperCase());
//                 } 
//                 catch (Exception e) {
//                     motive = ReportMotive.OTHER;
//                 }
//             }
//             // Get optional comment
//             if (args.length > 2 && !args[2].isEmpty()) {
//                 comment = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
//             }

//             // Treat request
//             IncomingReport incomingReport = new IncomingReport(sourceUUID, targetUUID, motive, comment);
//             controller.getReportingService().reportPlayer(incomingReport);
//             return true;
//         }
//     };
//     private Command solveReportCmd = new Command("solve") {       // /solve <reportId> <state> <comment?>
//         @Override
//         public boolean execute(CommandSender sender, String commandLabel, String[] args) {
//             UUID sourceUUID = null;
//             UUID reportUUID = null;
//             ReportStatus newState = null;
//             String comment = "";

//             // Get source
//             if (!(sender instanceof Player)) return false;
//             Player senderPlayer = (Player) sender;
//             sourceUUID = senderPlayer.getUniqueId();
//             // Get report
//             if (args.length > 0 && !args[0].isEmpty()) {
//                 try {
//                     reportUUID = UUID.fromString(args[0]);
//                 } 
//                 catch (Exception e) {
//                     return false;
//                 }
//             }
//             // Get state
//             if (args.length > 1 && !args[1].isEmpty()) {
//                 try {
//                     newState = ReportStatus.valueOf(args[1].toUpperCase());
//                 }
//                 catch (Exception e) {
//                     return false;
//                 }
//             }
//             // Get optional comment
//             if (args.length > 2 && !args[2].isEmpty()) {
//                 comment = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
//             }

//             // Treat request
//             return controller.getReportingService().treatReport(sourceUUID, reportUUID, newState, comment);
//         }
//     };

//     private Command readReportsCmd = new Command("readAll") {    // /readAll <page?>
//         @Override
//         public boolean execute(CommandSender sender, String commandLabel, String[] args) {
//             TextComponent response;

//             // Get optional page arg
//             int size = 10;
//             int selectedPage = 1;
//             if (args.length > 0 && !args[0].isEmpty()) {
//                 try {
//                     selectedPage = Integer.parseInt(args[0]);
//                 } 
//                 catch (Exception e) {
//                     return false;
//                 }
//             }
//             // Build report list
//             StoredReportPage page = controller.getReportingService().getReportList(size, selectedPage);
//             // Build response
//             response = Component.text("ID - Motive - Subject - PostDate - Status");
//             for (StoredReport report : page.reports()) {
//                 String targetName = controller.getRoleService().getNameById(report.target);
//                 response = response.append(
//                     Component.text(String.format("\n - %s", report.id.toString().substring(0,6)))
//                         .color(NamedTextColor.AQUA)
//                         .clickEvent(ClickEvent.runCommand(String.format("/read %s", report.id)))
//                         .hoverEvent(Component.text("Show details"))
//                         .append(Component.text(String.format(" | %s | %s | %s | %s", report.motive, targetName, shortDf.format(Date.from(report.postedAt)), report.status.toString())))
//                 );
//             };
//             response = response.append(Component.text(String.format("\n(%s/%s)", page.pageNb(), page.totalPages())));
//             // Send response
//             sender.sendMessage(response);
//             return true;
//         }
//     };

//     private Command readReportDetailsCmd = new Command("read") {       // /read <reportId>
//         @Override
//         public boolean execute(CommandSender sender, String commandLabel, String[] args) {
//             UUID reportUUID = null;
//             TextComponent response;

//             // Get report id
//             if (args.length > 0 && !args[0].isEmpty()) {
//                 try {
//                     reportUUID = UUID.fromString(args[0]);
//                 } 
//                 catch (Exception e) {
//                     sender.sendMessage("Please, specify a report id.");
//                     return false;
//                 } 
//             } else {
//                 sender.sendMessage("Please, specify a report id.");
//                 return false;
//             }

//             // Get report details
//             Optional<StoredReport> optReport = controller.getReportingService().getReport(reportUUID);
//             if (!optReport.isPresent()) {response = Component.text(String.format("Cannot find report %s", reportUUID.toString())).color(NamedTextColor.RED);}
//             else {
//                 // Get complementary info
//                 StoredReport report = optReport.get();
//                 String sourceName = controller.getRoleService().getNameById(report.source);
//                 String targetName = controller.getRoleService().getNameById(report.target);
//                 String solverName = controller.getRoleService().getNameById(report.solver);
//                 // Build response
//                 response = Component
//                 .text(String.format(" === Report: %s / %s ===", report.id.toString().substring(0,6), report.status.toString()))
//                 .append(Component.text(String.format("\nSource: %s", sourceName != null ? sourceName : "N/A")))
//                 .append(Component.text(String.format("\nTarget: %s", targetName != null ? targetName : "N/A")))
//                 .append(Component.text(String.format("\nMotive: %s", report.motive.toString())))
//                 .append(Component.text(String.format("\nContext: %s", report.postComment != null ? report.postComment : "N/A")));
//                 if(report.status == ReportStatus.PENDING) {
//                     Component solve = Component.text(" Solve")
//                         .color(NamedTextColor.GREEN)
//                         .clickEvent(ClickEvent.suggestCommand(String.format("/solve %s %s", report.id.toString(), ReportStatus.SOLVED.toString())))
//                         .hoverEvent(Component.text("Signal the report was dealt with. /solve <id> SOLVED <Comment>"));
//                     Component dismiss = Component.text("Dismiss")
//                         .color(NamedTextColor.RED)
//                         .clickEvent(ClickEvent.suggestCommand(String.format("/solve %s %s\n", report.id.toString(), ReportStatus.DENIED.toString())))
//                         .hoverEvent(Component.text("Signal the report was dismissed. /solve <id> DENIED <Comment>"));
//                     response = response
//                         .append(Component.text("\n === "))
//                         .append(solve)
//                         .append(Component.text(" / "))
//                         .append(dismiss)
//                         .append(Component.text(" === "));
//                 } else {
//                     response = response
//                         .append( Component.text(String.format("\nSolver: %s", solverName != null ? solverName : "N/A")))
//                         .append(Component.text(String.format("\nContext: %s", report.solveComment != null ? report.solveComment : "N/A")));
//                 }
//             }
//             // Send response
//             sender.sendMessage(response);
//             return true;
//         }
//     };




// }
