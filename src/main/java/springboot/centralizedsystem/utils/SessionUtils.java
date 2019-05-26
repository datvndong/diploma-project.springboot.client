package springboot.centralizedsystem.utils;

import javax.servlet.http.HttpSession;

import springboot.centralizedsystem.domains.Admin;
import springboot.centralizedsystem.resources.Keys;

public class SessionUtils {

    public static Admin getAdmin(HttpSession session) throws NullPointerException {
        return (Admin) session.getAttribute(Keys.ADMIN);
    }
}
