package springboot.centralizedsystem.admin.utils;

import org.json.JSONObject;

public class ValidateUtils {

    public static boolean isEmptyString(JSONObject jsonObject, String field) {
        return jsonObject.isNull(field) || jsonObject.getString(field).equals("");
    }
}
