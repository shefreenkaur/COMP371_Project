package ui;

import logic.Reservation;
import logic.ReservationManager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Main GUI class for the Hotel Management System.
 */
public class HotelManagementGUI extends JFrame {
    private ReservationUIConnector reservationConnector;

    // UI Components
    private JTabbedPane tabbedPane;
    private JPanel reservationPanel;
    private JPanel roomPanel;
    private JPanel inventoryPanel;
    private JPanel billingPanel;
    private JPanel reportingPanel;

    // Reservation Form Components
    private JTextField guestNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField checkInField;
    private JTextField checkOutField;
    private JComboBox<ReservationManager.RoomType> roomTypeComboBox;
    private JTextField guestsField;
    private JTextArea specialRequestsArea;
    private JButton addReservationButton;

    // Reservation List Components
    private JTable reservationsTable;
    private DefaultTableModel reservationsTableModel;
    private JButton viewReservationButton;
    private JButton checkInButton;
    private JButton checkOutButton;
    private JButton cancelReservationButton;

    /**
     * Constructor initializes the UI
     */
    public HotelManagementGUI() {
        // Initialize components
        reservationConnector = new ReservationUIConnector();
        initComponents();
        loadData();
    }

    /**
     * Initialize UI components
     */
    private void initComponents() {
        setTitle("Hotel Management System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create tabbed pane
        tabbedPane = new JTabbedPane();

        // Create panels for each tab
        createReservationPanel();
        createRoomPanel();
        createInventoryPanel();
        createBillingPanel();
        createReportingPanel();

        // Add panels to tabbed pane
        tabbedPane.addTab("Reservations", reservationPanel);
        tabbedPane.addTab("Rooms", roomPanel);
        tabbedPane.addTab("Inventory", inventoryPanel);
        tabbedPane.addTab("Billing", billingPanel);
        tabbedPane.addTab("Reports", reportingPanel);

        // Add tabbed pane to frame
        getContentPane().add(tabbedPane);
    }

    /**
     * Create reservation panel with form and list
     */
    private void createReservationPanel() {
        reservationPanel = new JPanel(new BorderLayout());

        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("New Reservation"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Guest Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Guest Name:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        guestNameField = new JTextField(20);
        formPanel.add(guestNameField, gbc);

        // Check-In Date
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        formPanel.add(new JLabel("Check-In Date (yyyy-mm-dd):"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        checkInField = new JTextField(10);
        formPanel.add(checkInField, gbc);

        // Check-Out Date
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Check-Out Date (yyyy-mm-dd):"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        checkOutField = new JTextField(10);
        formPanel.add(checkOutField, gbc);

        // Room Type
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Room Type:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        roomTypeComboBox = new JComboBox<>();
        formPanel.add(roomTypeComboBox, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        emailField = new JTextField(20);
        formPanel.add(emailField, gbc);

        // Phone
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        formPanel.add(new JLabel("Phone:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 5;
        phoneField = new JTextField(10);
        formPanel.add(phoneField, gbc);

        // Number of Guests
        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(new JLabel("Number of Guests:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 6;
        guestsField = new JTextField(5);
        formPanel.add(guestsField, gbc);

        // Special Requests
        gbc.gridx = 0;
        gbc.gridy = 7;
        formPanel.add(new JLabel("Special Requests:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.gridheight = 2;
        specialRequestsArea = new JTextArea(4, 20);
        specialRequestsArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(specialRequestsArea);
        formPanel.add(scrollPane, gbc);

        // Add Reservation Button
        gbc.gridx = 1;
        gbc.gridy = 9;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        addReservationButton = new JButton("Add Reservation");
        formPanel.add(addReservationButton, gbc);

        // Add action listener to button
        addReservationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addReservation();
            }
        });

        // Create list panel
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("Reservations"));

        // Create table model and table
        reservationsTableModel = new DefaultTableModel();
        for (String columnName : reservationConnector.getReservationsTableColumns()) {
            reservationsTableModel.addColumn(columnName);
        }

        reservationsTable = new JTable(reservationsTableModel);
        reservationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScrollPane = new JScrollPane(reservationsTable);
        listPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        viewReservationButton = new JButton("View Details");
        checkInButton = new JButton("Check-In");
        checkOutButton = new JButton("Check-Out");
        cancelReservationButton = new JButton("Cancel");

        buttonPanel.add(viewReservationButton);
        buttonPanel.add(checkInButton);
        buttonPanel.add(checkOutButton);
        buttonPanel.add(cancelReservationButton);

        listPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners to buttons
        viewReservationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewReservationDetails();
            }
        });

        checkInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkInReservation();
            }
        });

        checkOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkOutReservation();
            }
        });

        cancelReservationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelReservation();
            }
        });

        // Add form and list panels to main panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, formPanel, listPanel);
        splitPane.setDividerLocation(350);
        reservationPanel.add(splitPane);
    }

    private void createRoomPanel() {
        roomPanel = new RoomPanel();
    }

    private void createInventoryPanel() {
        inventoryPanel = new InventoryPanel();
    }

    private void createBillingPanel() {
        billingPanel = new BillingPanel();
    }

    private void createReportingPanel() {
        reportingPanel = new JPanel();
        reportingPanel.setLayout(new BorderLayout());
        reportingPanel.add(new JLabel("Reporting features coming soon...", JLabel.CENTER));
    }
    /**
     * Load initial data
     */
    private void loadData() {
        // Populate room type combo box
        reservationConnector.populateRoomTypeComboBox(roomTypeComboBox);

        // Set default dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        checkInField.setText(dateFormat.format(calendar.getTime()));

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        checkOutField.setText(dateFormat.format(calendar.getTime()));

        // Load reservations for the next 30 days
        loadReservations();
    }

    /**
     * Load reservations for the next 30 days
     */
    private void loadReservations() {
        // Clear table
        reservationsTableModel.setRowCount(0);

        // Get current date and date 30 days from now
        Calendar calendar = Calendar.getInstance();
        Date startDate = new Date(calendar.getTimeInMillis());

        calendar.add(Calendar.DAY_OF_MONTH, 30);
        Date endDate = new Date(calendar.getTimeInMillis());

        // Get reservation data
        Object[][] data = reservationConnector.getReservationsTableData(startDate, endDate);

        // Add data to table model
        for (Object[] row : data) {
            reservationsTableModel.addRow(row);
        }
    }

    /**
     * Add a new reservation
     */
    private void addReservation() {
        // Check if room is available
        if (!reservationConnector.isRoomAvailable(checkInField, checkOutField, roomTypeComboBox)) {
            JOptionPane.showMessageDialog(this,
                    "No rooms of the selected type are available for the specified dates.",
                    "Room Not Available", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create reservation
        boolean success = reservationConnector.createReservation(
                guestNameField, emailField, phoneField, checkInField,
                checkOutField, roomTypeComboBox, guestsField, specialRequestsArea
        );

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Reservation created successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            // Clear form
            guestNameField.setText("");
            emailField.setText("");
            phoneField.setText("");
            guestsField.setText("1");
            specialRequestsArea.setText("");

            // Reload reservations
            loadReservations();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Error creating reservation. Please check your inputs.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * View reservation details
     */
    private void viewReservationDetails() {
        int selectedRow = reservationsTable.getSelectedRow();
        if (selectedRow >= 0) {
            int reservationId = (int) reservationsTable.getValueAt(selectedRow, 0);
            Reservation reservation = reservationConnector.getReservation(reservationId);

            if (reservation != null) {
                // Create detail dialog
                JDialog detailDialog = new JDialog(this, "Reservation Details", true);
                detailDialog.setLayout(new BorderLayout());

                // Create panel for details
                JPanel detailPanel = new JPanel(new GridLayout(0, 2, 10, 5));
                detailPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                // Add reservation details
                detailPanel.add(new JLabel("Reservation ID:"));
                detailPanel.add(new JLabel(String.valueOf(reservation.getReservationId())));

                detailPanel.add(new JLabel("Guest Name:"));
                detailPanel.add(new JLabel(reservation.getFullName()));

                detailPanel.add(new JLabel("Email:"));
                detailPanel.add(new JLabel(reservation.getEmail()));

                detailPanel.add(new JLabel("Phone:"));
                detailPanel.add(new JLabel(reservation.getPhone()));

                detailPanel.add(new JLabel("Check-In Date:"));
                detailPanel.add(new JLabel(reservation.getCheckInDate().toString()));

                detailPanel.add(new JLabel("Check-Out Date:"));
                detailPanel.add(new JLabel(reservation.getCheckOutDate().toString()));

                detailPanel.add(new JLabel("Status:"));
                detailPanel.add(new JLabel(reservation.getStatus()));

                detailPanel.add(new JLabel("Number of Guests:"));
                detailPanel.add(new JLabel(String.valueOf(reservation.getTotalGuests())));

                detailPanel.add(new JLabel("Room:"));
                detailPanel.add(new JLabel(reservation.getRoomNumber()));

                detailPanel.add(new JLabel("Room Type:"));
                detailPanel.add(new JLabel(reservation.getRoomTypeName()));

                detailPanel.add(new JLabel("Rate per Night:"));
                detailPanel.add(new JLabel("$" + reservation.getRatePerNight()));

                detailPanel.add(new JLabel("Total Nights:"));
                detailPanel.add(new JLabel(String.valueOf(reservation.getNumberOfNights())));

                detailPanel.add(new JLabel("Total Cost:"));
                detailPanel.add(new JLabel("$" + reservation.getTotalRoomCost()));

                detailPanel.add(new JLabel("Special Requests:"));
                JTextArea requestsArea = new JTextArea(reservation.getSpecialRequests());
                requestsArea.setEditable(false);
                requestsArea.setLineWrap(true);
                requestsArea.setWrapStyleWord(true);
                JScrollPane scrollPane = new JScrollPane(requestsArea);
                scrollPane.setPreferredSize(new Dimension(200, 80));
                detailPanel.add(scrollPane);

                // Add close button
                JButton closeButton = new JButton("Close");
                closeButton.addActionListener(e -> detailDialog.dispose());

                // Add components to dialog
                detailDialog.add(detailPanel, BorderLayout.CENTER);
                detailDialog.add(closeButton, BorderLayout.SOUTH);

                // Set dialog size and show
                detailDialog.pack();
                detailDialog.setLocationRelativeTo(this);
                detailDialog.setVisible(true);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a reservation to view.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Check in a reservation
     */
    private void checkInReservation() {
        int selectedRow = reservationsTable.getSelectedRow();
        if (selectedRow >= 0) {
            int reservationId = (int) reservationsTable.getValueAt(selectedRow, 0);
            String currentStatus = (String) reservationsTable.getValueAt(selectedRow, 6);

            if (!"Confirmed".equals(currentStatus)) {
                JOptionPane.showMessageDialog(this,
                        "Only confirmed reservations can be checked in.",
                        "Invalid Status", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean success = reservationConnector.updateReservationStatus(reservationId, "Checked-in");

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Guest checked in successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);

                // Reload reservations
                loadReservations();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error checking in guest.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a reservation to check in.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Check out a reservation
     */
    private void checkOutReservation() {
        int selectedRow = reservationsTable.getSelectedRow();
        if (selectedRow >= 0) {
            int reservationId = (int) reservationsTable.getValueAt(selectedRow, 0);
            String currentStatus = (String) reservationsTable.getValueAt(selectedRow, 6);

            if (!"Checked-in".equals(currentStatus)) {
                JOptionPane.showMessageDialog(this,
                        "Only checked-in reservations can be checked out.",
                        "Invalid Status", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean success = reservationConnector.updateReservationStatus(reservationId, "Checked-out");

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Guest checked out successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);

                // Reload reservations
                loadReservations();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error checking out guest.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a reservation to check out.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Cancel a reservation
     */
    private void cancelReservation() {
        int selectedRow = reservationsTable.getSelectedRow();
        if (selectedRow >= 0) {
            int reservationId = (int) reservationsTable.getValueAt(selectedRow, 0);
            String currentStatus = (String) reservationsTable.getValueAt(selectedRow, 6);

            if ("Checked-in".equals(currentStatus) || "Checked-out".equals(currentStatus)) {
                JOptionPane.showMessageDialog(this,
                        "Checked-in or checked-out reservations cannot be cancelled.",
                        "Invalid Status", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int choice = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to cancel this reservation?",
                    "Confirm Cancellation", JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                boolean success = reservationConnector.cancelReservation(reservationId);

                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Reservation cancelled successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);

                    // Reload reservations
                    loadReservations();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Error cancelling reservation.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a reservation to cancel.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Main method to launch the application
     */
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create and display the GUI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new HotelManagementGUI().setVisible(true);
            }
        });
    }
}