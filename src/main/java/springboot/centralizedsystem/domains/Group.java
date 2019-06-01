package springboot.centralizedsystem.domains;

public class Group {

    private String id;
    private String idGroup;
    private String name;
    private String idParent;
    private int status;
    private String nameParent;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(String idGroup) {
        this.idGroup = idGroup;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdParent() {
        return idParent;
    }

    public void setIdParent(String idParent) {
        this.idParent = idParent;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getNameParent() {
        return nameParent;
    }

    public void setNameParent(String nameParent) {
        this.nameParent = nameParent;
    }

    public Group(String id, String idGroup, String name, String idParent, String nameParent) {
        super();
        this.id = id;
        this.idGroup = idGroup;
        this.name = name;
        this.idParent = idParent;
        this.nameParent = nameParent;
    }

    public Group() {
        super();
    }
}
