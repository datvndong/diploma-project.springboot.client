package springboot.centralizedsystem.resources;

public class APIs {

    public static final String TOKEN_KEY = "x-jwt-token";

    private static final String SERVER_URL = "http://localhost:3001";
    public static final String LOGIN_URL = SERVER_URL + "/user/login";
    public static final String LOGOUT_URL = SERVER_URL + "/logout";
}
