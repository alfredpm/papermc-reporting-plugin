package fr.yakyoku.reporting.report.models;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class StoredReport {
    public UUID id;
    public UUID source;
    public UUID target;
    public UUID solver;
    public ReportMotive motive;
    public ReportStatus status;
    public Instant postedAt;
    public Instant solvedAt;
    public String postComment;
    public String solveComment;

    public StoredReport(
        UUID id,
        UUID source,
        UUID target,
        UUID solver,
        ReportMotive motive,
        ReportStatus status,
        Instant postedAt,
        Instant solvedAt,
        String postComment,
        String solveComment
    ) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.solver = solver;
        this.motive = motive;
        this.status = status;
        this.postedAt = postedAt;
        this.solvedAt = solvedAt;
        this.postComment = postComment;
        this.solveComment = solveComment;
    }
    public StoredReport(
        IncomingReport report
    ) {
        this.id = UUID.randomUUID();
        this.source = report.source;
        this.target = report.target;
        this.solver = null;
        this.motive = report.motive;
        this.status = ReportStatus.PENDING;
        this.postedAt = report.postedAt;
        this.solvedAt = null;
        this.postComment = report.comment;
        this.solveComment = null;
    }
    public StoredReport(
        UUID id,
        IncomingReport report
    ) {
        this.id = id;
        this.source = report.source;
        this.target = report.target;
        this.solver = null;
        this.motive = report.motive;
        this.status = ReportStatus.PENDING;
        this.postedAt = report.postedAt;
        this.solvedAt = null;
        this.postComment = report.comment;
        this.solveComment = null;
    }
    public record ImmutableStoredReport(        
        UUID id,
        UUID source,
        UUID target,
        UUID solver,
        ReportMotive motive,
        ReportStatus status,
        Instant postedAt,
        Instant solvedAt,
        String postComment,
        String solveComment
    ) {}
    public record StoredReportPage(List<StoredReport> reports, int pageNb, int totalPages) {}

    public StoredReport copy() {
        return new StoredReport(id, source, target, solver, motive, status, postedAt, solvedAt, postComment, solveComment);
    }
    public void copyContent(StoredReport inReport) {
        this.id = inReport.id;
        this.source = inReport.source;
        this.target = inReport.target;
        this.solver = inReport.solver;
        this.motive = inReport.motive;
        this.status = inReport.status;
        this.postedAt = inReport.postedAt;
        this.solvedAt = inReport.solvedAt;
        this.postComment = inReport.postComment;
        this.solveComment = inReport.solveComment;
    }
    public ImmutableStoredReport toImmutable(StoredReport report) {
        return new ImmutableStoredReport(report.id, report.source, report.target, report.solver, report.motive, report.status, report.postedAt, report.solvedAt, report.postComment, report.solveComment);
    }
}