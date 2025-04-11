package ui;

import logic.Room;
import logic.Reservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.ParseException;
import java.util.Calendar;
import java.util.regex.Pattern;

/**
 * Class containing UI components for the Hotel Management System.
 * This class separates the UI creation from the main application logic.
 */
public class HotelUIs {
    // Styling constants (copy these from your main class)
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Color PRIMARY_COLOR = new Color(51, 102, 153);
    private static final Color SECONDARY_COLOR = new Color(240, 240, 240);
    private static final int BORDER_PADDING = 10;

    // Helper methods
    // Place any helper methods like createDateFormatter() and createActionButton() here

    /**
     * Helper method to create date formatter
     */
    private static MaskFormatter createDateFormatter() {
        MaskFormatter formatter = null;
        try {
            formatter = new MaskFormatter("####-##-##");
            formatter.setPlaceholderCharacter('_');
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formatter;
    }

    /**
     * Helper method to create styled action button
     */
    private static JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(REGULAR_FONT);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static class RoomsUI extends JPanel {
        private JPanel roomPanel;

        public RoomsUI() {
            roomPanel = new JPanel(new BorderLayout(10, 10));
            roomPanel.setBorder(new EmptyBorder(BORDER_PADDING, BORDER_PADDING, BORDER_PADDING, BORDER_PADDING));
            roomPanel.setBackground(SECONDARY_COLOR);

            // Initialize connector
            RoomUIConnector roomConnector = new RoomUIConnector();

            // Search panel
            JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
            searchPanel.setBackground(SECONDARY_COLOR);
            searchPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

            JLabel searchLabel = new JLabel("Search:");
            searchLabel.setFont(REGULAR_FONT);
            searchPanel.add(searchLabel, BorderLayout.WEST);

            JTextField searchField = new JTextField(20);
            searchField.setFont(REGULAR_FONT);
            searchPanel.add(searchField, BorderLayout.CENTER);

            // Create table model
            DefaultTableModel roomsTableModel = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            // Add columns to table
            for (String columnName : roomConnector.getRoomsTableColumns()) {
                roomsTableModel.addColumn(columnName);
            }

            // Populate table data
            Object[][] data = roomConnector.getRoomsTableData();
            for (Object[] row : data) {
                roomsTableModel.addRow(row);
            }

            // Create table
            JTable roomsTable = new JTable(roomsTableModel);
            roomsTable.setFont(REGULAR_FONT);
            roomsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            roomsTable.setRowHeight(25);
            roomsTable.setGridColor(new Color(230, 230, 230));
            roomsTable.setShowGrid(true);
            roomsTable.setIntercellSpacing(new Dimension(5, 5));

            // Set table header style
            roomsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
            roomsTable.getTableHeader().setBackground(PRIMARY_COLOR);
            roomsTable.getTableHeader().setForeground(Color.WHITE);
            roomsTable.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
            roomsTable.getTableHeader().setReorderingAllowed(false);

            // Enable sorting
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(roomsTableModel);
            roomsTable.setRowSorter(sorter);

            // Create scrollpane
            JScrollPane tableScrollPane = new JScrollPane(roomsTable);
            tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
            tableScrollPane.getViewport().setBackground(Color.WHITE);

            // Button panel
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
            buttonPanel.setBackground(SECONDARY_COLOR);
            buttonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

            buttonPanel.add(Box.createHorizontalGlue());

            // Create action buttons
            JButton addRoomButton = createActionButton("Add Room", new Color(76, 175, 80));
            JButton viewRoomButton = createActionButton("View Details", new Color(33, 150, 243));
            JButton deleteRoomButton = createActionButton("Delete Room", new Color(244, 67, 54));

            // Add action listeners
            addRoomButton.addActionListener(e -> {
                JDialog addDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Add Room", true);
                addDialog.setLayout(new GridLayout(0, 2));

                JTextField roomNumberField = new JTextField();
                JComboBox<String> typeComboBox = new JComboBox<>();
                roomConnector.populateRoomTypeComboBox(typeComboBox);
                JTextField rateField = new JTextField();
                JTextField capacityField = new JTextField();
                JTextArea featuresArea = new JTextArea();
                JTextArea notesArea = new JTextArea();

                addDialog.add(new JLabel("Room Number:")); addDialog.add(roomNumberField);
                addDialog.add(new JLabel("Room Type:")); addDialog.add(typeComboBox);
                addDialog.add(new JLabel("Rate:")); addDialog.add(rateField);
                addDialog.add(new JLabel("Capacity:")); addDialog.add(capacityField);
                addDialog.add(new JLabel("Features:")); addDialog.add(new JScrollPane(featuresArea));
                addDialog.add(new JLabel("Notes:")); addDialog.add(new JScrollPane(notesArea));

                JButton saveButton = new JButton("Save");
                saveButton.addActionListener(saveEvent -> {
                    try {
                        String roomNumber = roomNumberField.getText().trim();
                        String type = (String) typeComboBox.getSelectedItem();
                        double rate = Double.parseDouble(rateField.getText().trim());
                        int capacity = Integer.parseInt(capacityField.getText().trim());
                        String features = featuresArea.getText().trim();
                        String notes = notesArea.getText().trim();

                        if (roomConnector.addRoom(roomNumber, type, rate, capacity, features, notes)) {
                            JOptionPane.showMessageDialog(addDialog, "Room added successfully!");
                            // Refresh table
                            roomsTableModel.setRowCount(0);
                            for (Object[] row : roomConnector.getRoomsTableData()) {
                                roomsTableModel.addRow(row);
                            }
                            addDialog.dispose();
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(addDialog, "Error: " + ex.getMessage());
                    }
                });

                addDialog.add(saveButton);
                addDialog.setSize(400, 500);
                addDialog.setLocationRelativeTo(this);
                addDialog.setVisible(true);
            });

            viewRoomButton.addActionListener(e -> {
                int selectedRow = roomsTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(this, "Select a room to view details");
                    return;
                }

                // Convert to model index in case of sorting
                selectedRow = roomsTable.convertRowIndexToModel(selectedRow);
                String roomNumber = (String) roomsTableModel.getValueAt(selectedRow, 0);

                Room room = roomConnector.getRoom(roomNumber);
                if (room != null) {
                    JDialog detailDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Room Details", true);
                    detailDialog.setLayout(new GridLayout(0, 2));

                    detailDialog.add(new JLabel("Room Number:"));
                    detailDialog.add(new JLabel(room.getRoomNumber()));
                    detailDialog.add(new JLabel("Room Type:"));
                    //                detailDialog.add(new JLabel(room.getRoomType()));
                    detailDialog.add(new JLabel("Status:"));
                    detailDialog.add(new JLabel(room.getStatus()));
                    detailDialog.add(new JLabel("Rate:"));
                    //                detailDialog.add(new JLabel("$" + room.getRate()));
                    detailDialog.add(new JLabel("Capacity:"));
                    detailDialog.add(new JLabel(String.valueOf(room.getCapacity())));
                    detailDialog.add(new JLabel("Features:"));
                    //                detailDialog.add(new JLabel(room.getFeatures()));
                    detailDialog.add(new JLabel("Last Cleaned:"));
                    detailDialog.add(new JLabel(room.getLastCleaned() != null ?
                            room.getLastCleaned().toString() : "Not recorded"));
                    detailDialog.add(new JLabel("Notes:"));
                    detailDialog.add(new JLabel(room.getNotes()));

                    JButton closeButton = new JButton("Close");
                    closeButton.addActionListener(closeEvent -> detailDialog.dispose());
                    detailDialog.add(closeButton);

                    detailDialog.setSize(400, 500);
                    detailDialog.setLocationRelativeTo(this);
                    detailDialog.setVisible(true);
                }
            });

            deleteRoomButton.addActionListener(e -> {
                int selectedRow = roomsTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(this, "Select a room to delete");
                    return;
                }

                // Convert to model index in case of sorting
                selectedRow = roomsTable.convertRowIndexToModel(selectedRow);
                String roomNumber = (String) roomsTableModel.getValueAt(selectedRow, 0);

                int confirm = JOptionPane.showConfirmDialog(this,
                        "Delete room " + roomNumber + "?",
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    if (roomConnector.deleteRoom(roomNumber)) {
                        JOptionPane.showMessageDialog(this, "Room deleted successfully!");
                        // Refresh table
                        roomsTableModel.setRowCount(0);
                        for (Object[] row : roomConnector.getRoomsTableData()) {
                            roomsTableModel.addRow(row);
                        }
                    }
                }
            });

            // Search functionality
            searchField.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyReleased(java.awt.event.KeyEvent e) {
                    String searchText = searchField.getText().trim();

                    if (searchText.isEmpty()) {
                        sorter.setRowFilter(null);
                    } else {
                        // Case-insensitive search across all columns
                        RowFilter<DefaultTableModel, Object> filter = RowFilter.regexFilter(
                                "(?i)" + Pattern.quote(searchText), 0, 1, 2, 3, 4, 5, 6, 7);
                        sorter.setRowFilter(filter);
                    }
                }
            });

