package ui;

import database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BillingUIConnector {
    private static final Logger LOGGER = Logger.getLogger(BillingUIConnector.class.getName());
    private final Connection connection;

    public BillingUIConnector() {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    public String[] getBillingTableColumns() {
        return new String[] {
                "Bill ID", "Reservation ID", "Guest Name", "Total Amount", "Payment Status", "Date"
        };
    }

    public Object[][] getBillingTableData() {
        List<Object[]> data = new ArrayList<>();
        Statement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT b.bill_id, b.reservation_id, CONCAT(g.first_name, ' ', g.last_name) AS guest_name, " +
                    "b.grand_total, b.payment_status, b.billing_date " +
                    "FROM billing b " +
                    "JOIN reservations r ON b.reservation_id = r.reservation_id " +
                    "JOIN guests g ON r.guest_id = g.guest_id " +
                    "ORDER BY b.billing_date DESC";

            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("bill_id"),
                        rs.getInt("reservation_id"),
                        rs.getString("guest_name"),
                        rs.getDouble("grand_total"),
                        rs.getString("payment_status"),
                        rs.getTimestamp("billing_date")
                };
                data.add(row);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving billing data", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing resources", e);
            }
        }

        return data.toArray(new Object[0][0]);
    }

    public boolean createBill(int reservationId) {
        CallableStatement cstmt = null;

        try {
            cstmt = connection.prepareCall("{CALL sp_create_bill(?, ?, ?)}");
            cstmt.setInt(1, reservationId);
            cstmt.setInt(2, 1); // Default admin user
            cstmt.registerOutParameter(3, Types.INTEGER);

            cstmt.execute();
            int billId = cstmt.getInt(3);

            return billId > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating bill", e);
            return false;
        } finally {
            try {
                if (cstmt != null) cstmt.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing statement", e);
            }
        }
    }

    public boolean addServiceToBill(int billId, int serviceId, int quantity, String notes) {
        CallableStatement cstmt = null;

        try {
            cstmt = connection.prepareCall("{CALL sp_add_service_to_bill(?, ?, ?, ?)}");
            cstmt.setInt(1, billId);
            cstmt.setInt(2, serviceId);
            cstmt.setInt(3, quantity);
            cstmt.setString(4, notes);

            cstmt.execute();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding service to bill", e);
            return false;
        } finally {
            try {
                if (cstmt != null) cstmt.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing statement", e);
            }
        }
    }

    public boolean updatePaymentStatus(int billId, String status, String paymentMethod) {
        CallableStatement cstmt = null;

        try {
            cstmt = connection.prepareCall("{CALL sp_update_payment_status(?, ?, ?, ?)}");
            cstmt.setInt(1, billId);
            cstmt.setString(2, status);
            cstmt.setString(3, paymentMethod);
            cstmt.setString(4, null); // notes

            cstmt.execute();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating payment status", e);
            return false;
        } finally {
            try {
                if (cstmt != null) cstmt.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing statement", e);
            }
        }
    }

    public List<String> getServices() {
        List<String> services = new ArrayList<>();
        Statement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT name FROM services ORDER BY name";
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                services.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving services", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing resources", e);
            }
        }

        return services;
    }
}
