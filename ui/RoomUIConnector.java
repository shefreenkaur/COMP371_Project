package ui;

import logic.Room;
import logic.ReservationManager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Connector class between Room UI and database
 */
public class RoomUIConnector {
    private Connection connection;

    /**
     * Constructor
     */
    public RoomUIConnector() {
        try {
            // Load JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Establish connection - adjust these parameters for your database
            connection = java.sql.DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/hotel_management", "root", "1234");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Database connection failed: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Get column names for rooms table
     */
    public String[] getRoomsTableColumns() {
        return new String[] {
                "Room Number", "Type", "Status", "Rate", "Capacity", "Features", "Last Cleaned", "Notes"
        };
    }

    /**
     * Get room data for table display
     */
    public Object[][] getRoomsTableData() {
        List<Object[]> data = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT * FROM rooms ORDER BY room_number";
            stmt = connection.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                        rs.getString("room_number"),
                        rs.getString("room_type"),
                        rs.getString("status"),
                        rs.getDouble("rate"),
                        rs.getInt("capacity"),
                        rs.getString("features"),
                        rs.getDate("last_cleaned"),
                        rs.getString("notes")
                };
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error retrieving room data: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return data.toArray(new Object[0][0]);
    }

    /**
     * Create tables if not exists
     */
    public void createTablesIfNotExist() {
        Statement stmt = null;

        try {
            stmt = connection.createStatement();

            // Create Rooms table
            String createRoomsTable = "CREATE TABLE IF NOT EXISTS rooms ("
                    + "room_number VARCHAR(10) PRIMARY KEY,"
                    + "room_type VARCHAR(50) NOT NULL,"
                    + "status VARCHAR(20) NOT NULL DEFAULT 'Available',"
                    + "rate DOUBLE NOT NULL,"
                    + "capacity INT NOT NULL,"
                    + "features TEXT,"
                    + "last_cleaned DATE,"
                    + "notes TEXT"
                    + ")";
            stmt.executeUpdate(createRoomsTable);

            // Create Room Types reference table
            String createRoomTypesTable = "CREATE TABLE IF NOT EXISTS room_types ("
                    + "type_id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "type_name VARCHAR(50) NOT NULL UNIQUE,"
                    + "base_rate DOUBLE NOT NULL,"
                    + "description TEXT"
                    + ")";
            stmt.executeUpdate(createRoomTypesTable);

            // Insert default room types if empty
            String checkRoomTypes = "SELECT COUNT(*) FROM room_types";
            ResultSet rs = stmt.executeQuery(checkRoomTypes);
            if (rs.next() && rs.getInt(1) == 0) {
                String insertRoomTypes = "INSERT INTO room_types (type_name, base_rate, description) VALUES "
                        + "('Standard', 100.00, 'Basic room with essential amenities'),"
                        + "('Deluxe', 150.00, 'Spacious room with premium amenities'),"
                        + "('Suite', 250.00, 'Separate living area and bedroom'),"
                        + "('Executive', 300.00, 'Premium suite with business facilities'),"
                        + "('Presidential', 500.00, 'Ultimate luxury accommodation')";
                stmt.executeUpdate(insertRoomTypes);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error creating database tables: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Add a new room
     */
    public boolean addRoom(String roomNumber, String type, double rate, int capacity,
                           String features, String notes) {
        PreparedStatement stmt = null;

        try {
            String sql = "INSERT INTO rooms (room_number, room_type, status, rate, capacity, features, notes) "
                    + "VALUES (?, ?, 'Available', ?, ?, ?, ?)";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, roomNumber);
            stmt.setString(2, type);
            stmt.setDouble(3, rate);
            stmt.setInt(4, capacity);
            stmt.setString(5, features);
            stmt.setString(6, notes);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error adding room: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Update room details
     */
    public boolean updateRoom(String roomNumber, String type, double rate, int capacity,
                              String features, String notes) {
        PreparedStatement stmt = null;

        try {
            String sql = "UPDATE rooms SET room_type = ?, rate = ?, capacity = ?, "
                    + "features = ?, notes = ? WHERE room_number = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, type);
            stmt.setDouble(2, rate);
            stmt.setInt(3, capacity);
            stmt.setString(4, features);
            stmt.setString(5, notes);
            stmt.setString(6, roomNumber);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error updating room: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Update room status
     */
    public boolean updateRoomStatus(String roomNumber, String status) {
        PreparedStatement stmt = null;

        try {
            String sql = "UPDATE rooms SET status = ? WHERE room_number = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setString(2, roomNumber);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error updating room status: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Delete a room
     */
    public boolean deleteRoom(String roomNumber) {
        PreparedStatement stmt = null;

        try {
            // First check if the room is being used in any reservations
            String checkSql = "SELECT COUNT(*) FROM reservations WHERE room_number = ? "
                    + "AND status IN ('Confirmed', 'Checked-in')";
            PreparedStatement checkStmt = connection.prepareStatement(checkSql);
            checkStmt.setString(1, roomNumber);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(null,
                        "Cannot delete room. It has active reservations.",
                        "Delete Error", JOptionPane.ERROR_MESSAGE);
                rs.close();
                checkStmt.close();
                return false;
            }
            rs.close();
            checkStmt.close();

            // Delete the room
            String sql = "DELETE FROM rooms WHERE room_number = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, roomNumber);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error deleting room: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get a specific room by room number
     */
    public Room getRoom(String roomNumber) {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT * FROM rooms WHERE room_number = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, roomNumber);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return new Room(
                        rs.getString("room_number"),
                        rs.getString("room_type"),
                        rs.getString("status"),
                        rs.getDouble("rate"),
                        rs.getInt("capacity"),
                        rs.getString("features"),
                        rs.getDate("last_cleaned"),
                        rs.getString("notes")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error retrieving room details: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Get room types for dropdown
     */
    public void populateRoomTypeComboBox(JComboBox<String> comboBox) {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            comboBox.removeAllItems();

            String sql = "SELECT type_name FROM room_types ORDER BY type_name";
            stmt = connection.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                comboBox.addItem(rs.getString("type_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error retrieving room types: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Check if a room is available for the specified dates
     */
    public boolean isRoomAvailable(String roomNumber, Date checkInDate, Date checkOutDate) {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT COUNT(*) FROM reservations WHERE room_number = ? "
                    + "AND status IN ('Confirmed', 'Checked-in') "
                    + "AND NOT (check_out_date <= ? OR check_in_date >= ?)";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, roomNumber);
            stmt.setDate(2, checkInDate);
            stmt.setDate(3, checkOutDate);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) == 0; // If count is 0, room is available
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error checking room availability: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Close the database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}