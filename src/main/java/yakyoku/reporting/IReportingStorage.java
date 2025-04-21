package yakyoku.reporting;

import java.util.List;
import java.util.UUID;

import yakyoku.reporting.models.StoredReport;

public interface IReportingStorage {
    public StoredReport getReport(UUID id) throws Exception;
    public List<StoredReport> getAllReports() throws Exception;
    public void addReport(StoredReport report) throws Exception;
    public void updateReport(StoredReport report) throws Exception;
}