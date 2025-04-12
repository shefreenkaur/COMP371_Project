package ui;

import database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RoomUIConnector {
    private static final Logger LOGGER = Logger.getLogger(RoomUIConnector.class.getName());
    private final Connection connection;

    public RoomUIConnector() {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    public String[] getRoomsTableColumns() {
        return new String[] {
                "Room Number", "Type", "Status", "Floor", "Price", "Capacity", "Last Cleaned"
        };
    }

    public Object[][] getRoomsTableData() {
        List<Object[]> data = new ArrayList<>();
        Statement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT r.room_number, rt.name AS room_type, r.status, r.floor, " +
                    "rt.base_price, rt.capacity, r.last_cleaned " +
                    "FROM rooms r " +
                    "JOIN room_types rt ON r.type_id = rt.type_id " +
                    "ORDER BY r.room_number";

            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Object[] row = {
                        rs.getString("room_number"),
                        rs.getString("room_type"),
                        rs.getString("status"),
                        rs.getInt("floor"),
                        rs.getDouble("base_price"),
                        rs.getInt("capacity"),
                        rs.getDate("last_cleaned")
                };
                data.add(row);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving room data", e);
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

    public boolean updateRoomStatus(String roomNumber, String status) {
        PreparedStatement pstmt = null;

        try {
            // First get room ID
            int roomId = getRoomId(roomNumber);
            if (roomId == -1) {
                return false;
            }

            CallableStatement cstmt = connection.prepareCall("{CALL sp_update_room_status(?, ?)}");
            cstmt.setInt(1, roomId);
            cstmt.setString(2, status);

            cstmt.execute();
            cstmt.close();

            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating room status", e);
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing statement", e);
            }
        }
    }

    private int getRoomId(String roomNumber) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT room_id FROM rooms WHERE room_number = ?";
            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, roomNumber);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("room_id");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting room ID", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing resources", e);
            }
        }

        return -1;
    }

    public String[] getRoomStatuses() {
        return new String[] {
                "Available", "Occupied", "Maintenance", "Cleaning"
        };
    }
}
