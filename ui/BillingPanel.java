package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BillingPanel extends JPanel {
    private BillingUIConnector billingConnector;
    private JTable billingTable;
    private DefaultTableModel billingTableModel;
    private JTextField reservationIdField;
    private JButton createBillButton;
    private JComboBox<String> paymentStatusComboBox;
    private JComboBox<String> paymentMethodComboBox;
    private JButton updatePaymentButton;
    private JButton refreshButton;

    public BillingPanel() {
        billingConnector = new BillingUIConnector();
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create table model and table
        billingTableModel = new DefaultTableModel();
        for (String columnName : billingConnector.getBillingTableColumns()) {
            billingTableModel.addColumn(columnName);
        }

        billingTable = new JTable(billingTableModel);
        billingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        billingTable.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(billingTable);
        add(scrollPane, BorderLayout.CENTER);

        // Create control panel
        JPanel controlPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        JPanel createPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        createPanel.add(new JLabel("Reservation ID:"));

        reservationIdField = new JTextField(5);
        createPanel.add(reservationIdField);

        createBillButton = new JButton("Create Bill");
        createBillButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createBill();
            }
        });
        createPanel.add(createBillButton);

        JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        paymentPanel.add(new JLabel("Payment Status:"));

        paymentStatusComboBox = new JComboBox<>(new String[] {"Paid", "Partially Paid", "Pending"});
        paymentPanel.add(paymentStatusComboBox);

        paymentPanel.add(new JLabel("Payment Method:"));

        paymentMethodComboBox = new JComboBox<>(new String[] {"Cash", "Credit Card", "Debit Card", "Bank Transfer"});
        paymentPanel.add(paymentMethodComboBox);

        updatePaymentButton = new JButton("Update Payment");
        updatePaymentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updatePayment();
            }
        });
        paymentPanel.add(updatePaymentButton);

        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadData();
            }
        });
        paymentPanel.add(refreshButton);

        controlPanel.add(createPanel);
        controlPanel.add(paymentPanel);

        add(controlPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        // Clear table
        billingTableModel.setRowCount(0);

        // Get billing data
        Object[][] data = billingConnector.getBillingTableData();

        // Add data to table model
        for (Object[] row : data) {
            billingTableModel.addRow(row);
        }
    }

    private void createBill() {
        try {
            int reservationId = Integer.parseInt(reservationIdField.getText());

            if (billingConnector.createBill(reservationId)) {
                JOptionPane.showMessageDialog(this,
                        "Bill created successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadData();
                reservationIdField.setText("");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error creating bill. The reservation may not exist or already has a bill.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid reservation ID.",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updatePayment() {
        int selectedRow = billingTable.getSelectedRow();
        if (selectedRow >= 0) {
            selectedRow = billingTable.convertRowIndexToModel(selectedRow);
            int billId = (int) billingTableModel.getValueAt(selectedRow, 0);
            String status = (String) paymentStatusComboBox.getSelectedItem();
            String method = (String) paymentMethodComboBox.getSelectedItem();

            if (billingConnector.updatePaymentStatus(billId, status, method)) {
                JOptionPane.showMessageDialog(this,
                        "Payment status updated successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error updating payment status.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a bill to update.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}