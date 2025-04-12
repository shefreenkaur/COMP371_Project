package ui;

import database.DatabaseConnection;
import logic.Reservation;
import logic.ReservationManager;

import javax.swing.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReservationUIConnector {
    private static final Logger LOGGER = Logger.getLogger(ReservationUIConnector.class.getName());
    private final Connection connection;
    private final ReservationManager reservationManager;

    public ReservationUIConnector() {
        connection = DatabaseConnection.getInstance().getConnection();
        reservationManager = new ReservationManager();
    }

    public String[] getReservationsTableColumns() {
        return new String[] {
                "ID", "Guest Name", "Email", "Phone", "Check-In", "Check-Out", "Status", "Room"
        };
    }

    public Object[][] getReservationsTableData(Date startDate, Date endDate) {
        List<Reservation> reservations = reservationManager.getReservationsByDateRange(startDate, endDate);

        Object[][] data = new Object[reservations.size()][8];
        for (int i = 0; i < reservations.size(); i++) {
            Reservation res = reservations.get(i);
            data[i][0] = res.getReservationId();
            data[i][1] = res.getFullName();
            data[i][2] = res.getEmail();
            data[i][3] = res.getPhone();
            data[i][4] = res.getCheckInDate();
            data[i][5] = res.getCheckOutDate();
            data[i][6] = res.getStatus();
            data[i][7] = res.getRoomNumber();
        }

        return data;
    }

    public boolean createReservation(JTextField guestNameField, JTextField emailField,
                                     JTextField phoneField, JTextField checkInField,
                                     JTextField checkOutField, JComboBox roomTypeComboBox,
                                     JTextField guestsField, JTextArea specialRequestsArea) {
        try {
            // Parse guest name into first and last name
            String fullName = guestNameField.getText();
            String[] nameParts = fullName.split(" ", 2);
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : "";

            // Get email and phone
            String email = emailField.getText();
            String phone = phoneField.getText();

            // Parse dates
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date checkInUtil = dateFormat.parse(checkInField.getText());
            java.util.Date checkOutUtil = dateFormat.parse(checkOutField.getText());
            Date checkInDate = new Date(checkInUtil.getTime());
            Date checkOutDate = new Date(checkOutUtil.getTime());

            // Get room type ID from selected item
            ReservationManager.RoomType selectedRoomType =
                    (ReservationManager.RoomType) roomTypeComboBox.getSelectedItem();
            int roomTypeId = selectedRoomType.getId();

            // Get number of guests
            int totalGuests = Integer.parseInt(guestsField.getText());

            // Get special requests
            String specialRequests = specialRequestsArea.getText();

            // Create reservation object
            Reservation reservation = new Reservation(
                    firstName, lastName, email, phone,
                    checkInDate, checkOutDate, roomTypeId, totalGuests
            );
            reservation.setSpecialRequests(specialRequests);

            // Create reservation in database
            return reservationManager.createReservation(reservation);

        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE, "Error parsing date", e);
            return false;
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, "Error parsing number of guests", e);
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating reservation", e);
            return false;
        }
    }

    public boolean isRoomAvailable(JTextField checkInField, JTextField checkOutField, JComboBox roomTypeComboBox) {
        try {
            // Parse dates
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date checkInUtil = dateFormat.parse(checkInField.getText());
            java.util.Date checkOutUtil = dateFormat.parse(checkOutField.getText());
            Date checkInDate = new Date(checkInUtil.getTime());
            Date checkOutDate = new Date(checkOutUtil.getTime());

            // Get room type ID from selected item
            ReservationManager.RoomType selectedRoomType =
                    (ReservationManager.RoomType) roomTypeComboBox.getSelectedItem();
            int roomTypeId = selectedRoomType.getId();

            // Check availability
            List<ReservationManager.Room> availableRooms =
                    reservationManager.getAvailableRooms(checkInDate, checkOutDate, roomTypeId);

            return !availableRooms.isEmpty();

        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE, "Error parsing date", e);
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking room availability", e);
            return false;
        }
    }

    public void populateRoomTypeComboBox(JComboBox<ReservationManager.RoomType> roomTypeComboBox) {
        roomTypeComboBox.removeAllItems();

        List<ReservationManager.RoomType> roomTypes = reservationManager.getAllRoomTypes();
        for (ReservationManager.RoomType roomType : roomTypes) {
            roomTypeComboBox.addItem(roomType);
        }
    }

    public boolean updateReservationStatus(int reservationId, String newStatus) {
        return reservationManager.updateReservationStatus(reservationId, newStatus);
    }

    public boolean cancelReservation(int reservationId) {
        return reservationManager.cancelReservation(reservationId);
    }

    public Reservation getReservation(int reservationId) {
        return reservationManager.getReservationById(reservationId);
    }
}