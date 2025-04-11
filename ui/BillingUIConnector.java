package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Connector class between Billing UI and database
 */
public class BillingUIConnector {
    private Connection connection;
    private static final double TAX_RATE = 0.12; // 12% tax rate

    /**
     * Constructor
     */
    public BillingUIConnector() {
        try {
            // Load JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Establish connection - adjust these parameters for your database
            connection = java.sql.DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/hotel_management", "username", "password");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Database connection failed: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Get column names for billing table
     */
    public String[] getBillingTableColumns() {
        return new String[] {
                "Invoice ID", "Reservation ID", "Guest Name", "Room Number", "Check-In", "Check-Out",
                "Room Charges", "Additional Charges", "Taxes", "Total Amount", "Payment Status", "Payment Date"
        };
    }

    /**
     * Get column names for payment history table
     */
    public String[] getPaymentHistoryTableColumns() {
        return new String[] {
                "Payment ID", "Invoice ID", "Amount", "Payment Method", "Transaction ID", "Payment Date", "Processed By"
        };
    }

    /**
     * Create tables if not exists
     */
    public void createTablesIfNotExist() {
        Statement stmt = null;

        try {
            stmt = connection.createStatement();

            // Create Invoices table
            String createInvoicesTable = "CREATE TABLE IF NOT EXISTS invoices ("
                    + "invoice_id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "reservation_id INT NOT NULL,"
                    + "room_charges DOUBLE NOT NULL,"
                    + "additional_charges DOUBLE NOT NULL DEFAULT 0,"
                    + "taxes DOUBLE NOT NULL,"
                    + "total_amount DOUBLE NOT NULL,"
                    + "payment_status VARCHAR(20) NOT NULL DEFAULT 'Unpaid',"
                    + "payment_date DATE,"
                    + "FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id)"
                    + ")";
            stmt.executeUpdate(createInvoicesTable);

            // Create Additional Charges table
            String createChargesTable = "CREATE TABLE IF NOT EXISTS charges ("
                    + "charge_id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "invoice_id INT NOT NULL,"
                    + "description VARCHAR(255) NOT NULL,"
                    + "amount DOUBLE NOT NULL,"
                    + "charge_date DATE NOT NULL,"
                    + "FOREIGN KEY (invoice_id) REFERENCES invoices(invoice_id)"
                    + ")";
            stmt.executeUpdate(createChargesTable);

            // Create Payments table
            String createPaymentsTable = "CREATE TABLE IF NOT EXISTS payments ("
                    + "payment_id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "invoice_id INT NOT NULL,"
                    + "amount DOUBLE NOT NULL,"
                    + "payment_method VARCHAR(50) NOT NULL,"
                    + "transaction_id VARCHAR(100),"
                    + "payment_date DATE NOT NULL,"
                    + "processed_by VARCHAR(100),"
                    + "FOREIGN KEY (invoice_id) REFERENCES invoices(invoice_id)"
                    + ")";
            stmt.executeUpdate(createPaymentsTable);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error creating database tables: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get billing data for table display
     */
    public Object[][] getBillingTableData() {
        List<Object[]> data = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT i.*, r.full_name, r.room_number, r.check_in_date, r.check_out_date "
                    + "FROM invoices i "
                    + "JOIN reservations r ON i.reservation_id = r.reservation_id "
                    + "ORDER BY i.invoice_id DESC";
            stmt = connection.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("invoice_id"),
                        rs.getInt("reservation_id"),
                        rs.getString("full_name"),
                        rs.getString("room_number"),
                        rs.getDate("check_in_date"),
                        rs.getDate("check_out_date"),
                        rs.getDouble("room_charges"),
                        rs.getDouble("additional_charges"),
                        rs.getDouble("taxes"),
                        rs.getDouble("total_amount"),
                        rs.getString("payment_status"),
                        rs.getDate("payment_date")
                };
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error retrieving billing data: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return data.toArray(new Object[0][0]);
    }

    /**
     * Create an invoice for a reservation
     */
    public boolean createInvoice(int reservationId) {
        PreparedStatement stmt = null;

        try {
            // First check if invoice already exists
            String checkSql = "SELECT COUNT(*) FROM invoices WHERE reservation_id = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkSql);
            checkStmt.setInt(1, reservationId);
            ResultSet checkRs = checkStmt.executeQuery();

            if (checkRs.next() && checkRs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(null,
                        "An invoice already exists for this reservation.",
                        "Invoice Error", JOptionPane.ERROR_MESSAGE);
                checkRs.close();
                checkStmt.close();
                return false;
            }
            checkRs.close();
            checkStmt.close();

            // Get reservation details
            String reservationSql = "SELECT r.*, rm.rate FROM reservations r "
                    + "JOIN rooms rm ON r.room_number = rm.room_number "
                    + "WHERE r.reservation_id = ?";
            PreparedStatement reservationStmt = connection.prepareStatement(reservationSql);
            reservationStmt.setInt(1, reservationId);
            ResultSet rs = reservationStmt.executeQuery();

            if (rs.next()) {
                // Calculate room charges
                Date checkInDate = rs.getDate("check_in_date");
                Date checkOutDate = rs.getDate("check_out_date");
                double roomRate = rs.getDouble("rate");

                long diffInMillies = checkOutDate.getTime() - checkInDate.getTime();
                int nights = (int) (diffInMillies / (1000 * 60 * 60 * 24));
                double roomCharges = nights * roomRate;

                // Calculate taxes
                double taxes = roomCharges * TAX_RATE;
                double totalAmount = roomCharges + taxes;

                // Create invoice
                String sql = "INSERT INTO invoices (reservation_id, room_charges, additional_charges, "
                        + "taxes, total_amount, payment_status) "
                        + "VALUES (?, ?, 0, ?, ?, 'Unpaid')";
                stmt = connection.prepareStatement(sql);
                stmt.setInt(1, reservationId);
                stmt.setDouble(2, roomCharges);
                stmt.setDouble(3, taxes);
                stmt.setDouble(4, totalAmount);

                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            } else {
                JOptionPane.showMessageDialog(null,
                        "Reservation not found.",
                        "Invoice Error", JOptionPane.ERROR_MESSAGE);
                rs.close();
                reservationStmt.close();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error creating invoice: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Add additional charges to an invoice
     */
    public boolean addCharges(int invoiceId, String description, double amount) {
        PreparedStatement stmt = null;

        try {
            connection.setAutoCommit(false);

            // Insert charge into charges table
            String chargeSql = "INSERT INTO charges (invoice_id, description, amount, charge_date) "
                    + "VALUES (?, ?, ?, CURRENT_DATE)";
            PreparedStatement chargeStmt = connection.prepareStatement(chargeSql);
            chargeStmt.setInt(1, invoiceId);
            chargeStmt.setString(2, description);
            chargeStmt.setDouble(3, amount);
            chargeStmt.executeUpdate();
            chargeStmt.close();

            // Update invoice totals
            double tax = amount * TAX_RATE;
            String sql = "UPDATE invoices SET additional_charges = additional_charges + ?, "
                    + "taxes = taxes + ?, total_amount = total_amount + ? "
                    + "WHERE invoice_id = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setDouble(1, amount);
            stmt.setDouble(2, tax);
            stmt.setDouble(3, amount + tax);
            stmt.setInt(4, invoiceId);

            int rowsAffected = stmt.executeUpdate();

            connection.commit();
            return rowsAffected > 0;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error adding charges: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Process payment for an invoice
     */
    public boolean processPayment(int invoiceId, double amount, String paymentMethod,
                                  String transactionId, String processedBy) {
        PreparedStatement stmt = null;

        try {
            connection.setAutoCommit(false);

            // Get current invoice total and status
            double totalAmount = 0;
            String currentStatus = "";
            String checkSql = "SELECT total_amount, payment_status FROM invoices WHERE invoice_id = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkSql);
            checkStmt.setInt(1, invoiceId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                totalAmount = rs.getDouble("total_amount");
                currentStatus = rs.getString("payment_status");
            }
            rs.close();
            checkStmt.close();

            // Calculate total payments including this new one
            double totalPaid = amount;
            String paymentsSql = "SELECT SUM(amount) AS paid FROM payments WHERE invoice_id = ?";
            PreparedStatement paymentsStmt = connection.prepareStatement(paymentsSql);
            paymentsStmt.setInt(1, invoiceId);
            rs = paymentsStmt.executeQuery();

            if (rs.next() && rs.getObject(1) != null) {
                totalPaid += rs.getDouble(1);
            }
            rs.close();
            paymentsStmt.close();

            // Determine new payment status
            String newStatus;
            if (totalPaid >= totalAmount) {
                newStatus = "Paid";
            } else if (totalPaid > 0) {
                newStatus = "Partially Paid";
            } else {
                newStatus = "Unpaid";
            }

            // Record the payment
            String paymentSql = "INSERT INTO payments (invoice_id, amount, payment_method, "
                    + "transaction_id, payment_date, processed_by) "
                    + "VALUES (?, ?, ?, ?, CURRENT_DATE, ?)";
            PreparedStatement paymentStmt = connection.prepareStatement(paymentSql);
            paymentStmt.setInt(1, invoiceId);
            paymentStmt.setDouble(2, amount);
            paymentStmt.setString(3, paymentMethod);
            paymentStmt.setString(4, transactionId);
            paymentStmt.setString(5, processedBy);
            paymentStmt.executeUpdate();
            paymentStmt.close();

            // Update invoice status
            String sql = "UPDATE invoices SET payment_status = ?, payment_date = CURRENT_DATE "
                    + "WHERE invoice_id = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, newStatus);
            stmt.setInt(2, invoiceId);

            int rowsAffected = stmt.executeUpdate();

            connection.commit();
            return rowsAffected > 0;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error processing payment: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get payment history for an invoice
     */
    public Object[][] getPaymentHistory(int invoiceId) {
        List<Object[]> data = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT payment_id, invoice_id, amount, payment_method, transaction_id, "
                    + "payment_date, processed_by FROM payments "
                    + "WHERE invoice_id = ? ORDER BY payment_date DESC, payment_id DESC";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, invoiceId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("payment_id"),
                        rs.getInt("invoice_id"),
                        rs.getDouble("amount"),
                        rs.getString("payment_method"),
                        rs.getString("transaction_id"),
                        rs.getDate("payment_date"),
                        rs.getString("processed_by")
                };
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error retrieving payment history: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return data.toArray(new Object[0][0]);
    }

    /**
     * Get additional charges for an invoice
     */
    public Object[][] getAdditionalCharges(int invoiceId) {
        List<Object[]> data = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT charge_id, description, amount, charge_date FROM charges "
                    + "WHERE invoice_id = ? ORDER BY charge_date, charge_id";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, invoiceId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("charge_id"),
                        rs.getString("description"),
                        rs.getDouble("amount"),
                        rs.getDate("charge_date")
                };
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error retrieving additional charges: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return data.toArray(new Object[0][0]);
    }

    /**
     * Get invoices by payment status
     */
    public Object[][] getInvoicesByStatus(String paymentStatus) {
        List<Object[]> data = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql;
            if (paymentStatus != null && !paymentStatus.isEmpty() && !paymentStatus.equals("All")) {
                sql = "SELECT i.*, r.full_name, r.room_number, r.check_in_date, r.check_out_date "
                        + "FROM invoices i "
                        + "JOIN reservations r ON i.reservation_id = r.reservation_id "
                        + "WHERE i.payment_status = ? "
                        + "ORDER BY i.invoice_id DESC";
                stmt = connection.prepareStatement(sql);
                stmt.setString(1, paymentStatus);
            } else {
                sql = "SELECT i.*, r.full_name, r.room_number, r.check_in_date, r.check_out_date "
                        + "FROM invoices i "
                        + "JOIN reservations r ON i.reservation_id = r.reservation_id "
                        + "ORDER BY i.invoice_id DESC";
                stmt = connection.prepareStatement(sql);
            }

            rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("invoice_id"),
                        rs.getInt("reservation_id"),
                        rs.getString("full_name"),
                        rs.getString("room_number"),
                        rs.getDate("check_in_date"),
                        rs.getDate("check_out_date"),
                        rs.getDouble("room_charges"),
                        rs.getDouble("additional_charges"),
                        rs.getDouble("taxes"),
                        rs.getDouble("total_amount"),
                        rs.getString("payment_status"),
                        rs.getDate("payment_date")
                };
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error retrieving invoices: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return data.toArray(new Object[0][0]);
    }

    /**
     * Get invoices for a date range
     */
    public Object[][] getInvoicesByDateRange(Date startDate, Date endDate) {
        List<Object[]> data = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT i.*, r.full_name, r.room_number, r.check_in_date, r.check_out_date "
                    + "FROM invoices i "
                    + "JOIN reservations r ON i.reservation_id = r.reservation_id "
                    + "WHERE r.check_in_date BETWEEN ? AND ? "
                    + "ORDER BY i.invoice_id DESC";
            stmt = connection.prepareStatement(sql);
            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("invoice_id"),
                        rs.getInt("reservation_id"),
                        rs.getString("full_name"),
                        rs.getString("room_number"),
                        rs.getDate("check_in_date"),
                        rs.getDate("check_out_date"),
                        rs.getDouble("room_charges"),
                        rs.getDouble("additional_charges"),
                        rs.getDouble("taxes"),
                        rs.getDouble("total_amount"),
                        rs.getString("payment_status"),
                        rs.getDate("payment_date")
                };
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error retrieving invoices by date range: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return data.toArray(new Object[0][0]);
    }

    /**
     * Get payment methods for dropdown
     */
    public String[] getPaymentMethods() {
        return new String[] {
                "Cash", "Credit Card", "Debit Card", "Bank Transfer", "PayPal", "Other"
        };
    }

    /**
     * Get payment statuses for dropdown
     */
    public String[] getPaymentStatuses() {
        return new String[] {
                "All", "Unpaid", "Partially Paid", "Paid"
        };
    }

    /**
     * Get a specific invoice by ID
     */
    public Object[] getInvoice(int invoiceId) {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT i.*, r.full_name, r.room_number, r.check_in_date, r.check_out_date "
                    + "FROM invoices i "
                    + "JOIN reservations r ON i.reservation_id = r.reservation_id "
                    + "WHERE i.invoice_id = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, invoiceId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return new Object[] {
                        rs.getInt("invoice_id"),
                        rs.getInt("reservation_id"),
                        rs.getString("full_name"),
                        rs.getString("room_number"),
                        rs.getDate("check_in_date"),
                        rs.getDate("check_out_date"),
                        rs.getDouble("room_charges"),
                        rs.getDouble("additional_charges"),
                        rs.getDouble("taxes"),
                        rs.getDouble("total_amount"),
                        rs.getString("payment_status"),
                        rs.getDate("payment_date")
                };
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error retrieving invoice details: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Get reservations without invoices
     */
    public Object[][] getReservationsWithoutInvoices() {
        List<Object[]> data = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT r.reservation_id, r.full_name, r.room_number, "
                    + "r.check_in_date, r.check_out_date, r.status "
                    + "FROM reservations r "
                    + "LEFT JOIN invoices i ON r.reservation_id = i.reservation_id "
                    + "WHERE i.invoice_id IS NULL "
                    + "AND r.status IN ('Confirmed', 'Checked-in', 'Checked-out') "
                    + "ORDER BY r.check_in_date DESC";
            stmt = connection.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("reservation_id"),
                        rs.getString("full_name"),
                        rs.getString("room_number"),
                        rs.getDate("check_in_date"),
                        rs.getDate("check_out_date"),
                        rs.getString("status")
                };
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error retrieving reservations: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return data.toArray(new Object[0][0]);
    }

    /**
     * Get unpaid invoices for current guests
     */
    public Object[][] getUnpaidInvoicesForCurrentGuests() {
        List<Object[]> data = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT i.*, r.full_name, r.room_number, r.check_in_date, r.check_out_date "
                    + "FROM invoices i "
                    + "JOIN reservations r ON i.reservation_id = r.reservation_id "
                    + "WHERE i.payment_status <> 'Paid' "
                    + "AND r.status = 'Checked-in' "
                    + "ORDER BY i.invoice_id DESC";
            stmt = connection.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("invoice_id"),
                        rs.getInt("reservation_id"),
                        rs.getString("full_name"),
                        rs.getString("room_number"),
                        rs.getDate("check_in_date"),
                        rs.getDate("check_out_date"),
                        rs.getDouble("room_charges"),
                        rs.getDouble("additional_charges"),
                        rs.getDouble("taxes"),
                        rs.getDouble("total_amount"),
                        rs.getString("payment_status"),
                        rs.getDate("payment_date")
                };
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error retrieving unpaid invoices: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return data.toArray(new Object[0][0]);
    }

    /**
     * Get total revenue for a date range
     */
    public double getTotalRevenue(Date startDate, Date endDate) {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT SUM(total_amount) FROM invoices i "
                    + "JOIN reservations r ON i.reservation_id = r.reservation_id "
                    + "WHERE r.check_in_date BETWEEN ? AND ? "
                    + "AND i.payment_status = 'Paid'";
            stmt = connection.prepareStatement(sql);
            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            rs = stmt.executeQuery();

            if (rs.next() && rs.getObject(1) != null) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error calculating revenue: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return 0.0;
    }

    /**
     * Generate invoice PDF (placeholder for actual implementation)
     */
    public boolean generateInvoicePDF(int invoiceId, String outputPath) {
        try {
            // This would be implemented with a PDF library like iText or Apache PDFBox
            // For now, we'll just simulate PDF generation

            // Get invoice details
            Object[] invoice = getInvoice(invoiceId);
            if (invoice == null) {
                return false;
            }

            // Get additional charges
            Object[][] charges = getAdditionalCharges((Integer)invoice[0]);

            // Get payment history
            Object[][] payments = getPaymentHistory((Integer)invoice[0]);

            // Simulate PDF creation
            System.out.println("Generating PDF for invoice " + invoiceId + " to " + outputPath);
            System.out.println("Invoice details: Guest " + invoice[2] + ", Room " + invoice[3]);
            System.out.println("Check-in: " + invoice[4] + ", Check-out: " + invoice[5]);
            System.out.println("Room charges: $" + invoice[6]);
            System.out.println("Additional charges: $" + invoice[7]);
            System.out.println("Taxes: $" + invoice[8]);
            System.out.println("Total amount: $" + invoice[9]);
            System.out.println("Status: " + invoice[10]);

            // In a real implementation, this would create an actual PDF file

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error generating PDF: " + e.getMessage(),
                    "PDF Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Close the database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}