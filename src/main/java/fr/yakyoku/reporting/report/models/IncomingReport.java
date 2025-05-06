package fr.yakyoku.reporting.report.models;

import java.time.Instant;
import java.util.UUID;

public class IncomingReport {
    UUID source;
    UUID target;
    Instant postedAt;
    ReportMotive motive;
    String comment;

    public IncomingReport(
        UUID source,
        UUID target,
        ReportMotive motive,
        String comment
    ) {
        this.source = source;
        this.target = target;
        this.postedAt = Instant.now();
        this.motive = motive;
        this.comment = comment;
    }
}