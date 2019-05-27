package springboot.centralizedsystem.admin.utils;

import javax.servlet.http.HttpSession;

import springboot.centralizedsystem.admin.domains.User;
import springboot.centralizedsystem.admin.resources.Keys;

public class SessionUtils {

    public static User getAdmin(HttpSession session) throws NullPointerException {
        return (User) session.getAttribute(Keys.USER);
    }
}
