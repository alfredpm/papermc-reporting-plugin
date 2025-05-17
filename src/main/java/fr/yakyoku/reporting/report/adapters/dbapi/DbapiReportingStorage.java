package fr.yakyoku.reporting.report.adapters.dbapi;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.naruse.dbapi.api.DatabaseAPI;
import fr.naruse.dbapi.database.Database;
import fr.naruse.dbapi.sql.SQLHelper;
import fr.naruse.dbapi.sql.SQLRequest;
import fr.yakyoku.reporting.report.IReportingStorage;
import fr.yakyoku.reporting.report.models.ReportMotive;
import fr.yakyoku.reporting.report.models.ReportStatus;
import fr.yakyoku.reporting.report.models.StoredReport;

public class DbApiReportingStorage implements IReportingStorage {

    private final String TABLE_REPORT_NAME = "reports";
    private final Database reportDatabase;

    private String getName() {
        return "Report storage [dbapi]";
    }

    public DbApiReportingStorage() {
        DatabaseAPI.createNewDatabase(this.reportDatabase = new Database("Reports", TABLE_REPORT_NAME) {
            @Override
            public String getQuery() {
                return "CREATE TABLE IF NOT EXISTS " + TABLE_REPORT_NAME + " ("
                    +"id CHAR(36) PRIMARY KEY,"
                    +"source CHAR(36) NOT NULL,"
                    +"target CHAR(36) NOT NULL,"
                    +"solver CHAR(36),"
                    +"motive INT NOT NULL,"
                    +"status INT NOT NULL,"
                    +"postedAt INT NOT NULL,"
                    +"solvedAt INT,"
                    +"postComment VARCHAR(255),"
                    +"solveComment VARCHAR(255),"
                    +"FOREIGN KEY(source) REFERENCES users(id) ON DELETE RESTRICT,"
                    +"FOREIGN KEY(target) REFERENCES users(id) ON DELETE RESTRICT,"
                    +"FOREIGN KEY(solver) REFERENCES users(id) ON DELETE SET NULL"
                +");";
            }
        });
    }

    @Override
    public StoredReport getReport(UUID id) throws Exception { return this.getReport(id, false); }
    @Override
    public StoredReport getReport(UUID id, boolean secondThread) throws Exception {
        SQLRequest sqlRequest = new SQLRequest(SQLHelper.getSelectRequest(TABLE_REPORT_NAME, new String[]{"id", "source", "target", "solver", "motive", "status", "postedAt", "solvedAt", "postComment", "solveComment"}, "id"),
            id.toString()
        );
        /**
         * How am I supposed to extract a result from SQLRequests ?
         * I could just not return anything ever and just pass a "callback" function 
         * as parameter to be used in SQLResponse.handleResponse. 
         * That seems overengineered though.
         * 
         * When I need a return, I'll simply use the deprecated getResultSet(SQLRequest) for now.
         */

        // if (secondThread) {
        //     this.reportDatabase.prepareStatement(sqlRequest, response);      
        // } else {
        //     this.reportDatabase.prepareDirectStatement(sqlRequest, response);
        // }
        ResultSet result = this.reportDatabase.getResultSet(sqlRequest);
        if(result.next()) {
            StoredReport report = toObject(result);
            return report;
        } else {
            throw new Exception("Not found");
        }
    }

    @Override
    public List<StoredReport> getAllReports() throws Exception { return this.getAllReports(false); }
    @Override
    public List<StoredReport> getAllReports(boolean secondThread) throws Exception {
        SQLRequest sqlRequest = new SQLRequest(SQLHelper.getSelectRequest(TABLE_REPORT_NAME, new String[]{"id", "source", "target", "solver", "motive", "status", "postedAt", "solvedAt", "postComment", "solveComment"}));
        // if (secondThread) {
        //    this.reportDatabase.prepareStatement(sqlRequest, response);
        // } else {
        //     this.reportDatabase.prepareDirectStatement(sqlRequest, response);
        // }
        ResultSet results = this.reportDatabase.getResultSet(sqlRequest);
        List<StoredReport> reports = new ArrayList<>();
        while(results.next()) {
            reports.add(toObject(results));
        }
        return reports;
    }

    @Override
    public void addReport(StoredReport report) throws Exception { this.addReport(report,false); }
    @Override
    public void addReport(StoredReport report, boolean secondThread) throws Exception {
        SQLRequest sqlRequest = new SQLRequest(SQLHelper.getInsertRequest(TABLE_REPORT_NAME, new String[]{"id", "source", "target", "solver", "motive", "status", "postedAt", "solvedAt", "postComment", "solveComment"}),
                report.id.toString(),
                report.source != null ? report.source.toString() : null,
                report.target != null ? report.target.toString() : null,
                report.solver != null ? report.solver.toString() : null,
                report.motive.ordinal(),
                report.status.ordinal(),
                report.postedAt.getEpochSecond(),
                report.solvedAt != null ? report.solvedAt.getEpochSecond() : null,
                report.postComment,
                report.solveComment);
        if (secondThread) {
            this.reportDatabase.prepareStatement(sqlRequest);
        } else {
            this.reportDatabase.prepareDirectStatement(sqlRequest);
        }
    }

    @Override
    public void updateReport(StoredReport report) throws Exception { this.updateReport(report, false); }
    @Override
    public void updateReport(StoredReport report, boolean secondThread) throws Exception {
        SQLRequest sqlRequest = new SQLRequest(SQLHelper.getUpdateRequest(TABLE_REPORT_NAME, new String[]{"source", "target", "solver", "motive", "status", "postedAt", "solvedAt", "postComment", "solveComment"}, "id"),
                report.source != null ? report.source.toString() : null,
                report.target != null ? report.target.toString() : null,
                report.solver != null ? report.solver.toString() : null,
                report.motive.ordinal(),
                report.status.ordinal(),
                report.postedAt.getEpochSecond(),
                report.solvedAt != null ? report.solvedAt.getEpochSecond() : null,
                report.postComment,
                report.solveComment,
                report.id.toString());
        if (secondThread) {
            this.reportDatabase.prepareStatement(sqlRequest);
        } else {
            this.reportDatabase.prepareDirectStatement(sqlRequest);
        }
    }
    
    static private StoredReport toObject(ResultSet rs) {
        StoredReport report = new StoredReport(null, null, null, null, ReportMotive.CHEATING, ReportStatus.PENDING, null, null, null, null);
        try { report.id = UUID.fromString(rs.getString("id")); } catch (SQLException e) { }
        try { report.source = UUID.fromString(rs.getString("source")); } catch (SQLException e) { }
        try { report.target = UUID.fromString(rs.getString("target")); } catch (SQLException e) { }
        try { 
            String solverName = rs.getString("solver");
            report.solver = solverName != null ? UUID.fromString(solverName) : null; 
        } catch (SQLException e) { }
        try { report.motive = ReportMotive.values()[rs.getInt("motive")]; } catch (SQLException e) { }
        try { report.status = ReportStatus.values()[rs.getInt("status")]; } catch (SQLException e) { }
        try { report.postedAt = Instant.ofEpochSecond((long)rs.getInt("postedAt")); } catch (SQLException e) { }
        try { 
            int solvedSecond = rs.getInt("solvedAt");
            report.solvedAt = !rs.wasNull() ? Instant.ofEpochSecond((long)solvedSecond) : null; 
        } catch (SQLException e) { }
        try { report.postComment = rs.getString("postComment"); } catch (SQLException e) { }
        try { report.solveComment = rs.getString("solveComment"); } catch (SQLException e) { }
        return report;
    }
}
