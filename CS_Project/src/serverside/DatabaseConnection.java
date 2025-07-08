package serverside;

import java.sql.*;

public class DatabaseConnection {
    private static final String url= "jdbc:postgresql://localhost:5432/strathmore_university";
    private static final String user = "postgres";
    private static final String password= "Lyt1cs-dlr0w";
    
    public Connection getConnection(){
        try{
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            System.err.println("Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }  
        return null;
    }
}
