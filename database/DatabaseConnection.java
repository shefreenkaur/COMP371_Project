package database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DatabaseConnection class implements the Singleton design pattern to manage
 * database connections for the Hotel Management System.
 */
public class DatabaseConnection {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static DatabaseConnection instance;
    private Connection connection;

    // Database configuration properties
    private String url;
    private String username;
    private String password;

    /**
     * Private constructor to prevent instantiation from outside the class.
     */
    private DatabaseConnection() {
        // Load database properties
        loadDatabaseProperties();

        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Set connection properties
            Properties connectionProps = new Properties();
            connectionProps.put("user", username);
            connectionProps.put("password", password);
            connectionProps.put("serverTimezone", "UTC");

            // Create connection
            connection = DriverManager.getConnection(url, connectionProps);
            LOGGER.info("Database connection established successfully");
        } catch (ClassNotFoundException | SQLException e) {
            LOGGER.log(Level.SEVERE, "Error connecting to the database", e);
        }
    }

    /**
     * Load database connection properties from file or use defaults
     */
    private void loadDatabaseProperties() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("config/database.properties")) {
            props.load(fis);

            // Get properties
            url = props.getProperty("url", "jdbc:mysql://localhost:3306/hotel_management");
            username = props.getProperty("username", "root");
            password = props.getProperty("password", "");

            LOGGER.info("Database properties loaded successfully");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not load database.properties file, using defaults", e);
            // Set defaults if file not found
            url = "jdbc:mysql://localhost:3306/hotel_management";
            username = "root";
            password = "";
        }
    }

    /**
     * Get the singleton instance of DatabaseConnection.
     * @return The DatabaseConnection instance
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }


    /**
     * Get the database connection.
     * @return The Connection object
     */
    public Connection getConnection() {
        try {
            // Check if connection is closed or invalid, and reconnect if necessary
            if (connection == null || connection.isClosed() || !connection.isValid(5)) {
                LOGGER.info("Reconnecting to database...");

                Properties connectionProps = new Properties();
                connectionProps.put("user", username);
                connectionProps.put("password", password);
                connectionProps.put("serverTimezone", "UTC");

                connection = DriverManager.getConnection(url, connectionProps);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking database connection", e);
        }

        return connection;
    }

    /**
     * Close the database connection.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                LOGGER.info("Database connection closed");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error closing database connection", e);
        }
    }

    /**
     * Close database resources.
     * @param resultSet The ResultSet to close
     * @param statement The PreparedStatement to close
     */
    public void closeResources(ResultSet resultSet, PreparedStatement statement) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error closing database resources", e);
        }
    }

    /**
     * Execute a stored procedure to create a reservation.
     *
     * @param firstName        Guest's first name
     * @param lastName         Guest's last name
     * @param email            Guest's email
     * @param phone            Guest's phone number
     * @param checkInDate      Check-in date
     * @param checkOutDate     Check-out date
     * @param totalGuests      Number of guests
     * @param roomTypeId       Room type ID
     * @param specialRequests  Special requests
     * @param createdBy        User ID of the creator
     * @return                 The reservation ID if successful, -1 otherwise
     */
    public int createReservation(String firstName, String lastName, String email, String phone,
                                 java.sql.Date checkInDate, java.sql.Date checkOutDate,
                                 int totalGuests, int roomTypeId, String specialRequests, int createdBy) {
        CallableStatement callStmt = null;
        int reservationId = -1;

        try {
            callStmt = getConnection().prepareCall("{CALL sp_create_reservation(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}");

            callStmt.setString(1, firstName);
            callStmt.setString(2, lastName);
            callStmt.setString(3, email);
            callStmt.setString(4, phone);
            callStmt.setDate(5, checkInDate);
            callStmt.setDate(6, checkOutDate);
            callStmt.setInt(7, totalGuests);
            callStmt.setInt(8, roomTypeId);
            callStmt.setString(9, specialRequests);
            callStmt.setInt(10, createdBy);
            callStmt.registerOutParameter(11, Types.INTEGER);

            callStmt.execute();
            reservationId = callStmt.getInt(11);

            LOGGER.info("Reservation created with ID: " + reservationId);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating reservation", e);
        } finally {
            try {
                if (callStmt != null) {
                    callStmt.close();
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing CallableStatement", e);
            }
        }

        return reservationId;
    }

    /**
     * Execute a stored procedure to update reservation status.
     *
     * @param reservationId  The reservation ID
     * @param status         The new status
     * @return               True if successful, false otherwise
     */
    public boolean updateReservationStatus(int reservationId, String status) {
        CallableStatement callStmt = null;

        try {
            callStmt = getConnection().prepareCall("{CALL sp_update_reservation_status(?, ?)}");

            callStmt.setInt(1, reservationId);
            callStmt.setString(2, status);

            callStmt.execute();

            LOGGER.info("Reservation status updated for ID: " + reservationId);
            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating reservation status", e);
            return false;
        } finally {
            try {
                if (callStmt != null) {
                    callStmt.close();
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing CallableStatement", e);
            }
        }
    }


}