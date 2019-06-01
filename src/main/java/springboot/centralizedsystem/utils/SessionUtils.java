package springboot.centralizedsystem.utils;

import javax.servlet.http.HttpSession;

import springboot.centralizedsystem.domains.User;
import springboot.centralizedsystem.resources.Keys;

public class SessionUtils {

    public static User getUser(HttpSession session) {
        return (User) session.getAttribute(Keys.USER);
    }

    public static boolean isAdmin(HttpSession session) {
        return (boolean) session.getAttribute(Keys.IS_ADMIN);
    }
}
