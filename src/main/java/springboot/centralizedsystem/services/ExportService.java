package springboot.centralizedsystem.services;

public interface ExportService {

    String exportSubmissionDatasToString(String token, String path, String type);
}
