package springboot.centralizedsystem.admin.domains;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "groups")
public class GroupControl {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field(value = "idGroup")
    private String idGroup;

    @Field(value = "name")
    private String name;

    @Field(value = "idParent")
    private String idParent;

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

    public GroupControl(String idGroup, String name, String idParent) {
        super();
        this.idGroup = idGroup;
        this.name = name;
        this.idParent = idParent;
    }

    public GroupControl() {
        super();
    }
}
