package springboot.centralizedsystem.domains;

public class User {

    private String email;
    private String name;
    private String password;
    private String token;
    private String idGroup;
    private String gender;
    private String phoneNumber;
    private String address;
    private int status;
    private String nameGroup;
    private String id;
    private int reportsNumber;
    private int submittedNumber;

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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getNameGroup() {
        return nameGroup;
    }

    public void setNameGroup(String nameGroup) {
        this.nameGroup = nameGroup;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getReportsNumber() {
        return reportsNumber;
    }

    public void setReportsNumber(int reportsNumber) {
        this.reportsNumber = reportsNumber;
    }
    
    public int getSubmittedNumber() {
        return submittedNumber;
    }
    
    public void setSubmittedNumber(int submittedNumber) {
        this.submittedNumber = submittedNumber;
    }

    public User(String email, String name, String token, String idGroup, String gender, String phoneNumber,
            String address, String id) {
        super();
        this.email = email;
        this.name = name;
        this.token = token;
        this.idGroup = idGroup;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.id = id;
    }

    public User(String id, String email, String name, String nameGroup, String gender, String phoneNumber,
            String address) {
        super();
        this.id = id;
        this.email = email;
        this.name = name;
        this.nameGroup = nameGroup;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.address = address;
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
