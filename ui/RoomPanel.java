package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RoomPanel extends JPanel {
    private RoomUIConnector roomConnector;
    private JTable roomsTable;
    private DefaultTableModel roomsTableModel;
    private JComboBox<String> statusComboBox;
    private JButton updateStatusButton;
    private JButton refreshButton;

    public RoomPanel() {
        roomConnector = new RoomUIConnector();
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create table model and table
        roomsTableModel = new DefaultTableModel();
        for (String columnName : roomConnector.getRoomsTableColumns()) {
            roomsTableModel.addColumn(columnName);
        }

        roomsTable = new JTable(roomsTableModel);
        roomsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomsTable.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(roomsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Create control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(new JLabel("Room Status:"));

        statusComboBox = new JComboBox<>(roomConnector.getRoomStatuses());
        controlPanel.add(statusComboBox);

        updateStatusButton = new JButton("Update Status");
        updateStatusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateRoomStatus();
            }
        });
        controlPanel.add(updateStatusButton);

        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadData();
            }
        });
        controlPanel.add(refreshButton);

        add(controlPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        // Clear table
        roomsTableModel.setRowCount(0);

        // Get room data
        Object[][] data = roomConnector.getRoomsTableData();

        // Add data to table model
        for (Object[] row : data) {
            roomsTableModel.addRow(row);
        }
    }

    private void updateRoomStatus() {
        int selectedRow = roomsTable.getSelectedRow();
        if (selectedRow >= 0) {
            selectedRow = roomsTable.convertRowIndexToModel(selectedRow);
            String roomNumber = (String) roomsTableModel.getValueAt(selectedRow, 0);
            String newStatus = (String) statusComboBox.getSelectedItem();

            if (roomConnector.updateRoomStatus(roomNumber, newStatus)) {
                JOptionPane.showMessageDialog(this,
                        "Room status updated successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error updating room status.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a room to update.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
