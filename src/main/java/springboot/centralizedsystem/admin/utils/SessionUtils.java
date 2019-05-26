package springboot.centralizedsystem.admin.utils;

import javax.servlet.http.HttpSession;

import springboot.centralizedsystem.admin.domains.Admin;
import springboot.centralizedsystem.admin.resources.Keys;

public class SessionUtils {

    public static Admin getAdmin(HttpSession session) throws NullPointerException {
        return (Admin) session.getAttribute(Keys.ADMIN);
    }
}
