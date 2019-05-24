package springboot.centralizedsystem.utils;

import javax.servlet.http.HttpSession;

import springboot.centralizedsystem.domains.User;

public class SessionUtils {

    public static User getUser(HttpSession session) throws NullPointerException {
        return (User) session.getAttribute("user");
    }
}
