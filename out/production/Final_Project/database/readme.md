## Database Setup Instructions

1. Install MySQL 8.0 or higher
2. Run the setup scripts:
    - hotel_management_db_setup.sql
    - hotel_management_stored_procedures.sql
3. Configure database connection:
    - Create a folder named `config` in the project root
    - Create a file named `database.properties` with content:
      ```
      url=jdbc:mysql://localhost:3306/hotel_management
      username=root
      password=YourDatabasePassword
      ```
    - Replace `YourDatabasePassword` with your actual MySQL password
