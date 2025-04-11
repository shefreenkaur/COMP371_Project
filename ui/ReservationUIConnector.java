    package ui;

    import logic.Reservation;
    import logic.ReservationManager;
    import java.sql.Date;
    import java.text.ParseException;
    import java.text.SimpleDateFormat;
    import java.util.List;
    import javax.swing.*;
    import java.util.logging.Level;
    import java.util.logging.Logger;

    /**
     * This class acts as a connector between the UI and the ReservationManager.
     * It handles UI events and passes the data to the ReservationManager.
     */
    public class ReservationUIConnector {
        private static final Logger LOGGER = Logger.getLogger(ReservationUIConnector.class.getName());
        private final ReservationManager reservationManager;

        /**
         * Constructor initializes the ReservationManager
         */
        public ReservationUIConnector() {
            reservationManager = new ReservationManager();
        }

        /**
         * Creates a reservation from UI inputs
         *
         * @param guestNameField Guest name field
         * @param emailField Email field
         * @param phoneField Phone field
         * @param checkInField Check-in date field
         * @param checkOutField Check-out date field
         * @param roomTypeComboBox Room type combo box
         * @param guestsField Number of guests field
         * @param specialRequestsArea Special requests text area
         * @return True if reservation was created successfully, false otherwise
         */
        public boolean createReservation(JTextField guestNameField, JTextField emailField,
                                         JTextField phoneField, JTextField checkInField,
                                         JTextField checkOutField, JComboBox roomTypeComboBox,
                                         JSpinner guestsField, JTextArea specialRequestsArea) {
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
                int totalGuests = Integer.parseInt(guestsField.getNextValue().toString());

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

        /**
         * Populates a room type combo box with available room types
         *
         * @param roomTypeComboBox The combo box to populate
         */
        public void populateRoomTypeComboBox(JComboBox<ReservationManager.RoomType> roomTypeComboBox) {
            roomTypeComboBox.removeAllItems();

            List<ReservationManager.RoomType> roomTypes = reservationManager.getAllRoomTypes();
            for (ReservationManager.RoomType roomType : roomTypes) {
                roomTypeComboBox.addItem(roomType);
            }
        }

        /**
         * Checks if a room of the selected type is available for the given dates
         *
         * @param checkInField Check-in date field
         * @param checkOutField Check-out date field
         * @param roomTypeComboBox Room type combo box
         * @return True if room is available, false otherwise
         */
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

        /**
         * Updates the status of a reservation
         *
         * @param reservationId The ID of the reservation
         * @param newStatus The new status value
         * @return True if update was successful, false otherwise
         */
        public boolean updateReservationStatus(int reservationId, String newStatus) {
            return reservationManager.updateReservationStatus(reservationId, newStatus);
        }

        /**
         * Cancels a reservation
         *
         * @param reservationId The ID of the reservation to cancel
         * @return True if cancellation was successful, false otherwise
         */
        public boolean cancelReservation(int reservationId) {
            return reservationManager.cancelReservation(reservationId);
        }

        /**
         * Gets a list of reservations for a date range to display in a table
         *
         * @param startDate The start date
         * @param endDate The end date
         * @return Array of reservation data for display in a JTable
         */
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

        /**
         * Gets column names for the reservations table
         *
         * @return Array of column names
         */
        public String[] getReservationsTableColumns() {
            return new String[] {
                    "ID", "Guest Name", "Email", "Phone",
                    "Check-In", "Check-Out", "Status", "Room"
            };
        }

        /**
         * Gets a reservation by ID
         *
         * @param reservationId The ID of the reservation
         * @return The reservation object
         */
        public Reservation getReservation(int reservationId) {
            return reservationManager.getReservationById(reservationId);
        }

        public void closeConnection() {
        }
    }