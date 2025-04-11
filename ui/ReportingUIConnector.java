package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Calendar;

/**
 * Connector class between Reporting UI and database
 */
public class ReportingUIConnector {
    private Connection connection;

    /**
     * Constructor
     */
    public ReportingUIConnector() {
        try {
            // Load JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Establish connection - adjust these parameters for your database
            connection = java.sql.DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/hotel_management", "root", "1234");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Database connection failed: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Create tables if not exists
     */
    public void createTablesIfNotExist() {
        // For the reporting module, we don't need to create additional tables
        // as it uses data from existing tables.
    }

    /**
     * Get column names for occupancy report
     */
    public String[] getOccupancyReportColumns() {
        return new String[] {
                "Date", "Total Rooms", "Occupied Rooms", "Occupancy Rate (%)", "Revenue"
        };
    }

    /**
     * Get column names for revenue report
     */
    public String[] getRevenueReportColumns() {
        return new String[] {
                "Date", "Room Revenue", "Additional Charges", "Taxes", "Total Revenue"
        };
    }

    /**
     * Get column names for room type popularity report
     */
    public String[] getRoomTypePopularityColumns() {
        return new String[] {
                "Room Type", "Bookings", "Nights", "Revenue"
        };
    }

    /**
     * Get column names for guest statistics report
     */
    public String[] getGuestStatisticsColumns() {
        return new String[] {
                "Guest Name", "Number of Stays", "Total Nights", "Total Spent", "Last Stay"
        };
    }

    /**
     * Get occupancy report data for a date range
     */
    public Object[][] getOccupancyReport(Date startDate, Date endDate) {
        List<Object[]> data = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // First get total number of rooms
            int totalRooms = 0;
            Statement roomStmt = connection.createStatement();
            ResultSet roomRs = roomStmt.executeQuery("SELECT COUNT(*) FROM rooms");
            if (roomRs.next()) {
                totalRooms = roomRs.getInt(1);
            }
            roomRs.close();
            roomStmt.close();

            // Generate date list
            List<Date> dates = generateDateList(startDate, endDate);

            // For each date, calculate occupancy
            for (Date date : dates) {
                // Get number of occupied rooms on this date
                String sql = "SELECT COUNT(*) FROM reservations "
                        + "WHERE check_in_date <= ? AND check_out_date > ? "
                        + "AND status IN ('Confirmed', 'Checked-in')";
                stmt = connection.prepareStatement(sql);
                stmt.setDate(1, date);
                stmt.setDate(2, date);
                rs = stmt.executeQuery();

                int occupiedRooms = 0;
                if (rs.next()) {
                    occupiedRooms = rs.getInt(1);
                }
                rs.close();
                stmt.close();

                // Calculate occupancy rate
                double occupancyRate = totalRooms > 0 ? (double) occupiedRooms / totalRooms * 100 : 0;

                // Get revenue for this date
                double revenue = 0;
                sql = "SELECT SUM(rm.rate) FROM reservations r "
                        + "JOIN rooms rm ON r.room_number = rm.room_number "
                        + "WHERE r.check_in_date <= ? AND r.check_out_date > ? "
                        + "AND r.status IN ('Confirmed', 'Checked-in')";
                stmt = connection.prepareStatement(sql);
                stmt.setDate(1, date);
                stmt.setDate(2, date);
                rs = stmt.executeQuery();

                if (rs.next() && rs.getObject(1) != null) {
                    revenue = rs.getDouble(1);
                }
                rs.close();
                stmt.close();

                // Add to report data
                Object[] row = {
                        date,
                        totalRooms,
                        occupiedRooms,
                        Math.round(occupancyRate * 10) / 10.0, // Round to 1 decimal
                        revenue
                };
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error generating occupancy report: " + e.getMessage(),
                    "Report Error", JOptionPane.ERROR_MESSAGE);
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
     * Get revenue report data for a date range
     */
    public Object[][] getRevenueReport(Date startDate, Date endDate) {
        List<Object[]> data = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Generate date list
            List<Date> dates = generateDateList(startDate, endDate);

            // For each date, calculate revenue
            for (Date date : dates) {
                double roomRevenue = 0;
                double additionalCharges = 0;
                double taxes = 0;

                // Get room revenue for this date
                String sql = "SELECT SUM(i.room_charges) "
                        + "FROM invoices i "
                        + "JOIN reservations r ON i.reservation_id = r.reservation_id "
                        + "WHERE r.check_in_date <= ? AND r.check_out_date > ?";
                stmt = connection.prepareStatement(sql);
                stmt.setDate(1, date);
                stmt.setDate(2, date);
                rs = stmt.executeQuery();

                if (rs.next() && rs.getObject(1) != null) {
                    roomRevenue = rs.getDouble(1);
                }
                rs.close();
                stmt.close();

                // Get additional charges for this date
                sql = "SELECT SUM(i.additional_charges) "
                        + "FROM invoices i "
                        + "JOIN reservations r ON i.reservation_id = r.reservation_id "
                        + "WHERE r.check_in_date <= ? AND r.check_out_date > ?";
                stmt = connection.prepareStatement(sql);
                stmt.setDate(1, date);
                stmt.setDate(2, date);
                rs = stmt.executeQuery();

                if (rs.next() && rs.getObject(1) != null) {
                    additionalCharges = rs.getDouble(1);
                }
                rs.close();
                stmt.close();

                // Get taxes for this date
                sql = "SELECT SUM(i.taxes) "
                        + "FROM invoices i "
                        + "JOIN reservations r ON i.reservation_id = r.reservation_id "
                        + "WHERE r.check_in_date <= ? AND r.check_out_date > ?";
                stmt = connection.prepareStatement(sql);
                stmt.setDate(1, date);
                stmt.setDate(2, date);
                rs = stmt.executeQuery();

                if (rs.next() && rs.getObject(1) != null) {
                    taxes = rs.getDouble(1);
                }
                rs.close();
                stmt.close();

                // Calculate total
                double totalRevenue = roomRevenue + additionalCharges + taxes;

                // Add to report data
                Object[] row = {
                        date,
                        roomRevenue,
                        additionalCharges,
                        taxes,
                        totalRevenue
                };
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error generating revenue report: " + e.getMessage(),
                    "Report Error", JOptionPane.ERROR_MESSAGE);
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
     * Get room type popularity report
     */
    public Object[][] getRoomTypePopularityReport(Date startDate, Date endDate) {
        List<Object[]> data = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT rm.room_type, COUNT(r.reservation_id) AS booking_count, "
                    + "SUM(DATEDIFF(r.check_out_date, r.check_in_date)) AS nights, "
                    + "COALESCE(SUM(i.room_charges), 0) AS revenue "
                    + "FROM reservations r "
                    + "JOIN rooms rm ON r.room_number = rm.room_number "
                    + "LEFT JOIN invoices i ON r.reservation_id = i.reservation_id "
                    + "WHERE r.check_in_date BETWEEN ? AND ? "
                    + "GROUP BY rm.room_type "
                    + "ORDER BY booking_count DESC";
            stmt = connection.prepareStatement(sql);
            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                        rs.getString("room_type"),
                        rs.getInt("booking_count"),
                        rs.getInt("nights"),
                        rs.getDouble("revenue")
                };
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error generating room popularity report: " + e.getMessage(),
                    "Report Error", JOptionPane.ERROR_MESSAGE);
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
     * Get guest statistics report
     */
    public Object[][] getGuestStatisticsReport(Date startDate, Date endDate) {
        List<Object[]> data = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT r.full_name, COUNT(r.reservation_id) AS stay_count, "
                    + "SUM(DATEDIFF(r.check_out_date, r.check_in_date)) AS total_nights, "
                    + "COALESCE(SUM(i.total_amount), 0) AS total_spent, "
                    + "MAX(r.check_in_date) AS last_stay "
                    + "FROM reservations r "
                    + "LEFT JOIN invoices i ON r.reservation_id = i.reservation_id "
                    + "WHERE r.check_in_date BETWEEN ? AND ? "
                    + "GROUP BY r.full_name "
                    + "ORDER BY total_spent DESC";
            stmt = connection.prepareStatement(sql);
            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                        rs.getString("full_name"),
                        rs.getInt("stay_count"),
                        rs.getInt("total_nights"),
                        rs.getDouble("total_spent"),
                        rs.getDate("last_stay")
                };
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error generating guest statistics report: " + e.getMessage(),
                    "Report Error", JOptionPane.ERROR_MESSAGE);
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
     * Get cancellation report data
     */
    public Object[][] getCancellationReport(Date startDate, Date endDate) {
        List<Object[]> data = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT r.check_in_date, r.full_name, rm.room_type, "
                    + "DATEDIFF(r.check_out_date, r.check_in_date) AS nights, "
                    + "DATEDIFF(r.check_out_date, r.check_in_date) * rm.rate AS lost_revenue "
                    + "FROM reservations r "
                    + "JOIN rooms rm ON r.room_number = rm.room_number "
                    + "WHERE r.status = 'Cancelled' "
                    + "AND r.check_in_date BETWEEN ? AND ? "
                    + "ORDER BY r.check_in_date";
            stmt = connection.prepareStatement(sql);
            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                        rs.getDate("check_in_date"),
                        rs.getString("full_name"),
                        rs.getString("room_type"),
                        rs.getInt("nights"),
                        rs.getDouble("lost_revenue")
                };
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error generating cancellation report: " + e.getMessage(),
                    "Report Error", JOptionPane.ERROR_MESSAGE);
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
     * Get monthly summary report data
     */
    public Object[][] getMonthlySummaryReport(int year) {
        List<Object[]> data = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Get total number of rooms
            int totalRooms = 0;
            Statement roomStmt = connection.createStatement();
            ResultSet roomRs = roomStmt.executeQuery("SELECT COUNT(*) FROM rooms");
            if (roomRs.next()) {
                totalRooms = roomRs.getInt(1);
            }
            roomRs.close();
            roomStmt.close();

            // Month names
            String[] months = {
                    "January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"
            };

            // For each month, get summary data
            for (int month = 1; month <= 12; month++) {
                // Get first and last day of month
                Calendar cal = Calendar.getInstance();
                cal.set(year, month - 1, 1); // Month is 0-based in Calendar
                Date firstDay = new Date(cal.getTimeInMillis());

                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                Date lastDay = new Date(cal.getTimeInMillis());

                // Days in month
                int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

                // Get bookings count
                String sql = "SELECT COUNT(*) FROM reservations "
                        + "WHERE YEAR(check_in_date) = ? AND MONTH(check_in_date) = ?";
                stmt = connection.prepareStatement(sql);
                stmt.setInt(1, year);
                stmt.setInt(2, month);
                rs = stmt.executeQuery();

                int bookingCount = 0;
                if (rs.next()) {
                    bookingCount = rs.getInt(1);
                }
                rs.close();
                stmt.close();

                // Get occupied room-nights
                sql = "SELECT SUM(DATEDIFF(LEAST(check_out_date, ?), GREATEST(check_in_date, ?))) "
                        + "FROM reservations "
                        + "WHERE (check_in_date BETWEEN ? AND ? OR check_out_date BETWEEN ? AND ?) "
                        + "AND status IN ('Confirmed', 'Checked-in', 'Checked-out')";
                stmt = connection.prepareStatement(sql);
                stmt.setDate(1, lastDay);
                stmt.setDate(2, firstDay);
                stmt.setDate(3, firstDay);
                stmt.setDate(4, lastDay);
                stmt.setDate(5, firstDay);
                stmt.setDate(6, lastDay);
                rs = stmt.executeQuery();

                int occupiedNights = 0;
                if (rs.next() && rs.getObject(1) != null) {
                    occupiedNights = rs.getInt(1);
                }
                rs.close();
                stmt.close();

                // Calculate occupancy rate
                double occupancyRate = 0;
                if (totalRooms > 0 && daysInMonth > 0) {
                    occupancyRate = (double) occupiedNights / (totalRooms * daysInMonth) * 100;
                }

                // Get room revenue
                double roomRevenue = 0;
                sql = "SELECT SUM(i.room_charges) FROM invoices i "
                        + "JOIN reservations r ON i.reservation_id = r.reservation_id "
                        + "WHERE YEAR(r.check_in_date) = ? AND MONTH(r.check_in_date) = ?";
                stmt = connection.prepareStatement(sql);
                stmt.setInt(1, year);
                stmt.setInt(2, month);
                rs = stmt.executeQuery();

                if (rs.next() && rs.getObject(1) != null) {
                    roomRevenue = rs.getDouble(1);
                }
                rs.close();
                stmt.close();

                // Get additional revenue
                double additionalRevenue = 0;
                sql = "SELECT SUM(i.additional_charges) FROM invoices i "
                        + "JOIN reservations r ON i.reservation_id = r.reservation_id "
                        + "WHERE YEAR(r.check_in_date) = ? AND MONTH(r.check_in_date) = ?";
                stmt = connection.prepareStatement(sql);
                stmt.setInt(1, year);
                stmt.setInt(2, month);
                rs = stmt.executeQuery();

                if (rs.next() && rs.getObject(1) != null) {
                    additionalRevenue = rs.getDouble(1);
                }
                rs.close();
                stmt.close();

                // Calculate total revenue
                double totalRevenue = roomRevenue + additionalRevenue;

                // Add to report data
                Object[] row = {
                        months[month - 1], // Month name
                        bookingCount,
                        Math.round(occupancyRate * 10) / 10.0, // Round to 1 decimal
                        roomRevenue,
                        additionalRevenue,
                        totalRevenue
                };
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error generating monthly summary report: " + e.getMessage(),
                    "Report Error", JOptionPane.ERROR_MESSAGE);
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
     * Get data for occupancy chart
     */
    public Map<String, Double> getOccupancyChartData(Date startDate, Date endDate) {
        Map<String, Double> chartData = new HashMap<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Get total number of rooms
            int totalRooms = 0;
            Statement roomStmt = connection.createStatement();
            ResultSet roomRs = roomStmt.executeQuery("SELECT COUNT(*) FROM rooms");
            if (roomRs.next()) {
                totalRooms = roomRs.getInt(1);
            }
            roomRs.close();
            roomStmt.close();

            // Generate date list
            List<Date> dates = generateDateList(startDate, endDate);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            // For each date, calculate occupancy
            for (Date date : dates) {
                // Get number of occupied rooms on this date
                String sql = "SELECT COUNT(*) FROM reservations "
                        + "WHERE check_in_date <= ? AND check_out_date > ? "
                        + "AND status IN ('Confirmed', 'Checked-in')";
                stmt = connection.prepareStatement(sql);
                stmt.setDate(1, date);
                stmt.setDate(2, date);
                rs = stmt.executeQuery();

                int occupiedRooms = 0;
                if (rs.next()) {
                    occupiedRooms = rs.getInt(1);
                }
                rs.close();
                stmt.close();

                // Calculate occupancy rate
                double occupancyRate = totalRooms > 0 ? (double) occupiedRooms / totalRooms * 100 : 0;

                // Add to chart data
                chartData.put(sdf.format(date), Math.round(occupancyRate * 10) / 10.0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error generating occupancy chart data: " + e.getMessage(),
                    "Chart Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return chartData;
    }

    /**
     * Get data for revenue chart
     */
    public Map<String, Double> getRevenueChartData(Date startDate, Date endDate) {
        Map<String, Double> chartData = new HashMap<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Generate date list
            List<Date> dates = generateDateList(startDate, endDate);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            // For each date, calculate revenue
            for (Date date : dates) {
                // Get total revenue for this date
                String sql = "SELECT SUM(i.total_amount) "
                        + "FROM invoices i "
                        + "JOIN reservations r ON i.reservation_id = r.reservation_id "
                        + "WHERE r.check_in_date <= ? AND r.check_out_date > ?";
                stmt = connection.prepareStatement(sql);
                stmt.setDate(1, date);
                stmt.setDate(2, date);
                rs = stmt.executeQuery();

                double revenue = 0;
                if (rs.next() && rs.getObject(1) != null) {
                    revenue = rs.getDouble(1);
                }
                rs.close();
                stmt.close();

                // Add to chart data
                chartData.put(sdf.format(date), revenue);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error generating revenue chart data: " + e.getMessage(),
                    "Chart Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return chartData;
    }

    /**
     * Get data for room type distribution chart
     */
    public Map<String, Integer> getRoomTypeDistributionChartData() {
        Map<String, Integer> chartData = new HashMap<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT room_type, COUNT(*) AS count FROM rooms GROUP BY room_type ORDER BY count DESC";
            stmt = connection.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                chartData.put(rs.getString("room_type"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error generating room type chart data: " + e.getMessage(),
                    "Chart Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return chartData;
    }

    /**
     * Get available years for reports from the reservation data
     */
    public List<Integer> getAvailableYears() {
        List<Integer> years = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT DISTINCT YEAR(check_in_date) AS year FROM reservations ORDER BY year DESC";
            stmt = connection.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                years.add(rs.getInt("year"));
            }

            // If no data, add current year
            if (years.isEmpty()) {
                Calendar cal = Calendar.getInstance();
                years.add(cal.get(Calendar.YEAR));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error retrieving available years: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return years;
    }

    /**
     * Export report to CSV file
     */
    public boolean exportReportToCSV(Object[][] data, String[] columns, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write header
            for (int i = 0; i < columns.length; i++) {
                writer.print(columns[i]);
                if (i < columns.length - 1) {
                    writer.print(",");
                }
            }
            writer.println();

            // Write data
            for (Object[] row : data) {
                for (int i = 0; i < row.length; i++) {
                    if (row[i] != null) {
                        String value = row[i].toString();
                        // Escape commas and quotes in the value
                        if (value.contains(",") || value.contains("\"")) {
                            value = "\"" + value.replace("\"", "\"\"") + "\"";
                        }
                        writer.print(value);
                    }
                    if (i < row.length - 1) {
                        writer.print(",");
                    }
                }
                writer.println();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error exporting to CSV: " + e.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Export report to PDF file (placeholder for actual implementation)
     */
    public boolean exportReportToPDF(Object[][] data, String[] columns, String title, String filePath) {
        try {
            // This would be implemented with a PDF library like iText or Apache PDFBox
            // For now, we'll just simulate PDF creation

            System.out.println("Exporting " + title + " to PDF at " + filePath);
            System.out.println("Report has " + columns.length + " columns and " + data.length + " rows");

            // Print header
            for (String column : columns) {
                System.out.print(column + "\t");
            }
            System.out.println();

            // Print first 5 rows of data (or all if less than 5)
            int rowsToPrint = Math.min(data.length, 5);
            for (int i = 0; i < rowsToPrint; i++) {
                for (Object cell : data[i]) {
                    System.out.print((cell != null ? cell.toString() : "null") + "\t");
                }
                System.out.println();
            }

            // In a real implementation, this would create an actual PDF file

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error exporting to PDF: " + e.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Generate a list of consecutive dates between startDate and endDate (inclusive)
     */
    private List<Date> generateDateList(Date startDate, Date endDate) {
        List<Date> dates = new ArrayList<>();

        // Convert SQL Date to Calendar
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);

        // Clear time portion
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        endCal.set(Calendar.HOUR_OF_DAY, 0);
        endCal.set(Calendar.MINUTE, 0);
        endCal.set(Calendar.SECOND, 0);
        endCal.set(Calendar.MILLISECOND, 0);

        // Add dates to list
        while (!cal.after(endCal)) {
            dates.add(new Date(cal.getTimeInMillis()));
            cal.add(Calendar.DATE, 1);
        }

        return dates;
    }

    /**
     * Get hotel performance summary
     */
    public Object[] getHotelPerformanceSummary(Date startDate, Date endDate) {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Calculate total days in period
            long diffInMillies = endDate.

                    getTime() - startDate.getTime();
            int days = (int) (diffInMillies / (1000 * 60 * 60 * 24)) + 1;

            // Get total number of rooms
            int totalRooms = 0;
            Statement roomStmt = connection.createStatement();
            ResultSet roomRs = roomStmt.executeQuery("SELECT COUNT(*) FROM rooms");
            if (roomRs.next()) {
                totalRooms = roomRs.getInt(1);
            }
            roomRs.close();
            roomStmt.close();

            // Get total reservations
            int totalReservations = 0;
            String sql = "SELECT COUNT(*) FROM reservations "
                    + "WHERE check_in_date BETWEEN ? AND ?";
            stmt = connection.prepareStatement(sql);
            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            rs = stmt.executeQuery();

            if (rs.next()) {
                totalReservations = rs.getInt(1);
            }
            rs.close();
            stmt.close();

            // Get cancellations
            int cancellations = 0;
            sql = "SELECT COUNT(*) FROM reservations "
                    + "WHERE status = 'Cancelled' AND check_in_date BETWEEN ? AND ?";
            stmt = connection.prepareStatement(sql);
            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            rs = stmt.executeQuery();

            if (rs.next()) {
                cancellations = rs.getInt(1);
            }
            rs.close();
            stmt.close();

            // Get total revenue
            double totalRevenue = 0;
            sql = "SELECT SUM(total_amount) FROM invoices i "
                    + "JOIN reservations r ON i.reservation_id = r.reservation_id "
                    + "WHERE r.check_in_date BETWEEN ? AND ?";
            stmt = connection.prepareStatement(sql);
            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            rs = stmt.executeQuery();

            if (rs.next() && rs.getObject(1) != null) {
                totalRevenue = rs.getDouble(1);
            }
            rs.close();
            stmt.close();

            // Get room nights
            int roomNights = 0;
            sql = "SELECT SUM(DATEDIFF(check_out_date, check_in_date)) FROM reservations "
                    + "WHERE status IN ('Confirmed', 'Checked-in', 'Checked-out') "
                    + "AND check_in_date BETWEEN ? AND ?";
            stmt = connection.prepareStatement(sql);
            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            rs = stmt.executeQuery();

            if (rs.next() && rs.getObject(1) != null) {
                roomNights = rs.getInt(1);
            }
            rs.close();
            stmt.close();

            // Calculate occupancy rate
            double occupancyRate = 0;
            if (totalRooms > 0 && days > 0) {
                occupancyRate = (double) roomNights / (totalRooms * days) * 100;
            }

            // Calculate average daily rate
            double adr = 0;
            if (roomNights > 0) {
                adr = totalRevenue / roomNights;
            }

            // Calculate RevPAR (Revenue Per Available Room)
            double revpar = 0;
            if (totalRooms > 0 && days > 0) {
                revpar = totalRevenue / (totalRooms * days);
            }

            // Return summary data
            return new Object[] {
                    totalReservations,
                    cancellations,
                    roomNights,
                    Math.round(occupancyRate * 10) / 10.0, // Round to 1 decimal
                    Math.round(adr * 100) / 100.0, // Round to 2 decimals
                    Math.round(revpar * 100) / 100.0, // Round to 2 decimals
                    totalRevenue
            };
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error generating performance summary: " + e.getMessage(),
                    "Report Error", JOptionPane.ERROR_MESSAGE);
            return new Object[] {0, 0, 0, 0.0, 0.0, 0.0, 0.0};
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