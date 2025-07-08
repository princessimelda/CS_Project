package serverside;


public class SessionManager {
    private static String loggedInStudentId;

    public static void setLoggedInStudentId(String id) {
        loggedInStudentId = id;
    }

    public static String getLoggedInStudentId() {
        return loggedInStudentId;
    }
}

