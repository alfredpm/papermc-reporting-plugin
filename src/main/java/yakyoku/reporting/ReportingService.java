package yakyoku.reporting;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import yakyoku.reporting.models.IncomingReport;
import yakyoku.reporting.models.ReportStatus;
import yakyoku.reporting.models.StoredReport;
import yakyoku.reporting.models.StoredReport.StoredReportPage;

public class ReportingService {

    private String getName() {
        return "Reporting Service";
    }
    private IReportingStorage storage;
    
    public ReportingService(IReportingStorage storage) {
        this.storage = storage;
    }

    public Optional<StoredReport> getReport(UUID reportId) {
        try {
            return Optional.of(this.storage.getReport(reportId));
        }
        catch (Exception e) {
            Logger.getLogger(getName()).warning(e.getMessage());
            return Optional.empty();
        }
    }

    public StoredReportPage getReportList(int selectionSize, int pageNb) {
        if (selectionSize <= 0 || pageNb <= 0) return new StoredReportPage(List.of(), 0, 0);
        try {
            List<StoredReport> reports = this.storage.getAllReports();
            if (reports.size() <= 0) { return new StoredReportPage(List.of(), 0, 0); }
            reports.sort(
                (StoredReport reportA, StoredReport reportB) -> {
                    return reportA.postedAt.compareTo(reportB.postedAt);
                }
            );
            int actualPage = pageNb;
            int maxPage = Math.ceilDiv(reports.size(), selectionSize);
            int selectionStart = selectionSize*(pageNb-1);
            if (selectionStart < 0 || selectionStart > reports.size()-1) { selectionStart = 0; actualPage = 1; }
            int selectionEnd = selectionStart+selectionSize;
            if (selectionEnd > reports.size()) selectionEnd = reports.size();
            return new StoredReportPage(reports.subList(selectionStart, selectionEnd), actualPage, maxPage);
        }
        catch (Exception e) {
            Logger.getLogger(getName()).warning("Something happened: "+e.getMessage());
            return new StoredReportPage(List.of(), 0, 0);
        }
    }

    public void reportPlayer(IncomingReport inReport) {
        UUID reportId = UUID.randomUUID();
        StoredReport report = new StoredReport(reportId, inReport);
        try {
            storage.addReport(report);
        }
        catch (Exception e) {
            Logger.getLogger(getName()).severe(e.getMessage());
        }
    }

    public boolean treatReport(UUID sourceUUID, UUID reportUUID, ReportStatus newState, String comment) {
        Optional<StoredReport> result = getReport(reportUUID);
        if (result.isPresent()) {
            StoredReport updateReport = result.get();
            if (updateReport.status != ReportStatus.PENDING) { return false; } 
            updateReport.solver = sourceUUID;
            updateReport.solvedAt = Instant.now();
            updateReport.status = newState;
            updateReport.solveComment = comment;
            try {
                storage.updateReport(updateReport);
                return true;
            }
            catch (Exception e) {
                Logger.getLogger(getName()).severe(e.getMessage());
                return false;
            }
        } 
        else {
            Logger.getLogger(getName()).severe("Failed to treat report, report does not exist.");
            return false;
        }
        
    }
}