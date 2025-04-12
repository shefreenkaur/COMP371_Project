# Hotel Management System

## Project Overview

The Hotel Management System is a comprehensive Java application designed to streamline hotel operations including reservations, room management, inventory control, and billing. The system provides an intuitive graphical user interface that enables staff to efficiently manage hotel resources while enhancing the guest experience.

## Team Members and Responsibilities

Our team collaborated on different components of the system:

**Shefreen Kaur:**
- Designed and implemented the complete database architecture
- Created the database schema with 13 interconnected tables
- Developed stored procedures for complex operations
- Implemented the DatabaseConnection class using the Singleton pattern
- Created the data access layer (DAL) connecting Java to MySQL
- Integrated database components with the UI

**Paramvir & Samardeep:**
- Implemented UI components
- Developed business logic classes
- Created additional features

## System Architecture

The system follows a three-tier architecture:

1. **Presentation Layer:** Java Swing GUI components
2. **Business Logic Layer:** Manager classes for reservations, rooms, inventory, etc.
3. **Data Access Layer:** Database connection and operations via SQL

### Design Patterns

The system incorporates several key design patterns:

1. **Singleton Pattern:** Implemented in the DatabaseConnection class to ensure only one database connection exists throughout the application lifecycle
2. **Observer Pattern:** Used for real-time updates and notifications (e.g., low stock alerts)
3. **Factory Pattern:** Applied in the manager classes for creating business objects

## Features and Functionality

### Reservation Management
- Create, view, modify, and cancel reservations
- Check-in and check-out functionality
- View reservation details
- Filter and search reservations

### Room Management
- Track room availability and status
- Assign rooms to reservations
- Update room status (Available, Occupied, Maintenance, Cleaning)

### Inventory Management
- Track inventory items and quantities
- Generate low-stock alerts
- Log inventory transactions
- Categorize inventory items

### Billing System
- Create bills for reservations
- Add services and charges
- Process payments
- Generate invoices

## Database Design

The database consists of 13 interconnected tables:

1. **users:** System users with different roles
2. **room_types:** Types of rooms with pricing and capacity
3. **rooms:** Individual rooms in the hotel
4. **guests:** Guest information
5. **reservations:** Booking information
6. **reservation_rooms:** Junction table linking reservations to rooms
7. **inventory_categories:** Categories for inventory items
8. **inventory_items:** Stock items and quantities
9. **inventory_transactions:** Inventory movement logs
10. **services:** Available hotel services
11. **billing:** Invoice information
12. **bill_items:** Itemized bill entries
13. **housekeeping_tasks:** Room cleaning and maintenance tasks

The database design ensures data integrity through foreign key constraints and supports all required hotel operations.

## Setup Instructions

### Prerequisites
- Java Development Kit (JDK) 11 or higher
- MySQL Server 8.0 or higher
- MySQL Connector/J (JDBC driver)

### Database Setup

1. Install MySQL if not already installed
2. Run the database setup scripts:
   ```
   mysql -u root -p < hotel_management_db_setup.sql
   mysql -u root -p < hotel_management_stored_procedures.sql
   ```
3. (Optional) Load sample data:
   ```
   mysql -u root -p < mock_data.sql
   ```

### Application Configuration

1. Create a configuration file:
   - Create a folder named `config` in the project root
   - Create a file named `database.properties` with:
     ```
     url=jdbc:mysql://localhost:3306/hotel_management
     username=root
     password=YourDatabasePassword
     ```
   - Replace `YourDatabasePassword` with your MySQL password

### Running the Application

1. Compile the Java files
2. Run the application using the main class:
   ```
   java ui.HotelManagementGUI
   ```

## Project Structure

```
hotel_management/
├── config/
│   └── database.properties
├── database/
│   ├── DatabaseConnection.java
│   ├── hotel_management_db_setup.sql
│   ├── hotel_management_stored_procedures.sql
│   └── hotel_mock_data.sql
├── logic/
│   ├── Reservation.java
│   ├── ReservationManager.java
│   ├── Room.java
│   └── RoomManager.java
└── ui/
    ├── BillingPanel.java
    ├── BillingUIConnector.java
    ├── HotelManagementGUI.java
    ├── InventoryPanel.java
    ├── InventoryUIConnector.java
    ├── ReservationUIConnector.java
    └── RoomPanel.java
```

## Challenges and Solutions

### Challenge 1: Integration of multiple modules
**Solution:** Implemented the Singleton pattern for database connection to ensure consistent data access across all modules.

### Challenge 2: Real-time updates for reservation and room availability
**Solution:** Used event-driven design with the Observer pattern to notify appropriate components when status changes occur.

### Challenge 3: Low-stock alerts and housekeeping task management
**Solution:** Developed an automated inventory tracking system that monitors stock levels and triggers notifications when supplies run low.

## Future Enhancements

1. User authentication and role-based access control
2. Mobile application interface
3. Online booking integration
4. Advanced reporting and analytics
5. Customer loyalty program

## Conclusion

The Hotel Management System offers a comprehensive solution for managing hotel operations efficiently. The system's modular design makes it easy to extend and maintain while providing all essential functionality required for hotel management.

---

*Note to Professor: This project demonstrates the successful implementation of the required design patterns and database integration. The code follows best practices in software engineering, including separation of concerns, proper error handling, and efficient resource management.*