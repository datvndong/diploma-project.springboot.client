package springboot.centralizedsystem.controllers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import springboot.centralizedsystem.domains.User;
import springboot.centralizedsystem.resources.Configs;
import springboot.centralizedsystem.resources.RequestsPath;
import springboot.centralizedsystem.services.SubmissionService;
import springboot.centralizedsystem.utils.MediaTypeUtils;
import springboot.centralizedsystem.utils.SessionUtils;

@Controller
public class ExportController extends BaseController {

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private SubmissionService submissionService;

    // http://localhost:8080/export/json/{path}?fileName=abc.json
    // Using ResponseEntity<InputStreamResource>
    @GetMapping(RequestsPath.EXPORT_JSON)
    public ResponseEntity<InputStreamResource> exportJSONGET(Model model, HttpSession session,
            @PathVariable String path, @RequestParam(defaultValue = Configs.DEFAULT_FILE_NAME) String fileName)
            throws IOException {
        User user = SessionUtils.getUser(session);
        String token = user.getToken();

        MediaType mediaType = MediaTypeUtils.getMediaTypeForFileName(this.servletContext, fileName);

        // Write JSON file
        ResponseEntity<String> submissionRes = submissionService.findAllSubmissions(token, path);
        JSONArray jsonArray = new JSONArray(submissionRes.getBody());
        StringBuilder str = new StringBuilder();
        for (Object object : jsonArray) {
            str.append(object.toString());
            str.append(System.getProperty("line.separator"));
        }

        // Add to InputStreamResource
        InputStream is = new ByteArrayInputStream(str.toString().getBytes("UTF-8"));
        InputStreamResource resource = new InputStreamResource(is);

        return ResponseEntity.ok()
                // Content-Disposition
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName)
                // Content-Type
                .contentType(mediaType)
                .body(resource);
    }
}
