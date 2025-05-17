package fr.yakyoku.reporting.report;

import java.util.List;
import java.util.UUID;

import fr.yakyoku.reporting.report.models.StoredReport;

/**
 * TODO : Refactor all storage logic in its own module java.yakyoku.persist itself split into the various storage methods
 *  - Singular module with access to all data, may be used to handle complex requests crossing multiple domains (e.g. reports by user) would need to be done in app module otherwise
 *  - Would allow new services to access any relevant data without duplicating anything (obviously useful for something like user*role storage)
 *  - Probably easier to ensure cohesive database
 */

public interface IReportingStorage {    
    public StoredReport getReport(UUID id) throws Exception;            
    public StoredReport getReport(UUID id, boolean secondThread) throws Exception;
    public List<StoredReport> getAllReports() throws Exception;
    public List<StoredReport> getAllReports(boolean secondThread) throws Exception;
    public void addReport(StoredReport report) throws Exception;
    public void addReport(StoredReport report, boolean secondThread) throws Exception;
    public void updateReport(StoredReport report) throws Exception;
    public void updateReport(StoredReport report, boolean secondThread) throws Exception;
}