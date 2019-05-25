package springboot.centralizedsystem.domains;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "formscontrol")
public class FormControl {

    @Indexed(unique = true)
    @Field(value = "pathForm")
    private String pathForm;

    @Field(value = "assign")
    private String assign;

    @Field(value = "expiredDate")
    private String expiredDate;

    @Field(value = "expiredTime")
    private String expiredTime;

    public String getPathForm() {
        return pathForm;
    }

    public void setPathForm(String pathForm) {
        this.pathForm = pathForm;
    }

    public String getAssign() {
        return assign;
    }

    public void setAssign(String assign) {
        this.assign = assign;
    }

    public String getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(String expiredDate) {
        this.expiredDate = expiredDate;
    }

    public String getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(String expiredTime) {
        this.expiredTime = expiredTime;
    }

    public FormControl(String pathForm, String assign, String expiredDate, String expiredTime) {
        super();
        this.pathForm = pathForm;
        this.assign = assign;
        this.expiredDate = expiredDate;
        this.expiredTime = expiredTime;
    }

    public FormControl() {
        super();
    }
}
