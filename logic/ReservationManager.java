package logic;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import database.DatabaseConnection;

public class ReservationManager {
    private Connection connection;

    public ReservationManager() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public boolean addReservation(Reservation reservation) {
        String query = "INSERT INTO reservations (guestName, checkIn, checkOut, roomType, paymentStatus) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, reservation.getGuestName());
            stmt.setDate(2, new java.sql.Date(reservation.getCheckIn().getTime()));
            stmt.setDate(3, new java.sql.Date(reservation.getCheckOut().getTime()));
            stmt.setString(4, reservation.getRoomType());
            stmt.setString(5, reservation.getPaymentStatus());
            int result = stmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Failed to add reservation: " + e.getMessage());
            return false;
        }
    }

    public boolean updateReservation(Reservation reservation) {
        String query = "UPDATE reservations SET guestName = ?, checkIn = ?, checkOut = ?, roomType = ?, paymentStatus = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, reservation.getGuestName());
            stmt.setDate(2, new java.sql.Date(reservation.getCheckIn().getTime()));
            stmt.setDate(3, new java.sql.Date(reservation.getCheckOut().getTime()));
            stmt.setString(4, reservation.getRoomType());
            stmt.setString(5, reservation.getPaymentStatus());
            stmt.setInt(6, reservation.getId());  // Assuming that Reservation class has an ID field
            int result = stmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Failed to update reservation: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteReservation(int reservationId) {
        String query = "DELETE FROM reservations WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, reservationId);
            int result = stmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Failed to delete reservation: " + e.getMessage());
            return false;
        }
    }

    public Reservation getReservationById(int reservationId) {
        String query = "SELECT * FROM reservations WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, reservationId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Reservation(
                    rs.getString("guestName"),
                    rs.getDate("checkIn"),
                    rs.getDate("checkOut"),
                    rs.getString("roomType"),
                    rs.getString("paymentStatus")
                );
            }
        } catch (SQLException e) {
            System.err.println("Failed to retrieve reservation: " + e.getMessage());
        }
        return null;
    }

    public List<Reservation> getAllReservations() {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT * FROM reservations";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                reservations.add(new Reservation(
                    rs.getString("guestName"),
                    rs.getDate("checkIn"),
                    rs.getDate("checkOut"),
                    rs.getString("roomType"),
                    rs.getString("paymentStatus")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Failed to retrieve reservations: " + e.getMessage());
        }
        return reservations;
    }
}
