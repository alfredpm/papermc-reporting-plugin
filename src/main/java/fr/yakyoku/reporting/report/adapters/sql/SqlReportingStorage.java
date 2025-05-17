package fr.yakyoku.reporting.report.adapters.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.commons.lang.NotImplementedException;

import fr.yakyoku.reporting.report.IReportingStorage;
import fr.yakyoku.reporting.report.models.ReportMotive;
import fr.yakyoku.reporting.report.models.ReportStatus;
import fr.yakyoku.reporting.report.models.StoredReport;

public class SqlReportingStorage implements IReportingStorage{
    Connection conn;

    private String getName() {
        return "Report storage [sql]";
    }

    public SqlReportingStorage(Connection connection) {
        conn = connection;
        this.initializeDatabase();
    }
    private boolean initializeDatabase() {
        Logger.getLogger(getName()).warning("Initializing db");
        try {
            PreparedStatement createReports = conn.prepareStatement("CREATE TABLE IF NOT EXISTS reports ("
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
            +");");
            createReports.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public StoredReport getReport(UUID id) throws Exception { return this.getReport(id, false); }
    @Override
    public StoredReport getReport(UUID id, boolean secondThread) throws Exception {
        if(secondThread) {
            throw new NotImplementedException(getName()+" does not support multithread for getReport.");
        }
        Logger.getLogger(getName()).warning("Getting report by id");
        PreparedStatement getReport = conn.prepareStatement("SELECT * FROM reports WHERE "
            +"id = ?"
        +";");
        getReport.setString(1, id.toString());
        ResultSet result = getReport.executeQuery();
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
        if(secondThread) {
            throw new NotImplementedException(getName()+" does not support multithread for getAllReport.");
        }
        Logger.getLogger(getName()).warning("Getting all reports");
        PreparedStatement getReports = conn.prepareStatement("SELECT * FROM reports;");
        ResultSet results = getReports.executeQuery();
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
        if(secondThread) {
            throw new NotImplementedException(getName()+" does not support multithread for addReport.");
        }
        Logger.getLogger(getName()).warning("Adding report");
        PreparedStatement addReport = conn.prepareStatement("INSERT INTO reports VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
        addReport.setString(1, report.id.toString());
        addReport.setString(2, report.source.toString());
        addReport.setString(3, report.target.toString());
        setNullableString(addReport,4, report.solver != null ? report.solver.toString() : null);
        addReport.setInt(5, report.motive.ordinal());
        addReport.setInt(6, report.status.ordinal());
        addReport.setLong(7, report.postedAt.getEpochSecond());
        setNullableLong(addReport,8,  report.solvedAt != null ? report.solvedAt.getEpochSecond() : null);
        setNullableString(addReport,9, report.postComment);
        setNullableString(addReport,10, report.solveComment);
        addReport.executeUpdate();
    }

    @Override
    public void updateReport(StoredReport report) throws Exception { this.updateReport(report, false); }
    @Override
    public void updateReport(StoredReport report, boolean secondThread) throws Exception {
        if(secondThread) {
            throw new NotImplementedException(getName()+" does not support multithread for updateReport.");
        }
        Logger.getLogger(getName()).warning("Updating report");
        PreparedStatement updateReport = conn.prepareStatement("UPDATE reports "
        +"SET source=?, target=?, solver=?, motive=?, status=?, postedAt=?, solvedAt=?, postComment=?, solveComment=? " 
	    +"WHERE " 
		    +"id = ?"
        +";");
        updateReport.setString(1, report.source.toString());
        updateReport.setString(2, report.target.toString());
        setNullableString(updateReport,3, report.solver != null ? report.solver.toString() : null);
        updateReport.setInt(4, report.motive.ordinal());
        updateReport.setInt(5, report.status.ordinal());
        updateReport.setLong(6, report.postedAt.getEpochSecond());
        setNullableLong(updateReport,7,  report.solvedAt != null ? report.solvedAt.getEpochSecond() : null);
        setNullableString(updateReport,8, report.postComment);
        setNullableString(updateReport,9, report.solveComment);
        updateReport.setString(10, report.id.toString());
        updateReport.executeUpdate();
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


    private static void setNullableInt(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value != null) stmt.setInt(index, value);
        else stmt.setNull(index, Types.INTEGER);
    }

    private static void setNullableLong(PreparedStatement stmt, int index, Long value) throws SQLException {
        if (value != null) stmt.setLong(index, value);
        else stmt.setNull(index, Types.BIGINT);
    }

    private static void setNullableString(PreparedStatement stmt, int index, String value) throws SQLException {
        if (value != null) stmt.setString(index, value);
        else stmt.setNull(index, Types.VARCHAR);
    }

}
