## Database Setup Instructions

1. Install MySQL 8.0 or higher
2. Run the setup scripts:
      ```
         mysql -u root -p
      ```
     ```
         USE hotel_management
         source hotel_management_db_setup.sql
         source hotel_management_stored_procedures.sql
      ```

3. To populate the reservation section:
     ```
      USE hotel_management
      source mock_data.sql
    ```
4. Configure database connection:
    - Create a folder named `config` in the project root
    - Create a file named `database.properties` with content:
      ```
      url=jdbc:mysql://localhost:3306/hotel_management
      username=root
      password=YourDatabasePassword
      ```
    - Replace `YourDatabasePassword` with your actual MySQL password
