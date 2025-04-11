package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Connector class between Inventory UI and database
 */
public class InventoryUIConnector {
    private Connection connection;

    /**
     * Constructor
     */
    public InventoryUIConnector() {
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
     * Get column names for inventory table
     */
    public String[] getInventoryTableColumns() {
        return new String[] {
                "Item ID", "Name", "Category", "Quantity", "Unit Cost", "Total Value", "Supplier", "Last Ordered", "Reorder Level"
        };
    }

    /**
     * Create tables if not exists
     */
    public void createTablesIfNotExist() {
        Statement stmt = null;

        try {
            stmt = connection.createStatement();

            // Create Inventory table
            String createInventoryTable = "CREATE TABLE IF NOT EXISTS inventory ("
                    + "item_id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "name VARCHAR(100) NOT NULL,"
                    + "category VARCHAR(50) NOT NULL,"
                    + "quantity INT NOT NULL DEFAULT 0,"
                    + "unit_cost DOUBLE NOT NULL,"
                    + "supplier VARCHAR(100),"
                    + "last_ordered DATE,"
                    + "reorder_level INT NOT NULL DEFAULT 10"
                    + ")";
            stmt.executeUpdate(createInventoryTable);

            // Create Inventory Log table for tracking usage
            String createInventoryLogTable = "CREATE TABLE IF NOT EXISTS inventory_log ("
                    + "log_id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "item_id INT NOT NULL,"
                    + "change_amount INT NOT NULL,"
                    + "change_date DATE NOT NULL,"
                    + "notes TEXT,"
                    + "FOREIGN KEY (item_id) REFERENCES inventory(item_id)"
                    + ")";
            stmt.executeUpdate(createInventoryLogTable);

            // Create Categories reference table
            String createCategoriesTable = "CREATE TABLE IF NOT EXISTS inventory_categories ("
                    + "category_id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "category_name VARCHAR(50) NOT NULL UNIQUE"
                    + ")";
            stmt.executeUpdate(createCategoriesTable);

            // Insert default categories if empty
            String checkCategories = "SELECT COUNT(*) FROM inventory_categories";
            ResultSet rs = stmt.executeQuery(checkCategories);
            if (rs.next() && rs.getInt(1) == 0) {
                String insertCategories = "INSERT INTO inventory_categories (category_name) VALUES "
                        + "('Toiletries'),"
                        + "('Linens'),"
                        + "('Cleaning Supplies'),"
                        + "('Office Supplies'),"
                        + "('Kitchen Supplies'),"
                        + "('Maintenance'),"
                        + "('Food and Beverage')";
                stmt.executeUpdate(insertCategories);
            }
            rs.close();

            // Create Suppliers reference table
            String createSuppliersTable = "CREATE TABLE IF NOT EXISTS suppliers ("
                    + "supplier_id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "supplier_name VARCHAR(100) NOT NULL UNIQUE,"
                    + "contact_name VARCHAR(100),"
                    + "phone VARCHAR(20),"
                    + "email VARCHAR(100),"
                    + "address TEXT"
                    + ")";
            stmt.executeUpdate(createSuppliersTable);

            // Insert default suppliers if empty
            String checkSuppliers = "SELECT COUNT(*) FROM suppliers";
            rs = stmt.executeQuery(checkSuppliers);
            if (rs.next() && rs.getInt(1) == 0) {
                String insertSuppliers = "INSERT INTO suppliers (supplier_name, contact_name, phone) VALUES "
                        + "('Hotel Supplies Inc.', 'John Smith', '555-1234'),"
                        + "('Cleaner World', 'Jane Doe', '555-5678'),"
                        + "('Office Depot', 'Customer Service', '555-9012'),"
                        + "('Food Wholesalers', 'Mike Johnson', '555-3456')";
                stmt.executeUpdate(insertSuppliers);
            }
            rs.close();
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
     * Get inventory data for table display
     */
    public Object[][] getInventoryTableData() {
        List<Object[]> data = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT * FROM inventory ORDER BY name";
            stmt = connection.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                int quantity = rs.getInt("quantity");
                double unitCost = rs.getDouble("unit_cost");
                double totalValue = quantity * unitCost;

                Object[] row = {
                        rs.getInt("item_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        quantity,
                        unitCost,
                        totalValue, // Calculated field
                        rs.getString("supplier"),
                        rs.getDate("last_ordered"),
                        rs.getInt("reorder_level")
                };
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error retrieving inventory data: " + e.getMessage(),
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
     * Add new inventory item
     */
    public boolean addItem(String name, String category, int quantity, double unitCost,
                           String supplier, int reorderLevel) {
        PreparedStatement stmt = null;

        try {
            String sql = "INSERT INTO inventory (name, category, quantity, unit_cost, supplier, last_ordered, reorder_level) "
                    + "VALUES (?, ?, ?, ?, ?, CURRENT_DATE, ?)";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, category);
            stmt.setInt(3, quantity);
            stmt.setDouble(4, unitCost);
            stmt.setString(5, supplier);
            stmt.setInt(6, reorderLevel);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0 && quantity > 0) {
                // Log the initial stock addition
                int itemId = 0;
                PreparedStatement idStmt = connection.prepareStatement("SELECT LAST_INSERT_ID()");
                ResultSet rs = idStmt.executeQuery();
                if (rs.next()) {
                    itemId = rs.getInt(1);
                }
                rs.close();
                idStmt.close();

                String logSql = "INSERT INTO inventory_log (item_id, change_amount, change_date, notes) "
                        + "VALUES (?, ?, CURRENT_DATE, 'Initial stock')";
                PreparedStatement logStmt = connection.prepareStatement(logSql);
                logStmt.setInt(1, itemId);
                logStmt.setInt(2, quantity);
                logStmt.executeUpdate();
                logStmt.close();
            }

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error adding inventory item: " + e.getMessage(),
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
     * Update inventory item
     */
    public boolean updateItem(int itemId, String name, String category, int quantity,
                              double unitCost, String supplier, int reorderLevel) {
        PreparedStatement stmt = null;

        try {
            // First get current quantity to track change
            int currentQuantity = 0;
            PreparedStatement getStmt = connection.prepareStatement(
                    "SELECT quantity FROM inventory WHERE item_id = ?");
            getStmt.setInt(1, itemId);
            ResultSet rs = getStmt.executeQuery();
            if (rs.next()) {
                currentQuantity = rs.getInt("quantity");
            }
            rs.close();
            getStmt.close();

            // Update the item
            String sql = "UPDATE inventory SET name = ?, category = ?, quantity = ?, "
                    + "unit_cost = ?, supplier = ?, reorder_level = ? "
                    + "WHERE item_id = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, category);
            stmt.setInt(3, quantity);
            stmt.setDouble(4, unitCost);
            stmt.setString(5, supplier);
            stmt.setInt(6, reorderLevel);
            stmt.setInt(7, itemId);

            int rowsAffected = stmt.executeUpdate();

            // Log the quantity change if different
            if (rowsAffected > 0 && quantity != currentQuantity) {
                int change = quantity - currentQuantity;
                String notes = change > 0 ? "Stock adjustment (increase)" : "Stock adjustment (decrease)";

                String logSql = "INSERT INTO inventory_log (item_id, change_amount, change_date, notes) "
                        + "VALUES (?, ?, CURRENT_DATE, ?)";
                PreparedStatement logStmt = connection.prepareStatement(logSql);
                logStmt.setInt(1, itemId);
                logStmt.setInt(2, change);
                logStmt.setString(3, notes);
                logStmt.executeUpdate();
                logStmt.close();
            }

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error updating inventory item: " + e.getMessage(),
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
     * Delete inventory item
     */
    public boolean deleteItem(int itemId) {
        PreparedStatement stmt = null;

        try {
            // First delete related inventory log entries
            String deleteLogSql = "DELETE FROM inventory_log WHERE item_id = ?";
            PreparedStatement deleteLogStmt = connection.prepareStatement(deleteLogSql);
            deleteLogStmt.setInt(1, itemId);
            deleteLogStmt.executeUpdate();
            deleteLogStmt.close();

            // Then delete the inventory item
            String sql = "DELETE FROM inventory WHERE item_id = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, itemId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error deleting inventory item: " + e.getMessage(),
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
     * Update inventory quantity (add or subtract)
     */
    public boolean updateQuantity(int itemId, int quantityChange, String notes) {
        PreparedStatement stmt = null;

        try {
            // First check current quantity
            int currentQuantity = 0;
            PreparedStatement getStmt = connection.prepareStatement(
                    "SELECT quantity FROM inventory WHERE item_id = ?");
            getStmt.setInt(1, itemId);
            ResultSet rs = getStmt.executeQuery();
            if (rs.next()) {
                currentQuantity = rs.getInt("quantity");
            }
            rs.close();
            getStmt.close();

            // Ensure quantity doesn't go below zero
            int newQuantity = currentQuantity + quantityChange;
            if (newQuantity < 0) {
                JOptionPane.showMessageDialog(null,
                        "Error: Quantity cannot be negative.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Update the quantity
            String sql = "UPDATE inventory SET quantity = ? WHERE item_id = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, itemId);

            int rowsAffected = stmt.executeUpdate();

            // Log the change
            if (rowsAffected > 0) {
                String logSql = "INSERT INTO inventory_log (item_id, change_amount, change_date, notes) "
                        + "VALUES (?, ?, CURRENT_DATE, ?)";
                PreparedStatement logStmt = connection.prepareStatement(logSql);
                logStmt.setInt(1, itemId);
                logStmt.setInt(2, quantityChange);
                logStmt.setString(3, notes);
                logStmt.executeUpdate();
                logStmt.close();

                // Update last_ordered date if this is an addition
                if (quantityChange > 0) {
                    String updateDateSql = "UPDATE inventory SET last_ordered = CURRENT_DATE WHERE item_id = ?";
                    PreparedStatement updateDateStmt = connection.prepareStatement(updateDateSql);
                    updateDateStmt.setInt(1, itemId);
                    updateDateStmt.executeUpdate();
                    updateDateStmt.close();
                }
            }

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error updating inventory quantity: " + e.getMessage(),
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
     * Get inventory items that need to be reordered
     */
    public Object[][] getItemsNeedingReorder() {
        List<Object[]> data = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT * FROM inventory WHERE quantity <= reorder_level ORDER BY name";
            stmt = connection.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                int quantity = rs.getInt("quantity");
                double unitCost = rs.getDouble("unit_cost");
                double totalValue = quantity * unitCost;

                Object[] row = {
                        rs.getInt("item_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        quantity,
                        unitCost,
                        totalValue, // Calculated field
                        rs.getString("supplier"),
                        rs.getDate("last_ordered"),
                        rs.getInt("reorder_level")
                };
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error retrieving inventory items: " + e.getMessage(),
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
     * Get inventory items filtered by category
     */
    public Object[][] getFilteredItems(String category) {
        List<Object[]> data = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql;
            if (category != null && !category.isEmpty() && !category.equals("All Categories")) {
                sql = "SELECT * FROM inventory WHERE category = ? ORDER BY name";
                stmt = connection.prepareStatement(sql);
                stmt.setString(1, category);
            } else {
                sql = "SELECT * FROM inventory ORDER BY name";
                stmt = connection.prepareStatement(sql);
            }

            rs = stmt.executeQuery();

            while (rs.next()) {
                int quantity = rs.getInt("quantity");
                double unitCost = rs.getDouble("unit_cost");
                double totalValue = quantity * unitCost;

                Object[] row = {
                        rs.getInt("item_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        quantity,
                        unitCost,
                        totalValue, // Calculated field
                        rs.getString("supplier"),
                        rs.getDate("last_ordered"),
                        rs.getInt("reorder_level")
                };
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error retrieving filtered inventory: " + e.getMessage(),
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
     * Get inventory categories for dropdown
     */
    public void populateCategoryComboBox(JComboBox<String> comboBox) {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            comboBox.removeAllItems();
            comboBox.addItem("All Categories");

            String sql = "SELECT category_name FROM inventory_categories ORDER BY category_name";
            stmt = connection.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                comboBox.addItem(rs.getString("category_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error retrieving categories: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get suppliers for dropdown
     */
    public void populateSupplierComboBox(JComboBox<String> comboBox) {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            comboBox.removeAllItems();

            String sql = "SELECT supplier_name FROM suppliers ORDER BY supplier_name";
            stmt = connection.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                comboBox.addItem(rs.getString("supplier_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error retrieving suppliers: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get inventory usage history for a specific item
     */
    public Object[][] getInventoryUsageHistory(int itemId) {
        List<Object[]> data = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT log_id, change_amount, change_date, notes FROM inventory_log " +
                    "WHERE item_id = ? ORDER BY change_date DESC, log_id DESC";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, itemId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("log_id"),
                        rs.getInt("change_amount"),
                        rs.getDate("change_date"),
                        rs.getString("notes")
                };
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error retrieving inventory usage history: " + e.getMessage(),
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
     * Get a specific inventory item by ID
     */
    public Object[] getInventoryItem(int itemId) {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT * FROM inventory WHERE item_id = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, itemId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int quantity = rs.getInt("quantity");
                double unitCost = rs.getDouble("unit_cost");
                double totalValue = quantity * unitCost;

                return new Object[] {
                        rs.getInt("item_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        quantity,
                        unitCost,
                        totalValue, // Calculated field
                        rs.getString("supplier"),
                        rs.getDate("last_ordered"),
                        rs.getInt("reorder_level")
                };
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error retrieving inventory item: " + e.getMessage(),
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