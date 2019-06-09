package springboot.centralizedsystem.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.UnknownHttpStatusCodeException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import springboot.centralizedsystem.domains.FormControl;
import springboot.centralizedsystem.domains.Group;
import springboot.centralizedsystem.domains.ImportFile;
import springboot.centralizedsystem.domains.User;
import springboot.centralizedsystem.resources.Configs;
import springboot.centralizedsystem.resources.Keys;
import springboot.centralizedsystem.resources.Messages;
import springboot.centralizedsystem.resources.RequestsPath;
import springboot.centralizedsystem.resources.Views;
import springboot.centralizedsystem.services.FormControlService;
import springboot.centralizedsystem.services.FormService;
import springboot.centralizedsystem.services.GroupService;
import springboot.centralizedsystem.services.RoleService;
import springboot.centralizedsystem.services.SendEmailService;
import springboot.centralizedsystem.utils.SessionUtils;
import springboot.centralizedsystem.utils.ValidateUtils;

@Controller
public class FormController extends BaseController {

    @Autowired
    private FormService formService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private FormControlService formControlService;

    @Autowired
    private SendEmailService sendEmailService;

    @Autowired
    private GroupService groupService;

    @GetMapping(RequestsPath.FORMS)
    public String formsGET(ModelMap model, HttpSession session, @PathVariable String page, RedirectAttributes redirect,
            @ModelAttribute(Keys.DELETE) String deleteMess, @ModelAttribute(Keys.IMPORT) String importMess) {
        try {
            if (!SessionUtils.isAdmin(session)) {
                return roleForbidden(redirect);
            }
            User user = SessionUtils.getUser(session);

            int sizeListForms = formControlService.findByOwner(user.getEmail()).size();
            int currPage = Integer.parseInt(page);
            int totalPages = (int) Math.ceil((float) sizeListForms / Configs.NUMBER_ROWS_PER_PAGE);

            model.addAttribute("list", formService.findAllForms(user.getToken(), user.getEmail(), currPage));

            if (!deleteMess.equals("")) {
                boolean isDeleteSuccess = Boolean.parseBoolean(deleteMess);
                model.addAttribute("deleteMess", Messages.DELETE("form", isDeleteSuccess));
                model.addAttribute("deleteStatus", isDeleteSuccess);
            }

            if (!importMess.equals("")) {
                boolean isImportSuccess = Boolean.parseBoolean(importMess);
                model.addAttribute("importMess", Messages.IMPORT(isImportSuccess));
                model.addAttribute("importStatus", isImportSuccess);
            }

            model.addAttribute("currPage", currPage);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("importFile", new ImportFile());
            model.addAttribute("title", "Forms management");

            return Views.FORMS;
        } catch (HttpClientErrorException e) {
            return Views.ERROR_404;
        } catch (HttpServerErrorException e) {
            return Views.ERROR_500;
        } catch (ParseException e) {
            return Views.ERROR_UNKNOWN;
        }
    }

