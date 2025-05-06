package fr.yakyoku.reporting.report.adapters.dbapi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.naruse.dbapi.api.DatabaseAPI;
import fr.naruse.dbapi.database.Database;
import fr.naruse.dbapi.main.DBAPICore;
import fr.yakyoku.reporting.report.IReportingStorage;
import fr.yakyoku.reporting.report.models.ReportMotive;
import fr.yakyoku.reporting.report.models.ReportStatus;
import fr.yakyoku.reporting.report.models.StoredReport;

/// TODO
/// Dbapi is a wrapper around an sql db.
/// Replace connection with dbapi appropriate endpoint, may need to revise all sql 

public class DbapiReportingStorage implements IReportingStorage{
    Connection conn;

    public DbapiReportingStorage(Connection connection) {
        conn = connection;
        this.initializeDatabase();
    }
    private boolean initializeDatabase() {
        try {
            PreparedStatement createReports = conn.prepareStatement("CREATE TABLE IF NOT EXISTS reports ("
                +"id CHAR(36) PRIMARY KEY,"
                +"source CHAR(36) NOT NULL,"
                +"target CHAR(36) NOT NULL,"
                +"solver CHAR(36),"
                +"status INT NOT NULL,"
                +"postedAt INT NOT NULL,"
                +"solvedAt INT,"
                +"postComment VARCHAR(255),"
                +"solveComment VARCHAR(255),"
                +"FOREIGN KEY(source) REFERENCES user(id) ON DELETE RESTRICT,"
                +"FOREIGN KEY(target) REFERENCES user(id) ON DELETE RESTRICT,"
                +"FOREIGN KEY(solver) REFERENCES user(id) ON DELETE SET NULL"
            +");");
            createReports.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public StoredReport getReport(UUID id) throws Exception {
        PreparedStatement getReport = conn.prepareStatement("SELECT * FROM reports WHERE "
            +"id = ?"
        +";");
        getReport.setString(1, id.toString());
        ResultSet results = getReport.executeQuery();
        StoredReport report = toObject(results);
        return report;
    }

    @Override
    public List<StoredReport> getAllReports() throws Exception {
        PreparedStatement getReports = conn.prepareStatement("SELECT * FROM reports;");
        ResultSet results = getReports.executeQuery();
        List<StoredReport> reports = new ArrayList<>();
        while(results.next()) {
            reports.add(toObject(results));
        }
        return reports;
    }

    @Override
    public void addReport(StoredReport report) throws Exception {
        PreparedStatement addReport = conn.prepareStatement("INSERT INTO reports VALUES ?;");
        addReport.setString(1, null);
        addReport.executeUpdate();
    }

    @Override
    public void updateReport(StoredReport report) throws Exception {
        PreparedStatement updateReport = conn.prepareStatement("UPDATE reports "
        +"SET ? " 
	    +"WHERE " 
		    +"id = ?"
        +";");
        updateReport.setObject(1, report);
        updateReport.setString(2, report.id.toString());
        updateReport.executeUpdate();
    }



    static private StoredReport toObject(ResultSet rs) {
        StoredReport report = new StoredReport(null, null, null, null, ReportMotive.CHEATING, ReportStatus.PENDING, null, null, null, null);
        try { report.id = UUID.fromString(rs.getString("id")); } catch (SQLException e) { }
        try { report.source = UUID.fromString(rs.getString("source")); } catch (SQLException e) { }
        try { report.target = UUID.fromString(rs.getString("target")); } catch (SQLException e) { }
        try { report.solver = UUID.fromString(rs.getString("solver")); } catch (SQLException e) { }
        try { report.motive = ReportMotive.values()[rs.getInt("motive")]; } catch (SQLException e) { }
        try { report.status = ReportStatus.values()[rs.getInt("status")]; } catch (SQLException e) { }
        try { report.postedAt = Instant.ofEpochSecond((long)rs.getInt("postedAt")); } catch (SQLException e) { }
        try { report.solvedAt = Instant.ofEpochSecond((long)rs.getInt("solvedAt")); } catch (SQLException e) { }
        try { report.postComment = rs.getString("postComment"); } catch (SQLException e) { }
        try { report.solveComment = rs.getString("solveComment"); } catch (SQLException e) { }
        return report;
    }

}
