package ui;

import database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InventoryUIConnector {
    private static final Logger LOGGER = Logger.getLogger(InventoryUIConnector.class.getName());
    private final Connection connection;

    public InventoryUIConnector() {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    public String[] getInventoryTableColumns() {
        return new String[] {
                "ID", "Name", "Category", "Quantity", "Min Quantity", "Unit", "Cost", "Supplier", "Last Restocked"
        };
    }

    public Object[][] getInventoryTableData() {
        List<Object[]> data = new ArrayList<>();
        Statement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT i.item_id, i.name, ic.name AS category, i.current_quantity, " +
                    "i.min_quantity, i.unit, i.cost_per_unit, i.supplier, i.last_restocked " +
                    "FROM inventory_items i " +
                    "JOIN inventory_categories ic ON i.category_id = ic.category_id " +
                    "ORDER BY i.name";

            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("item_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getInt("current_quantity"),
                        rs.getInt("min_quantity"),
                        rs.getString("unit"),
                        rs.getDouble("cost_per_unit"),
                        rs.getString("supplier"),
                        rs.getTimestamp("last_restocked")
                };
                data.add(row);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving inventory data", e);
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

    public Object[][] getLowStockItems() {
        List<Object[]> data = new ArrayList<>();
        CallableStatement cstmt = null;
        ResultSet rs = null;

        try {
            cstmt = connection.prepareCall("{CALL sp_get_low_stock_items()}");
            boolean hasResults = cstmt.execute();

            if (hasResults) {
                rs = cstmt.getResultSet();

                while (rs.next()) {
                    Object[] row = {
                            rs.getInt("item_id"),
                            rs.getString("name"),
                            rs.getString("category"),
                            rs.getInt("current_quantity"),
                            rs.getInt("min_quantity"),
                            rs.getString("unit"),
                            rs.getDouble("cost_per_unit"),
                            rs.getString("supplier")
                    };
                    data.add(row);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving low stock items", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (cstmt != null) cstmt.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing resources", e);
            }
        }

        return data.toArray(new Object[0][0]);
    }

    public boolean updateInventory(int itemId, int quantity, String transactionType, String notes) {
        CallableStatement cstmt = null;

        try {
            cstmt = connection.prepareCall("{CALL sp_update_inventory(?, ?, ?, ?, ?, ?, ?)}");
            cstmt.setInt(1, itemId);
            cstmt.setInt(2, quantity);
            cstmt.setString(3, transactionType);
            cstmt.setString(4, null); // related_to
            cstmt.setNull(5, Types.INTEGER); // related_id
            cstmt.setString(6, notes);
            cstmt.setInt(7, 1); // Default admin user

            cstmt.execute();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating inventory", e);
            return false;
        } finally {
            try {
                if (cstmt != null) cstmt.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing statement", e);
            }
        }
    }

    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        Statement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT name FROM inventory_categories ORDER BY name";
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                categories.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving categories", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing resources", e);
            }
        }

        return categories;
    }
}