            buttonPanel.add(addRoomButton);
            buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
            buttonPanel.add(viewRoomButton);
            buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
            buttonPanel.add(deleteRoomButton);

            // Combine components
            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.add(searchPanel, BorderLayout.NORTH);
            mainPanel.add(tableScrollPane, BorderLayout.CENTER);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            roomPanel.add(mainPanel, BorderLayout.CENTER);

            // Add the room panel to this panel
            setLayout(new BorderLayout());
            add(roomPanel, BorderLayout.CENTER);
        }
    }

    /**
     * Inventory Panel UI
     */
    public static class InventoryUI extends JPanel {
        public InventoryUI() {
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(BORDER_PADDING, BORDER_PADDING, BORDER_PADDING, BORDER_PADDING));
            setBackground(SECONDARY_COLOR);

            // Initialize connector
                InventoryUIConnector inventoryConnector = new InventoryUIConnector();

            // Create tabs for different inventory categories
            JTabbedPane inventoryTabs = new JTabbedPane();
            inventoryTabs.setFont(REGULAR_FONT);

            // Add tabs for different inventory categories
            inventoryTabs.addTab("Room Supplies", createInventoryTypePanel(inventoryConnector, "Room"));
            inventoryTabs.addTab("Bathroom Supplies", createInventoryTypePanel(inventoryConnector, "Bathroom"));
            inventoryTabs.addTab("Cleaning Supplies", createInventoryTypePanel(inventoryConnector, "Cleaning"));
            inventoryTabs.addTab("Amenities", createInventoryTypePanel(inventoryConnector, "Amenities"));
            inventoryTabs.addTab("Maintenance", createInventoryTypePanel(inventoryConnector, "Maintenance"));

            add(inventoryTabs, BorderLayout.CENTER);
        }

        /**
         * Creates a panel for a specific inventory type
         */
        private JPanel createInventoryTypePanel(InventoryUIConnector connector, String type) {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBackground(SECONDARY_COLOR);

            // Search panel
            JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
            searchPanel.setBackground(SECONDARY_COLOR);
            searchPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

            JLabel searchLabel = new JLabel("Search:");
            searchLabel.setFont(REGULAR_FONT);
            searchPanel.add(searchLabel, BorderLayout.WEST);

            JTextField searchField = new JTextField(20);
            searchField.setFont(REGULAR_FONT);
            searchPanel.add(searchField, BorderLayout.CENTER);

            // Create table model
            DefaultTableModel inventoryTableModel = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column != 0; // Allow editing except for item ID
                }
            };

            // Add columns to table
            for (String columnName : connector.getInventoryTableColumns()) {
                inventoryTableModel.addColumn(columnName);
            }

            // Populate table data
            Object[][] data = connector.getInventoryData(type);
            for (Object[] row : data) {
                inventoryTableModel.addRow(row);
            }

            // Create table
            JTable inventoryTable = new JTable(inventoryTableModel);
            inventoryTable.setFont(REGULAR_FONT);
            inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            inventoryTable.setRowHeight(25);
            inventoryTable.setGridColor(new Color(230, 230, 230));
            inventoryTable.setShowGrid(true);
            inventoryTable.setIntercellSpacing(new Dimension(5, 5));

            // Set table header style
            inventoryTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
            inventoryTable.getTableHeader().setBackground(PRIMARY_COLOR);
            inventoryTable.getTableHeader().setForeground(Color.WHITE);
            inventoryTable.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
            inventoryTable.getTableHeader().setReorderingAllowed(false);

            // Enable sorting
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(inventoryTableModel);
            inventoryTable.setRowSorter(sorter);

            // Create scrollpane
            JScrollPane tableScrollPane = new JScrollPane(inventoryTable);
            tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
            tableScrollPane.getViewport().setBackground(Color.WHITE);

            // Button panel
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
            buttonPanel.setBackground(SECONDARY_COLOR);
            buttonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

            buttonPanel.add(Box.createHorizontalGlue());

            // Create action buttons
            JButton addItemButton = createActionButton("Add Item", new Color(76, 175, 80));
            JButton saveChangesButton = createActionButton("Save Changes", new Color(33, 150, 243));
            JButton deleteItemButton = createActionButton("Delete Item", new Color(244, 67, 54));
            JButton orderSuppliesButton = createActionButton("Order Supplies", new Color(156, 39, 176));

            // Add action listeners
            addItemButton.addActionListener(e -> {
                JDialog addDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Add Inventory Item", true);
                addDialog.setLayout(new GridLayout(0, 2, 10, 10));
                addDialog.setModal(BorderFactory.createEmptyBorder(15, 15, 15, 15).isBorderOpaque());

                JTextField nameField = new JTextField();
                JTextField descriptionField = new JTextField();
                JTextField quantityField = new JTextField();
                JTextField minLevelField = new JTextField();
                JTextField supplierField = new JTextField();
                JTextField costField = new JTextField();

                addDialog.add(new JLabel("Item Name:"));
                addDialog.add(nameField);
                addDialog.add(new JLabel("Description:"));
                addDialog.add(descriptionField);
                addDialog.add(new JLabel("Quantity:"));
                addDialog.add(quantityField);
                addDialog.add(new JLabel("Minimum Level:"));
                addDialog.add(minLevelField);
                addDialog.add(new JLabel("Supplier:"));
                addDialog.add(supplierField);
                addDialog.add(new JLabel("Cost per Unit:"));
                addDialog.add(costField);

                JButton saveButton = new JButton("Save");
                saveButton.addActionListener(saveEvent -> {
                    try {
                        String name = nameField.getText().trim();
                        String description = descriptionField.getText().trim();
                        int quantity = Integer.parseInt(quantityField.getText().trim());
                        int minLevel = Integer.parseInt(minLevelField.getText().trim());
                        String supplier = supplierField.getText().trim();
                        double cost = Double.parseDouble(costField.getText().trim());

                        if (connector.addInventoryItem(type, name, description, quantity, minLevel, supplier, cost)) {
                            JOptionPane.showMessageDialog(addDialog, "Item added successfully!");
                            // Refresh table
                            inventoryTableModel.setRowCount(0);
                            for (Object[] row : connector.getInventoryData(type)) {
                                inventoryTableModel.addRow(row);
                            }
                            addDialog.dispose();
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(addDialog, "Error: " + ex.getMessage());
                    }
                });

                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(cancelEvent -> addDialog.dispose());

                JPanel buttonPanel1 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                buttonPanel1.add(saveButton);
                buttonPanel1.add(cancelButton);

                addDialog.add(new JLabel(""));
                addDialog.add(buttonPanel1);

                addDialog.setSize(400, 350);
                addDialog.setLocationRelativeTo(this);
                addDialog.setVisible(true);
            });

            saveChangesButton.addActionListener(e -> {
                try {
                    // Update all rows in the table
                    boolean success = true;
                    for (int row = 0; row < inventoryTableModel.getRowCount(); row++) {
                        int itemId = (int) inventoryTableModel.getValueAt(row, 0);
                        String name = (String) inventoryTableModel.getValueAt(row, 1);
                        String description = (String) inventoryTableModel.getValueAt(row, 2);
                        int quantity = Integer.parseInt(inventoryTableModel.getValueAt(row, 3).toString());
                        int minLevel = Integer.parseInt(inventoryTableModel.getValueAt(row, 4).toString());
                        String supplier = (String) inventoryTableModel.getValueAt(row, 5);
                        double cost = Double.parseDouble(inventoryTableModel.getValueAt(row, 6).toString());

                        if (!connector.updateInventoryItem(itemId, name, description, quantity, minLevel, supplier, cost)) {
                            success = false;
                        }
                    }

                    if (success) {
                        JOptionPane.showMessageDialog(this, "Changes saved successfully!");
                        // Removed updateStatusLabel call
                    } else {
                        JOptionPane.showMessageDialog(this, "Some items could not be updated. Please check your inputs.",
                                "Update Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error saving changes: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            deleteItemButton.addActionListener(e -> {
                int selectedRow = inventoryTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(this, "Please select an item to delete");
                    return;
                }

                // Convert to model index in case of sorting
                selectedRow = inventoryTable.convertRowIndexToModel(selectedRow);
                int itemId = (int) inventoryTableModel.getValueAt(selectedRow, 0);
                String itemName = (String) inventoryTableModel.getValueAt(selectedRow, 1);

                int confirm = JOptionPane.showConfirmDialog(this,
                        "Delete item: " + itemName + "?",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    if (connector.deleteInventoryItem(itemId)) {
                        JOptionPane.showMessageDialog(this, "Item deleted successfully!");
                        inventoryTableModel.removeRow(selectedRow);
                        // Removed updateStatusLabel call
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Error deleting item",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            orderSuppliesButton.addActionListener(e -> {
                // Get all items below minimum level
                StringBuilder orderList = new StringBuilder("Items to order:\n\n");
                boolean hasItemsToOrder = false;

                for (int row = 0; row < inventoryTableModel.getRowCount(); row++) {
                    int quantity = Integer.parseInt(inventoryTableModel.getValueAt(row, 3).toString());
                    int minLevel = Integer.parseInt(inventoryTableModel.getValueAt(row, 4).toString());

                    if (quantity < minLevel) {
                        String itemName = (String) inventoryTableModel.getValueAt(row, 1);
                        String supplier = (String) inventoryTableModel.getValueAt(row, 5);
                        int orderAmount = minLevel - quantity;

                        orderList.append(itemName)
                                .append(" (")
                                .append(orderAmount)
                                .append(" units) from ")
                                .append(supplier)
                                .append("\n");

                        hasItemsToOrder = true;
                    }
                }

                if (hasItemsToOrder) {
                    JTextArea orderArea = new JTextArea(orderList.toString());
                    orderArea.setEditable(false);
                    orderArea.setFont(REGULAR_FONT);

                    JScrollPane scrollPane = new JScrollPane(orderArea);
                    scrollPane.setPreferredSize(new Dimension(400, 300));

                    JOptionPane.showMessageDialog(this,
                            scrollPane,
                            "Order List", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "All inventory items are above minimum levels. No orders needed.",
                            "Order List", JOptionPane.INFORMATION_MESSAGE);
                }
            });

            // Search functionality
            searchField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    String searchText = searchField.getText().trim();

                    if (searchText.isEmpty()) {
                        sorter.setRowFilter(null);
                    } else {
                        // Case-insensitive search across all columns
                        RowFilter<DefaultTableModel, Object> filter = RowFilter.regexFilter(
                                "(?i)" + Pattern.quote(searchText));
                        sorter.setRowFilter(filter);
                    }
                }
            });

            buttonPanel.add(addItemButton);
            buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
            buttonPanel.add(saveChangesButton);
            buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
            buttonPanel.add(deleteItemButton);
            buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
            buttonPanel.add(orderSuppliesButton);

            // Combine components
            panel.add(searchPanel, BorderLayout.NORTH);
            panel.add(tableScrollPane, BorderLayout.CENTER);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            return panel;
        }
    }

    /**
     * Billing Panel UI
     */
    public static class BillingUI extends JPanel {
        public BillingUI() {
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(BORDER_PADDING, BORDER_PADDING, BORDER_PADDING, BORDER_PADDING));
            setBackground(SECONDARY_COLOR);

            BillingUIConnector connector = new BillingUIConnector();


            JLabel title = new JLabel("Billing Management", SwingConstants.CENTER);
            title.setFont(new Font("Segoe UI", Font.BOLD, 18));
            title.setForeground(new Color(51, 102, 153));
            add(title, BorderLayout.NORTH);

            // Table to show bills
            String[] columns = {"Bill ID", "Guest ID", "Reservation ID", "Total Amount", "Status", "Date"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            Object[][] data = connector.getBillingTableData();
            for (Object[] row : data) {
                model.addRow(row);
            }
            JTable billTable = new JTable(model);

            JScrollPane scrollPane = new JScrollPane(billTable);
            scrollPane.getViewport().setBackground(Color.WHITE);
            add(scrollPane, BorderLayout.CENTER);

            // Buttons
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

            JButton generateBillBtn = new JButton("Generate Bill");
            JButton markPaidBtn = new JButton("Mark as Paid");

            generateBillBtn.addActionListener(e -> {
                JOptionPane.showMessageDialog(this, "Generate Bill functionality not implemented yet.");
            });

            markPaidBtn.addActionListener(e -> {
                JOptionPane.showMessageDialog(this, "Mark as Paid functionality not implemented yet.");
            });

            buttonPanel.add(generateBillBtn);
            buttonPanel.add(markPaidBtn);
            add(buttonPanel, BorderLayout.SOUTH);
        }
    }

    /**
     * Reporting Panel UI
     */
    public static class ReportingUI extends JPanel {
        public ReportingUI() {
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(BORDER_PADDING, BORDER_PADDING, BORDER_PADDING, BORDER_PADDING));
            setBackground(SECONDARY_COLOR);

            // Initialize connector
            ReportingUIConnector reportingConnector = new ReportingUIConnector();

            // Control panel for report selection
            JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.setBackground(SECONDARY_COLOR);
            controlPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

            JLabel reportTypeLabel = new JLabel("Report Type:");
            reportTypeLabel.setFont(REGULAR_FONT);
            controlPanel.add(reportTypeLabel);

            String[] reportTypes = {
                    "Occupancy Report",
                    "Revenue Report",
                    "Room Type Popularity",
                    "Guest Statistics",
                    "Monthly Summary"
            };
            JComboBox<String> reportTypeComboBox = new JComboBox<>(reportTypes);
            reportTypeComboBox.setFont(REGULAR_FONT);
            controlPanel.add(reportTypeComboBox);

            // Date range selection
            JLabel startDateLabel = new JLabel("Start Date:");
            startDateLabel.setFont(REGULAR_FONT);
            controlPanel.add(startDateLabel);

            JFormattedTextField startDateField = new JFormattedTextField(createDateFormatter());
            startDateField.setColumns(10);
            startDateField.setValue(new java.util.Date());
            controlPanel.add(startDateField);

            JLabel endDateLabel = new JLabel("End Date:");
            endDateLabel.setFont(REGULAR_FONT);
            controlPanel.add(endDateLabel);

            JFormattedTextField endDateField = new JFormattedTextField(createDateFormatter());
            endDateField.setColumns(10);
            endDateField.setValue(new java.util.Date());
            controlPanel.add(endDateField);

            // Create table model
            DefaultTableModel reportTableModel = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            // Create table
            JTable reportTable = new JTable(reportTableModel);
            reportTable.setFont(REGULAR_FONT);
            reportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            reportTable.setRowHeight(25);
            reportTable.setGridColor(new Color(230, 230, 230));
            reportTable.setShowGrid(true);
            reportTable.setIntercellSpacing(new Dimension(5, 5));

            // Set table header style
            reportTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
            reportTable.getTableHeader().setBackground(PRIMARY_COLOR);
            reportTable.getTableHeader().setForeground(Color.WHITE);
            reportTable.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
            reportTable.getTableHeader().setReorderingAllowed(false);

            // Create scrollpane
            JScrollPane tableScrollPane = new JScrollPane(reportTable);
            tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
            tableScrollPane.getViewport().setBackground(Color.WHITE);

            // Button panel
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
            buttonPanel.setBackground(SECONDARY_COLOR);
            buttonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

            buttonPanel.add(Box.createHorizontalGlue());

            // Create action buttons
            JButton generateReportButton = createActionButton("Generate Report", new Color(76, 175, 80));
            JButton exportPDFButton = createActionButton("Export PDF", new Color(33, 150, 243));
            JButton exportCSVButton = createActionButton("Export CSV", new Color(156, 39, 176));

            // Generate Report Action
            generateReportButton.addActionListener(e -> {
                try {
                    // Convert date fields to SQL Date
                    java.util.Date startDate = (java.util.Date) startDateField.getValue();
                    java.util.Date endDate = (java.util.Date) endDateField.getValue();

                    // Clear existing table model
                    reportTableModel.setRowCount(0);
                    reportTableModel.setColumnCount(0);

                    // Get selected report type
                    String selectedReport = (String) reportTypeComboBox.getSelectedItem();

                    // Convert dates to SQL Date
                    java.sql.Date sqlStartDate = new java.sql.Date(startDate.getTime());
                    java.sql.Date sqlEndDate = new java.sql.Date(endDate.getTime());

                    // Generate appropriate report based on selection
                    Object[][] reportData;
                    String[] columns;

                    switch (selectedReport) {
                        case "Occupancy Report":
                            reportData = reportingConnector.getOccupancyReport(sqlStartDate, sqlEndDate);
                            columns = reportingConnector.getOccupancyReportColumns();
                            break;
                        case "Revenue Report":
                            reportData = reportingConnector.getRevenueReport(sqlStartDate, sqlEndDate);
                            columns = reportingConnector.getRevenueReportColumns();
                            break;
                        case "Room Type Popularity":
                            reportData = reportingConnector.getRoomTypePopularityReport(sqlStartDate, sqlEndDate);
                            columns = reportingConnector.getRoomTypePopularityColumns();
                            break;
                        case "Guest Statistics":
                            reportData = reportingConnector.getGuestStatisticsReport(sqlStartDate, sqlEndDate);
                            columns = reportingConnector.getGuestStatisticsColumns();
                            break;
                        case "Monthly Summary":
                            // For monthly summary, we'll use the current year
                            Calendar cal = Calendar.getInstance();
                            int year = cal.get(Calendar.YEAR);
                            reportData = reportingConnector.getMonthlySummaryReport(year);
                            columns = new String[]{"Month", "Bookings", "Occupancy (%)", "Room Revenue", "Additional Revenue", "Total Revenue"};
                            break;
                        default:
                            JOptionPane.showMessageDialog(this, "Invalid report type");
                            return;
                    }

                    // Set columns
                    for (String column : columns) {
                        reportTableModel.addColumn(column);
                    }

                    // Add data
                    for (Object[] row : reportData) {
                        reportTableModel.addRow(row);
                    }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error generating report: " + ex.getMessage(),
                            "Report Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });

            // Export PDF Action
            exportPDFButton.addActionListener(e -> {
                if (reportTableModel.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(this, "Generate a report first");
                    return;
                }

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Export Report to PDF");
                fileChooser.setSelectedFile(new File("report.pdf"));

                int userSelection = fileChooser.showSaveDialog(this);

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();
                    String[] columns = new String[reportTableModel.getColumnCount()];
                    for (int i = 0; i < columns.length; i++) {
                        columns[i] = reportTableModel.getColumnName(i);
                    }

                    Object[][] data = new Object[reportTableModel.getRowCount()][reportTableModel.getColumnCount()];
                    for (int row = 0; row < reportTableModel.getRowCount(); row++) {
                        for (int col = 0; col < reportTableModel.getColumnCount(); col++) {
                            data[row][col] = reportTableModel.getValueAt(row, col);
                        }
                    }

                    // Assuming method in ReportingUIConnector
                    reportingConnector.exportReportToPDF(
                            data,
                            columns,
                            (String)reportTypeComboBox.getSelectedItem(),
                            fileToSave.getAbsolutePath()
                    );
                }
            });

            // Export CSV Action
            exportCSVButton.addActionListener(e -> {
                if (reportTableModel.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(this, "Generate a report first");
                    return;
                }

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Export Report to CSV");
                fileChooser.setSelectedFile(new File("report.csv"));

                int userSelection = fileChooser.showSaveDialog(this);

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();
                    String[] columns = new String[reportTableModel.getColumnCount()];
                    for (int i = 0; i < columns.length; i++) {
                        columns[i] = reportTableModel.getColumnName(i);
                    }

                    Object[][] data = new Object[reportTableModel.getRowCount()][reportTableModel.getColumnCount()];
                    for (int row = 0; row < reportTableModel.getRowCount(); row++) {
                        for (int col = 0; col < reportTableModel.getColumnCount(); col++) {
                            data[row][col] = reportTableModel.getValueAt(row, col);
                        }
                    }

                    // Assuming method in ReportingUIConnector
                    reportingConnector.exportReportToCSV(
                            data,
                            columns,
                            fileToSave.getAbsolutePath()
                    );
                }
            });

            buttonPanel.add(generateReportButton);
            buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
            buttonPanel.add(exportPDFButton);
            buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
            buttonPanel.add(exportCSVButton);

            // Combine components
            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.add(controlPanel, BorderLayout.NORTH);
            mainPanel.add(tableScrollPane, BorderLayout.CENTER);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            add(mainPanel, BorderLayout.CENTER);
        }
    }
}