    @GetMapping(RequestsPath.FORM)
    public ResponseEntity<String> formGET(@RequestParam("path") String path, HttpSession session) {
        if (!SessionUtils.isAdmin(session)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        User user = SessionUtils.getUser(session);

        return formService.findOneFormWithToken(user.getToken(), path);
    }

    @GetMapping(RequestsPath.CREATE_FORM)
    public String createFormGET(ModelMap model, HttpSession session, RedirectAttributes redirect) {
        if (!SessionUtils.isAdmin(session)) {
            return roleForbidden(redirect);
        }
        User user = SessionUtils.getUser(session);
        if (user == null) {
            return unauthorized(redirect);
        }

        model.addAttribute("title", "Form Builder");
        return Views.BUILD_FORM;
    }

    @GetMapping(RequestsPath.EDIT_FORM)
    public String editFormGET(ModelMap model, HttpSession session, RedirectAttributes redirect,
            @PathVariable String path) {
        if (!SessionUtils.isAdmin(session)) {
            return roleForbidden(redirect);
        }
        User user = SessionUtils.getUser(session);
        if (user == null) {
            return unauthorized(redirect);
        }

        model.addAttribute("pathForm", path);
        model.addAttribute("title", "Form Builder");

        return Views.BUILD_FORM;
    }

    @GetMapping(RequestsPath.BUILDER)
    public String builderGET(ModelMap model, HttpSession session, RedirectAttributes redirect,
            @RequestParam("path") String path) {
        try {
            if (!SessionUtils.isAdmin(session)) {
                return roleForbidden(redirect);
            }
            User user = SessionUtils.getUser(session);
            String token = user.getToken();

            model.addAttribute("listRoles", roleService.findAll(token));

            List<Group> listGroups = new ArrayList<>();

            JSONObject formJSON = null;
            boolean isCreate = path.equals(""); // No path parameter
            if (isCreate) {
                // Create form
                formJSON = new JSONObject();
                formJSON.put("title", "");
                formJSON.put("path", "");
                formJSON.put("name", "");
                formJSON.put("tags", new ArrayList<String>());
                formJSON.put("components", new ArrayList<String>());
                formJSON.put("oldPath", "");
                formJSON.put("startDate", "");
                formJSON.put("startTime", "");
                formJSON.put("expiredDate", "");
                formJSON.put("expiredTime", "");

                listGroups.add(groupService.findGroupParent(token, "data.idParent=root"));
            } else {
                // Edit form
                ResponseEntity<String> formRes = formService.findOneFormWithToken(token, path);
                formJSON = new JSONObject(formRes.getBody());

                FormControl formControl = formControlService.findByPathForm(path);
                if (formControl == null) {
                    return Views.ERROR_UNKNOWN;
                }
                String[] start = formControl.getStart().split(" ");
                String[] expired = formControl.getExpired().split(" ");
                String assign = formControl.getAssign();
                formJSON.put("oldPath", formControl.getPathForm());
                formJSON.put("assign", assign);
                boolean isAssignToGroup = !(assign.equals(Keys.ANONYMOUS) || assign.equals(Keys.AUTHENTICATED));
                formJSON.put("isAssignToGroup", isAssignToGroup);
                formJSON.put("startDate", start[0]);
                formJSON.put("startTime", start[1]);
                formJSON.put("expiredDate", expired[0]);
                formJSON.put("expiredTime", expired[1]);

                if (isAssignToGroup) {
                    Group currentGroup = groupService.findGroupParent(token, "data.idGroup=" + assign);
                    Group parentGroup = groupService.findGroupParent(token,
                            "data.idGroup=" + currentGroup.getIdParent());
                    listGroups = groupService.findListChildGroupByIdParentWithPage(token, parentGroup.getIdGroup(),
                            parentGroup.getName(), 0);
                } else {
                    listGroups.add(groupService.findGroupParent(token, "data.idParent=root"));
                }
            }

            model.addAttribute("isCreate", isCreate);
            model.addAttribute("obj", formJSON.toString());
            model.addAttribute("listGroups", listGroups);

            return Views.BUILDER;
        } catch (HttpClientErrorException e) {
            return Views.ERROR_404;
        } catch (HttpServerErrorException e) {
            return Views.ERROR_500;
        } catch (UnknownHttpStatusCodeException | ResourceAccessException e) {
            return Views.ERROR_UNKNOWN;
        }
    }

    @PostMapping(RequestsPath.CREATE_FORM)
    public ResponseEntity<String> createFormPOST(@RequestParam("formJSON") String formJSON,
            @RequestParam("oldPath") String oldPath, HttpSession session) throws MessagingException {
        try {
            if (!SessionUtils.isAdmin(session)) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            User user = SessionUtils.getUser(session);

            Boolean isCreate = oldPath.equals("");
            JSONObject jsonObject = new JSONObject(formJSON);
            String[] fields = { "title", "path", "name", "assign", "startDate", "startTime", "expiredDate",
                    "expiredTime" };
            for (String field : fields) {
                if (ValidateUtils.isEmptyString(jsonObject, field)) {
                    return new ResponseEntity<>("Please fill out `" + field + "` field", HttpStatus.BAD_REQUEST);
                }
            }

            String pathForm = jsonObject.getString("path");
            String assign = jsonObject.getString("assign");

            String startDate = jsonObject.getString("startDate");
            String expiredDate = jsonObject.getString("expiredDate");

            // Compare date, start > expired
            SimpleDateFormat sdf = new SimpleDateFormat(Configs.DATE_FORMAT);
            Date date1 = sdf.parse(startDate);
            Date date2 = sdf.parse(expiredDate);
            if (date1.compareTo(date2) >= 0) {
                return new ResponseEntity<>(Messages.DATE_PICK_ERROR, HttpStatus.BAD_REQUEST);
            }

            // Get start and expired date time to save in database
            String start = startDate + " " + jsonObject.getString("startTime");
            String expired = expiredDate + " " + jsonObject.getString("expiredTime");

            // Send to form.io server and save to database
            ResponseEntity<String> res = formService.buildForm(user.getToken(), formJSON, isCreate ? "" : oldPath);

            if (isCreate) {
                if (!formControlService.insert(new FormControl(pathForm, user.getEmail(), assign, start, expired))) {
                    return new ResponseEntity<>(Messages.DATABASE_ERROR, HttpStatus.BAD_REQUEST);
                }

                // Handle this - send email - took a long time
                sendEmailService.sendEmail("vandatnguyen1896@gmail.com", jsonObject.getString("title"));
            } else {
                int rowAffected = formControlService
                        .update(new FormControl(pathForm, user.getEmail(), assign, start, expired), oldPath);
                if (rowAffected < 1) {
                    return new ResponseEntity<>(Messages.DATABASE_ERROR, HttpStatus.BAD_REQUEST);
                }
            }

            return res;
        } catch (ParseException e) {
            return new ResponseEntity<>(Messages.FORMAT_DATE_ERROR, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(RequestsPath.DELETE_FORM)
    public String deleteFormDELETE(HttpSession session, RedirectAttributes redirect, @PathVariable String path) {
        if (!SessionUtils.isAdmin(session)) {
            return roleForbidden(redirect);
        }
        User user = SessionUtils.getUser(session);

        boolean isDeleteFormControlSuccess = formControlService.deleteByPathForm(path);
        if (!isDeleteFormControlSuccess) {
            redirect.addFlashAttribute(Keys.DELETE, false);
            return "redirect:" + RequestsPath.FORMS;
        }

        boolean isDeleteFormSuccess = formService.deleteForm(user.getToken(), path);
        redirect.addFlashAttribute(Keys.DELETE, Boolean.toString(isDeleteFormSuccess));

        redirect.addAttribute("page", 1);
        return "redirect:" + RequestsPath.FORMS;
    }
}
