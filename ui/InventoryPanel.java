package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InventoryPanel extends JPanel {
    private InventoryUIConnector inventoryConnector;
    private JTable inventoryTable;
    private DefaultTableModel inventoryTableModel;
    private JTextField quantityField;
    private JComboBox<String> transactionTypeComboBox;
    private JTextField notesField;
    private JButton updateStockButton;
    private JButton refreshButton;
    private JButton lowStockButton;

    public InventoryPanel() {
        inventoryConnector = new InventoryUIConnector();
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create table model and table
        inventoryTableModel = new DefaultTableModel();
        for (String columnName : inventoryConnector.getInventoryTableColumns()) {
            inventoryTableModel.addColumn(columnName);
        }

        inventoryTable = new JTable(inventoryTableModel);
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        inventoryTable.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        add(scrollPane, BorderLayout.CENTER);

        // Create control panel
        JPanel controlPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        JPanel updatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        updatePanel.add(new JLabel("Quantity:"));

        quantityField = new JTextField(5);
        updatePanel.add(quantityField);

        updatePanel.add(new JLabel("Type:"));

        transactionTypeComboBox = new JComboBox<>(new String[] {"In", "Out"});
        updatePanel.add(transactionTypeComboBox);

        updatePanel.add(new JLabel("Notes:"));

        notesField = new JTextField(20);
        updatePanel.add(notesField);

        updateStockButton = new JButton("Update Stock");
        updateStockButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateInventory();
            }
        });
        updatePanel.add(updateStockButton);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadData();
            }
        });
        buttonPanel.add(refreshButton);

        lowStockButton = new JButton("Show Low Stock");
        lowStockButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadLowStockItems();
            }
        });
        buttonPanel.add(lowStockButton);

        controlPanel.add(updatePanel);
        controlPanel.add(buttonPanel);

        add(controlPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        // Clear table
        inventoryTableModel.setRowCount(0);

        // Get inventory data
        Object[][] data = inventoryConnector.getInventoryTableData();

        // Add data to table model
        for (Object[] row : data) {
            inventoryTableModel.addRow(row);
        }
    }

    private void loadLowStockItems() {
        // Clear table
        inventoryTableModel.setRowCount(0);

        // Get low stock items
        Object[][] data = inventoryConnector.getLowStockItems();

        // Add data to table model
        for (Object[] row : data) {
            inventoryTableModel.addRow(row);
        }
    }

    private void updateInventory() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                selectedRow = inventoryTable.convertRowIndexToModel(selectedRow);
                int itemId = (int) inventoryTableModel.getValueAt(selectedRow, 0);
                int quantity = Integer.parseInt(quantityField.getText());
                String transactionType = (String) transactionTypeComboBox.getSelectedItem();
                String notes = notesField.getText();

                if (inventoryConnector.updateInventory(itemId, quantity, transactionType, notes)) {
                    JOptionPane.showMessageDialog(this,
                            "Inventory updated successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                    quantityField.setText("");
                    notesField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Error updating inventory.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid quantity.",
                        "Invalid Input", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select an item to update.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}