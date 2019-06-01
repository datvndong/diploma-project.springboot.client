package springboot.centralizedsystem.controllers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import springboot.centralizedsystem.domains.ImportFile;
import springboot.centralizedsystem.domains.User;
import springboot.centralizedsystem.resources.Collections;
import springboot.centralizedsystem.resources.Keys;
import springboot.centralizedsystem.resources.RequestsPath;
import springboot.centralizedsystem.services.ReadSurveyService;
import springboot.centralizedsystem.utils.SessionUtils;

@Controller
public class ReadSurveyController extends BaseController {

    @Autowired
    private ReadSurveyService readSurveyService;

    @Autowired
    MongoTemplate mongoTemplate;

    @PostMapping(RequestsPath.READ_SURVEY)
    public String readSurveyPOST(@Valid ImportFile importFile, HttpServletRequest request, HttpSession session,
            RedirectAttributes redirect) {
        redirect.addAttribute("page", 1);
        try {
            User user = SessionUtils.getUser(session);
            if (user == null) {
                return unauthorized(redirect);
            }

            String uploadRootPath = request.getServletContext().getRealPath("upload");

            File uploadRootDir = new File(uploadRootPath);
            // Create root upload folder if not existed.
            if (!uploadRootDir.exists()) {
                uploadRootDir.mkdirs();
            }
            MultipartFile[] fileDatas = importFile.getFileDatas();

            // Upload file to server & return path
            String path = readSurveyService.getPathFileImport(uploadRootDir, fileDatas);

            // Dump data by blue print JSON & insert to DB
            List<String> list = readSurveyService.getListDataFromFile(path, user.getEmail());
            DBObject dbObject = null;
            DBCollection collection = mongoTemplate.getCollection(Collections.SURVEYS);
            for (String string : list) {
                dbObject = (DBObject) JSON.parse(string);
                collection.insert(dbObject);
            }

            redirect.addFlashAttribute(Keys.IMPORT, true);
            return "redirect:" + RequestsPath.FORMS;
        } catch (IOException e) {
            redirect.addFlashAttribute(Keys.IMPORT, false);
            return "redirect:" + RequestsPath.FORMS;
        }
    }
}
