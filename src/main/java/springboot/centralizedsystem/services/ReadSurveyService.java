package springboot.centralizedsystem.services;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface ReadSurveyService {

    List<String> getListDataFromFile(String pathFile) throws IOException;

    String getPathFileImport(File uploadRootDir, MultipartFile[] fileDatas) throws IOException;
}
