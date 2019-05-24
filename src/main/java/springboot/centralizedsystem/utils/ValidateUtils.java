package springboot.centralizedsystem.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

public class ValidateUtils {

    private static final Pattern VALID_NAME_REGEX = Pattern.compile("[\\w-/]+",
            Pattern.CASE_INSENSITIVE);

    public static boolean isEmptyString(JSONObject jsonObject, String field) {
        return jsonObject.isNull(field) || jsonObject.getString(field).equals("");
    }

    public static boolean isValidName(String name) {
        char first = name.charAt(0);
        char last = name.charAt(name.length() - 1);
        Matcher matcher = VALID_NAME_REGEX.matcher(name);
        return matcher.matches() && first != '-' && first != '/' && last != '-' && last != '/';
    }
}
