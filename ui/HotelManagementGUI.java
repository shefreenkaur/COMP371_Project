package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import logic.Reservation;
import logic.ReservationManager;

public class HotelManagementGUI {
    private JFrame frame;
    private JTextField guestNameField, checkInField, checkOutField, roomTypeField;
    private JButton addButton;

    public HotelManagementGUI() {
        frame = new JFrame("Hotel Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
        frame.setLayout(new GridLayout(6, 2, 10, 10)); // Adjust grid layout spacing

        setupMenuBar(); // Setup menu bar for advanced GUI features

        // Enhancing text fields and labels
        JLabel guestNameLabel = new JLabel("Guest Name:");
        guestNameField = new JTextField();
        guestNameField.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JLabel checkInLabel = new JLabel("Check-In Date (yyyy-mm-dd):");
        checkInField = new JTextField();
        checkInField.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JLabel checkOutLabel = new JLabel("Check-Out Date (yyyy-mm-dd):");
        checkOutField = new JTextField();
        checkOutField.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JLabel roomTypeLabel = new JLabel("Room Type:");
        roomTypeField = new JTextField();
        roomTypeField.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // Styling and adding components
        guestNameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        checkInLabel.setFont(new Font("Arial", Font.BOLD, 14));
        checkOutLabel.setFont(new Font("Arial", Font.BOLD, 14));
        roomTypeLabel.setFont(new Font("Arial", Font.BOLD, 14));

        addButton = new JButton("Add Reservation");
        addButton.setFont(new Font("Arial", Font.BOLD, 16));
        addButton.setBackground(new Color(100, 149, 237)); // Light blue color
        addButton.setForeground(Color.WHITE);
        addButton.addActionListener(this::addReservation);

        // Adding components to the frame
        frame.add(guestNameLabel);
        frame.add(guestNameField);
        frame.add(checkInLabel);
        frame.add(checkInField);
        frame.add(checkOutLabel);
        frame.add(checkOutField);
        frame.add(roomTypeLabel);
        frame.add(roomTypeField);
        frame.add(new JLabel()); // Placeholder for grid alignment
        frame.add(addButton);

        frame.setVisible(true);
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Hotel Management System Version 1.0", "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        frame.setJMenuBar(menuBar);
    }

    private void addReservation(ActionEvent e) {
        if (!validateInputs()) {
            return;
        }

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Reservation reservation = new Reservation(
                guestNameField.getText(),
                formatter.parse(checkInField.getText()),
                formatter.parse(checkOutField.getText()),
                roomTypeField.getText(),
                "Unpaid" // Assuming default payment status
            );
            ReservationManager manager = new ReservationManager();
            boolean success = manager.addReservation(reservation);
            if (success) {
                JOptionPane.showMessageDialog(frame, "Reservation added successfully!");
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to add reservation.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
        }
    }

    private boolean validateInputs() {
        if (guestNameField.getText().trim().isEmpty() ||
            checkInField.getText().trim().isEmpty() ||
            checkOutField.getText().trim().isEmpty() ||
            roomTypeField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "All fields must be filled out", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            formatter.setLenient(false);
            formatter.parse(checkInField.getText().trim());
            formatter.parse(checkOutField.getText().trim());
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(frame, "Invalid date format. Please use YYYY-MM-DD.", "Date Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        new HotelManagementGUI();
    }
}
