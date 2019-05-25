package springboot.centralizedsystem.resources;

public class Messages {

    public static final String INVALID_ACCOUNT_ERROR = "Invalid username or password.";
    public static final String TOKEN_EXPIRED_ERROR = "Sorry, your session has expired. Please login again.";

    public static String DELETE(String resource, boolean isDeleteSuccess) {
        return isDeleteSuccess ? "Successfully deleted " + resource + "!" : "Error, failed to delete " + resource + ".";
    }

    public static final String DATABASE_ERROR = "Database error.";
    public static final String FORMAT_DATE_ERROR = "Format date error.";
    public static final String DATE_PICK_ERROR = "Start date must be before expired date.";
}
