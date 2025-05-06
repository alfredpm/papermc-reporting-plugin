package fr.yakyoku.reporting.report.adapters.memory;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.logging.Logger;

import fr.yakyoku.reporting.report.IReportingStorage;
import fr.yakyoku.reporting.report.models.StoredReport;

public class InMemoryReportingStorage implements IReportingStorage {
    Map<UUID, StoredReport> storage = new HashMap<>();

    private String getName() {
        return "Reporting Storage [In Memory]";
    }

    @Override
    public StoredReport getReport(UUID id) throws Exception {
        try {
            return storage.get(id).copy();
        }
        catch (Exception e){
            throw new NoSuchElementException("No report found with id :"+id);
        }
    }    
    
    @Override
    public List<StoredReport> getAllReports() throws Exception {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void addReport(StoredReport report) throws Exception {
        try {
            storage.put(report.id, report);
        }
        catch (Exception e){
            throw new InvalidObjectException("New report cannot be added with these parameters.");
        }
    }

    @Override
    public void updateReport(StoredReport inReport) throws Exception {
        try {
            StoredReport report = storage.get(inReport.id);
            report.copyContent(inReport);
        }
        catch (Exception e){
            throw new NoSuchElementException("No report found with id :"+inReport.id);
        }
    }

    public void logStorage() {
        String message = "Memory :";
        Logger.getLogger(getName()).info(message);;
        storage.entrySet().stream().forEach((entry) -> {
            Logger.getLogger(getName()).info(String.format("%s - %s", entry.getKey().toString().substring(0, 6), entry.getValue().source.toString().substring(0, 6)));
        });
    }
}
