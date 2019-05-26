package springboot.centralizedsystem.admin.domains;

public class Role {

    private String _id;
    private String title;
    private String groupCode;
    private String parentCode;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    public Role(String _id, String title, String groupCode, String parentCode) {
        super();
        this._id = _id;
        this.title = title;
        this.groupCode = groupCode;
        this.parentCode = parentCode;
    }

    public Role() {
        super();
    }
}
