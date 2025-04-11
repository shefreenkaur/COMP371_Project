package logic;

import database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages all reservation-related operations in the system.
 * This class acts as a bridge between the UI and the database for reservation operations.
 */
public class ReservationManager {
    private static final Logger LOGGER = Logger.getLogger(ReservationManager.class.getName());
    private final DatabaseConnection dbConnection;

    // Current user ID (would be set after login in a real system)
    private int currentUserId = 1; // Default to admin for this example

    /**
     * Constructor initializes database connection
     */
    public ReservationManager() {
        dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Creates a new reservation in the system
     *
     * @param reservation The reservation object with required details
     * @return True if creation was successful, false otherwise
     */
    public boolean createReservation(Reservation reservation) {
        int reservationId = dbConnection.createReservation(
                reservation.getFirstName(),
                reservation.getLastName(),
                reservation.getEmail(),
                reservation.getPhone(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate(),
                reservation.getTotalGuests(),
                reservation.getRoomTypeId(),
                reservation.getSpecialRequests(),
                currentUserId
        );

        if (reservationId > 0) {
            reservation.setReservationId(reservationId);
            LOGGER.info("Reservation created successfully with ID: " + reservationId);
            return true;
        } else {
            LOGGER.warning("Failed to create reservation");
            return false;
        }
    }

    /**
     * Updates the status of a reservation
     *
     * @param reservationId The ID of the reservation
     * @param newStatus The new status to set
     * @return True if update was successful, false otherwise
     */
    public boolean updateReservationStatus(int reservationId, String newStatus) {
        return dbConnection.updateReservationStatus(reservationId, newStatus);
    }

    /**
     * Cancels a reservation
     *
     * @param reservationId The ID of the reservation to cancel
     * @return True if cancellation was successful, false otherwise
     */
    public boolean cancelReservation(int reservationId) {
        Connection conn = null;
        CallableStatement callStmt = null;
        boolean success = false;

        try {
            conn = dbConnection.getConnection();
            callStmt = conn.prepareCall("{CALL sp_cancel_reservation(?)}");
            callStmt.setInt(1, reservationId);

            callStmt.execute();
            success = true;

            LOGGER.info("Reservation cancelled: " + reservationId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error cancelling reservation", e);
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
     * Gets a list of all room types available in the system
     *
     * @return List of room types with ID and name
     */
    public List<RoomType> getAllRoomTypes() {
        List<RoomType> roomTypes = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT type_id, name, base_price, capacity FROM room_types");

            while (rs.next()) {
                RoomType roomType = new RoomType();
                roomType.setId(rs.getInt("type_id"));
                roomType.setName(rs.getString("name"));
                roomType.setBasePrice(rs.getDouble("base_price"));
                roomType.setCapacity(rs.getInt("capacity"));

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
     * Gets available rooms for a date range and room type
     *
     * @param checkInDate The check-in date
     * @param checkOutDate The check-out date
     * @param roomTypeId The room type ID (or null for all types)
     * @return List of available rooms
     */
    public List<Room> getAvailableRooms(Date checkInDate, Date checkOutDate, Integer roomTypeId) {
        List<Room> availableRooms = new ArrayList<>();
        Connection conn = null;
        CallableStatement callStmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            callStmt = conn.prepareCall("{CALL sp_get_available_rooms(?, ?, ?)}");

            callStmt.setDate(1, checkInDate);
            callStmt.setDate(2, checkOutDate);

            if (roomTypeId != null) {
                callStmt.setInt(3, roomTypeId);
            } else {
                callStmt.setNull(3, Types.INTEGER);
            }

            boolean hasResults = callStmt.execute();
            if (hasResults) {
                rs = callStmt.getResultSet();

                while (rs.next()) {
                    Room room = new Room();
                    room.setRoomId(rs.getInt("room_id"));
                    room.setRoomNumber(rs.getString("room_number"));
                    room.setRoomType(rs.getString("room_type"));
                    room.setBasePrice(rs.getDouble("base_price"));
                    room.setCapacity(rs.getInt("capacity"));

                    availableRooms.add(room);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting available rooms", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (callStmt != null) callStmt.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing resources", e);
            }
        }

        return availableRooms;
    }

    /**
     * Gets a list of reservations within a date range
     *
     * @param startDate Start date for the search
     * @param endDate End date for the search
     * @return List of reservations
     */
    public List<Reservation> getReservationsByDateRange(Date startDate, Date endDate) {
        List<Reservation> reservations = new ArrayList<>();
        Connection conn = null;
        CallableStatement callStmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            callStmt = conn.prepareCall("{CALL sp_get_reservations_by_date_range(?, ?)}");

            callStmt.setDate(1, startDate);
            callStmt.setDate(2, endDate);

            boolean hasResults = callStmt.execute();
            if (hasResults) {
                rs = callStmt.getResultSet();

                while (rs.next()) {
                    Reservation reservation = new Reservation();
                    reservation.setReservationId(rs.getInt("reservation_id"));

                    // Split the full name into first and last name
                    String fullName = rs.getString("guest_name");
                    String[] nameParts = fullName.split(" ", 2);
                    reservation.setFirstName(nameParts[0]);
                    reservation.setLastName(nameParts.length > 1 ? nameParts[1] : "");

                    reservation.setEmail(rs.getString("email"));
                    reservation.setPhone(rs.getString("phone"));
                    reservation.setCheckInDate(rs.getDate("check_in_date"));
                    reservation.setCheckOutDate(rs.getDate("check_out_date"));
                    reservation.setStatus(rs.getString("status"));
                    reservation.setTotalGuests(rs.getInt("total_guests"));
                    reservation.setRoomNumber(rs.getString("rooms"));
                    reservation.setSpecialRequests(rs.getString("special_requests"));
                    reservation.setCreatedAt(rs.getTimestamp("created_at"));

                    reservations.add(reservation);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting reservations by date range", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (callStmt != null) callStmt.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing resources", e);
            }
        }

        return reservations;
    }

    /**
     * Gets a specific reservation by ID
     *
     * @param reservationId The ID of the reservation to retrieve
     * @return The reservation object, or null if not found
     */
    public Reservation getReservationById(int reservationId) {
        Reservation reservation = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            String sql = "SELECT r.reservation_id, g.guest_id, g.first_name, g.last_name, " +
                    "g.email, g.phone, r.check_in_date, r.check_out_date, r.status, " +
                    "r.total_guests, r.special_requests, rm.room_number, rt.name as room_type, " +
                    "rr.rate_per_night, r.created_by, r.created_at, r.updated_at " +
                    "FROM reservations r " +
                    "JOIN guests g ON r.guest_id = g.guest_id " +
                    "JOIN reservation_rooms rr ON r.reservation_id = rr.reservation_id " +
                    "JOIN rooms rm ON rr.room_id = rm.room_id " +
                    "JOIN room_types rt ON rm.type_id = rt.type_id " +
                    "WHERE r.reservation_id = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, reservationId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                reservation = new Reservation();
                reservation.setReservationId(rs.getInt("reservation_id"));
                reservation.setGuestId(rs.getInt("guest_id"));
                reservation.setFirstName(rs.getString("first_name"));
                reservation.setLastName(rs.getString("last_name"));
                reservation.setEmail(rs.getString("email"));
                reservation.setPhone(rs.getString("phone"));
                reservation.setCheckInDate(rs.getDate("check_in_date"));
                reservation.setCheckOutDate(rs.getDate("check_out_date"));
                reservation.setStatus(rs.getString("status"));
                reservation.setTotalGuests(rs.getInt("total_guests"));
                reservation.setSpecialRequests(rs.getString("special_requests"));
                reservation.setRoomNumber(rs.getString("room_number"));
                reservation.setRoomTypeName(rs.getString("room_type"));
                reservation.setRatePerNight(rs.getDouble("rate_per_night"));
                reservation.setCreatedBy(rs.getInt("created_by"));
                reservation.setCreatedAt(rs.getTimestamp("created_at"));
                reservation.setUpdatedAt(rs.getTimestamp("updated_at"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving reservation by ID", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing resources", e);
            }
        }

        return reservation;
    }

    /**
     * Set the current user ID (would be called after login)
     *
     * @param userId The current user's ID
     */
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    /**
     * Get the current user ID
     *
     * @return The current user's ID
     */
    public int getCurrentUserId() {
        return currentUserId;
    }

    /**
     * Inner class to represent a Room Type
     */
    public static class RoomType {
        private int id;
        private String name;
        private double basePrice;
        private int capacity;

        // Getters and setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public double getBasePrice() { return basePrice; }
        public void setBasePrice(double basePrice) { this.basePrice = basePrice; }

        public int getCapacity() { return capacity; }
        public void setCapacity(int capacity) { this.capacity = capacity; }

        @Override
        public String toString() {
            return name + " ($" + basePrice + ")";
        }
    }

    /**
     * Inner class to represent a Room
     */
    public static class Room {
        private int roomId;
        private String roomNumber;
        private String roomType;
        private double basePrice;
        private int capacity;

        // Getters and setters
        public int getRoomId() { return roomId; }
        public void setRoomId(int roomId) { this.roomId = roomId; }

        public String getRoomNumber() { return roomNumber; }
        public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

        public String getRoomType() { return roomType; }
        public void setRoomType(String roomType) { this.roomType = roomType; }

        public double getBasePrice() { return basePrice; }
        public void setBasePrice(double basePrice) { this.basePrice = basePrice; }

        public int getCapacity() { return capacity; }
        public void setCapacity(int capacity) { this.capacity = capacity; }

        @Override
        public String toString() {
            return "Room " + roomNumber + " (" + roomType + ")";
        }
    }
}