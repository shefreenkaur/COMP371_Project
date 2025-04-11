package logic;

import database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages all room-related operations in the system.
 * This class acts as a bridge between the UI and the database for room operations.
 */
public class RoomManager {
    private static final Logger LOGGER = Logger.getLogger(RoomManager.class.getName());
    private final DatabaseConnection dbConnection;

    /**
     * Constructor initializes database connection
     */
    public RoomManager() {
        dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Gets all rooms in the system
     *
     * @return List of all rooms
     */
    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();

            String sql = "SELECT r.room_id, r.room_number, r.type_id, rt.name as type_name, " +
                    "r.floor, r.status, r.last_cleaned, r.notes, rt.base_price, rt.capacity, rt.amenities " +
                    "FROM rooms r " +
                    "JOIN room_types rt ON r.type_id = rt.type_id " +
                    "ORDER BY r.room_number";

            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Room room = new Room(rs.getString("room_number"), rs.getString("room_type"), rs.getString("status"), rs.getDouble("rate"), rs.getInt("capacity"), rs.getString("features"), rs.getDate("last_cleaned"), rs.getString("notes"));
                room.setRoomId(rs.getInt("room_id"));
                room.setRoomNumber(rs.getString("room_number"));
                room.setTypeId(rs.getInt("type_id"));
                room.setTypeName(rs.getString("type_name"));
                room.setFloor(rs.getInt("floor"));
                room.setStatus(rs.getString("status"));
                room.setLastCleaned(rs.getTimestamp("last_cleaned"));
                room.setNotes(rs.getString("notes"));
                room.setBasePrice(rs.getDouble("base_price"));
                room.setCapacity(rs.getInt("capacity"));
                room.setAmenities(rs.getString("amenities"));

                rooms.add(room);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving rooms", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing resources", e);
            }
        }

        return rooms;
    }

    /**
     * Gets rooms by status
     *
     * @param status The status to filter by
     * @return List of rooms with the specified status
     */
    public List<Room> getRoomsByStatus(String status) {
        List<Room> rooms = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();

            String sql = "SELECT r.room_id, r.room_number, r.type_id, rt.name as type_name, " +
                    "r.floor, r.status, r.last_cleaned, r.notes, rt.base_price, rt.capacity, rt.amenities " +
                    "FROM rooms r " +
                    "JOIN room_types rt ON r.type_id = rt.type_id " +
                    "WHERE r.status = ? " +
                    "ORDER BY r.room_number";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                Room room = new Room(rs.getString("room_number"), rs.getString("room_type"), rs.getString("status"), rs.getDouble("rate"), rs.getInt("capacity"), rs.getString("features"), rs.getDate("last_cleaned"), rs.getString("notes"));
                room.setRoomId(rs.getInt("room_id"));
                room.setRoomNumber(rs.getString("room_number"));
                room.setTypeId(rs.getInt("type_id"));
                room.setTypeName(rs.getString("type_name"));
                room.setFloor(rs.getInt("floor"));
                room.setStatus(rs.getString("status"));
                room.setLastCleaned(rs.getTimestamp("last_cleaned"));
                room.setNotes(rs.getString("notes"));
                room.setBasePrice(rs.getDouble("base_price"));
                room.setCapacity(rs.getInt("capacity"));
                room.setAmenities(rs.getString("amenities"));

                rooms.add(room);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving rooms by status", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing resources", e);
            }
        }

        return rooms;
    }

    /**
     * Gets a room by ID
     *
     * @param roomId The ID of the room to retrieve
     * @return The room object, or null if not found
     */
    public Room getRoomById(int roomId) {
        Room room = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();

            String sql = "SELECT r.room_id, r.room_number, r.type_id, rt.name as type_name, " +
                    "r.floor, r.status, r.last_cleaned, r.notes, rt.base_price, rt.capacity, rt.amenities " +
                    "FROM rooms r " +
                    "JOIN room_types rt ON r.type_id = rt.type_id " +
                    "WHERE r.room_id = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, roomId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                room = new Room(rs.getString("room_number"), rs.getString("room_type"), rs.getString("status"), rs.getDouble("rate"), rs.getInt("capacity"), rs.getString("features"), rs.getDate("last_cleaned"), rs.getString("notes"));
                room.setRoomId(rs.getInt("room_id"));
                room.setRoomNumber(rs.getString("room_number"));
                room.setTypeId(rs.getInt("type_id"));
                room.setTypeName(rs.getString("type_name"));
                room.setFloor(rs.getInt("floor"));
                room.setStatus(rs.getString("status"));
                room.setLastCleaned(rs.getTimestamp("last_cleaned"));
                room.setNotes(rs.getString("notes"));
                room.setBasePrice(rs.getDouble("base_price"));
                room.setCapacity(rs.getInt("capacity"));
                room.setAmenities(rs.getString("amenities"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving room by ID", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing resources", e);
            }
        }

        return room;
    }

    /**
     * Updates a room's status
     *
     * @param roomId The ID of the room
     * @param status The new status
     * @return True if update was successful, false otherwise
     */
    public boolean updateRoomStatus(int roomId, String status) {
        Connection conn = null;
        CallableStatement callStmt = null;
        boolean success = false;

        try {
            conn = dbConnection.getConnection();
            callStmt = conn.prepareCall("{CALL sp_update_room_status(?, ?)}");

            callStmt.setInt(1, roomId);
            callStmt.setString(2, status);

            callStmt.execute();
            success = true;

            LOGGER.info("Room status updated for ID: " + roomId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating room status", e);
        } finally {
            try {
                if (callStmt != null) callStmt.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing statement", e);
            }
        }

        return success;
    }

    /**
     * Creates a housekeeping task for a room
     *
     * @param roomId The ID of the room
     * @param taskType The type of task
     * @param scheduledDate The scheduled date
     * @param assignedTo The user ID of the assigned staff
     * @param notes Any notes for the task
     * @param createdBy The user ID of the creator
     * @return True if creation was successful, false otherwise
     */
    public boolean createHousekeepingTask(int roomId, String taskType,
                                          java.sql.Timestamp scheduledDate,
                                          Integer assignedTo, String notes, int createdBy) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = dbConnection.getConnection();

            String sql = "INSERT INTO housekeeping_tasks " +
                    "(room_id, task_type, status, assigned_to, scheduled_date, notes, created_by) " +
                    "VALUES (?, ?, 'Pending', ?, ?, ?, ?)";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, roomId);
            pstmt.setString(2, taskType);

            if (assignedTo != null) {
                pstmt.setInt(3, assignedTo);
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }

            pstmt.setTimestamp(4, scheduledDate);
            pstmt.setString(5, notes);
            pstmt.setInt(6, createdBy);

            int rowsAffected = pstmt.executeUpdate();
            success = rowsAffected > 0;

            LOGGER.info("Housekeeping task created for room ID: " + roomId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating housekeeping task", e);
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing statement", e);
            }
        }

        return success;
    }

    /**
     * Gets room types with their details
     *
     * @return List of room types
     */
    public List<RoomType> getRoomTypes() {
        List<RoomType> roomTypes = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.createStatement();

            String sql = "SELECT type_id, name, description, base_price, capacity, amenities " +
                    "FROM room_types ORDER BY base_price";

            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                RoomType roomType = new RoomType();
                roomType.setId(rs.getInt("type_id"));
                roomType.setName(rs.getString("name"));
                roomType.setDescription(rs.getString("description"));
                roomType.setBasePrice(rs.getDouble("base_price"));
                roomType.setCapacity(rs.getInt("capacity"));
                roomType.setAmenities(rs.getString("amenities"));

                roomTypes.add(roomType);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving room types", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing resources", e);
            }
        }

        return roomTypes;
    }

    /**
     * Inner class to represent a Room Type
     */
    public static class RoomType {
        private int id;
        private String name;
        private String description;
        private double basePrice;
        private int capacity;
        private String amenities;

        // Getters and Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public double getBasePrice() { return basePrice; }
        public void setBasePrice(double basePrice) { this.basePrice = basePrice; }

        public int getCapacity() { return capacity; }
        public void setCapacity(int capacity) { this.capacity = capacity; }

        public String getAmenities() { return amenities; }
        public void setAmenities(String amenities) { this.amenities = amenities; }

        @Override
        public String toString() {
            return name + " ($" + basePrice + " per night)";
        }
    }
}