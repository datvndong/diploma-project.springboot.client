package springboot.centralizedsystem.domains;

public class User {

    private String email;
    private String name;
    private String password;
    private String token;
    private String idGroup;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(String idGroup) {
        this.idGroup = idGroup;
    }

    public User(String email, String name, String token, String idGroup) {
        super();
        this.email = email;
        this.name = name;
        this.token = token;
        this.idGroup = idGroup;
    }

    public User(String email, String name, String token) {
        super();
        this.email = email;
        this.name = name;
        this.token = token;
    }

    public User(String email) {
        super();
        this.email = email;
    }

    public User() {
        super();
    }
}
