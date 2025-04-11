package database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * A simple test class to verify the database connection and query execution.
 */
public class TestDatabaseConnection {
    public static void main(String[] args) {
        try {
            // Get the database connection
            DatabaseConnection dbConn = DatabaseConnection.getInstance();
            Connection conn = dbConn.getConnection();

            if (conn != null) {
                System.out.println("Database connection successful!");

                // Test a simple query
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM room_types");

                System.out.println("\nRoom Types in the database:");
                System.out.println("----------------------------");
                while (rs.next()) {
                    int id = rs.getInt("type_id");
                    String name = rs.getString("name");
                    double price = rs.getDouble("base_price");
                    System.out.println(id + ". " + name + " - $" + price + " per night");
                }

                // Close resources
                rs.close();
                stmt.close();
                dbConn.closeConnection();
                System.out.println("\nConnection closed successfully.");
            } else {
                System.out.println("Failed to establish database connection.");
            }
        } catch (Exception e) {
            System.out.println("Error testing database connection:");
            e.printStackTrace();
        }
    }
}