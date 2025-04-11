-- Hotel Management System Database Setup Script
-- Created by: Shefreen

-- Create database
CREATE DATABASE IF NOT EXISTS hotel_management;
USE hotel_management;

-- Create users table
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('Admin', 'Receptionist', 'Housekeeping', 'Manager') NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create room_types table
CREATE TABLE room_types (
    type_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    base_price DECIMAL(10,2) NOT NULL,
    capacity INT NOT NULL,
    amenities TEXT
);

-- Create rooms table
CREATE TABLE rooms (
    room_id INT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(10) NOT NULL UNIQUE,
    type_id INT NOT NULL,
    floor INT NOT NULL,
    status ENUM('Available', 'Occupied', 'Maintenance', 'Cleaning') DEFAULT 'Available',
    last_cleaned DATETIME,
    notes TEXT,
    FOREIGN KEY (type_id) REFERENCES room_types(type_id)
);

-- Create guests table
CREATE TABLE guests (
    guest_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    address TEXT,
    id_type VARCHAR(50),
    id_number VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create reservations table
CREATE TABLE reservations (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    guest_id INT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    status ENUM('Confirmed', 'Checked-in', 'Checked-out', 'Cancelled', 'No-show') DEFAULT 'Confirmed',
    total_guests INT NOT NULL,
    special_requests TEXT,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (guest_id) REFERENCES guests(guest_id),
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

-- Create reservation_rooms junction table
CREATE TABLE reservation_rooms (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reservation_id INT NOT NULL,
    room_id INT NOT NULL,
    rate_per_night DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES rooms(room_id)
);

-- Create inventory_categories table
CREATE TABLE inventory_categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

-- Create inventory_items table
CREATE TABLE inventory_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    unit VARCHAR(20) NOT NULL,
    current_quantity INT NOT NULL DEFAULT 0,
    min_quantity INT NOT NULL DEFAULT 5,
    cost_per_unit DECIMAL(10,2),
    supplier VARCHAR(100),
    last_restocked DATETIME,
    FOREIGN KEY (category_id) REFERENCES inventory_categories(category_id)
);

-- Create inventory_transactions table
CREATE TABLE inventory_transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    item_id INT NOT NULL,
    quantity INT NOT NULL,
    transaction_type ENUM('In', 'Out') NOT NULL,
    related_to VARCHAR(50),
    related_id INT,
    notes TEXT,
    performed_by INT,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES inventory_items(item_id),
    FOREIGN KEY (performed_by) REFERENCES users(user_id)
);

-- Create services table
CREATE TABLE services (
    service_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(50)
);

-- Create billing table
CREATE TABLE billing (
    bill_id INT AUTO_INCREMENT PRIMARY KEY,
    reservation_id INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    tax_amount DECIMAL(10,2) DEFAULT 0,
    grand_total DECIMAL(10,2) NOT NULL,
    payment_status ENUM('Pending', 'Partially Paid', 'Paid', 'Refunded') DEFAULT 'Pending',
    payment_method VARCHAR(50),
    billing_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    created_by INT,
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id),
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

-- Create bill_items table
CREATE TABLE bill_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    bill_id INT NOT NULL,
    item_type ENUM('Room', 'Service', 'Food', 'Other') NOT NULL,
    description VARCHAR(255) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    date_added TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (bill_id) REFERENCES billing(bill_id) ON DELETE CASCADE
);

-- Create housekeeping_tasks table
CREATE TABLE housekeeping_tasks (
    task_id INT AUTO_INCREMENT PRIMARY KEY,
    room_id INT NOT NULL,
    task_type ENUM('Regular Cleaning', 'Deep Cleaning', 'Maintenance', 'Inspection') NOT NULL,
    status ENUM('Pending', 'In Progress', 'Completed', 'Cancelled') DEFAULT 'Pending',
    assigned_to INT,
    scheduled_date DATETIME NOT NULL,
    completed_date DATETIME,
    notes TEXT,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES rooms(room_id),
    FOREIGN KEY (assigned_to) REFERENCES users(user_id),
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

-- ======= INSERT SAMPLE DATA =======

-- Insert sample room types
INSERT INTO room_types (name, description, base_price, capacity, amenities)
VALUES 
    ('Standard', 'Basic room with essential amenities', 100.00, 2, 'WiFi, TV, AC'),
    ('Deluxe', 'Spacious room with premium amenities', 150.00, 2, 'WiFi, TV, AC, Mini Bar, Premium View'),
    ('Suite', 'Luxury suite with separate living area', 250.00, 4, 'WiFi, TV, AC, Mini Bar, Jacuzzi, Kitchen'),
    ('Family', 'Large room suitable for families', 200.00, 6, 'WiFi, TV, AC, Extra Beds, Family Entertainment');

-- Insert sample rooms
INSERT INTO rooms (room_number, type_id, floor, status)
VALUES 
    ('101', 1, 1, 'Available'),
    ('102', 1, 1, 'Available'),
    ('103', 1, 1, 'Available'),
    ('201', 2, 2, 'Available'),
    ('202', 2, 2, 'Available'),
    ('301', 3, 3, 'Available'),
    ('302', 3, 3, 'Available'),
    ('401', 4, 4, 'Available');

-- Insert admin user
INSERT INTO users (username, password, full_name, role, email)
VALUES ('admin', 'password123', 'System Administrator', 'Admin', 'admin@hotel.com');

-- Insert inventory categories
INSERT INTO inventory_categories (name, description)
VALUES 
    ('Toiletries', 'Bathroom supplies like soap, shampoo, etc.'),
    ('Linens', 'Bed sheets, towels, pillowcases, etc.'),
    ('Cleaning', 'Cleaning supplies and equipment'),
    ('Food', 'Food items for restaurant and room service'),
    ('Office', 'Office supplies and stationery');

-- Insert sample inventory items
INSERT INTO inventory_items (category_id, name, description, unit, current_quantity, min_quantity, cost_per_unit)
VALUES 
    (1, 'Soap', 'Hotel branded soap', 'piece', 500, 100, 0.50),
    (1, 'Shampoo', 'Hotel branded shampoo', 'bottle', 400, 100, 1.00),
    (2, 'Bed Sheet', 'Queen size bed sheet', 'piece', 200, 50, 15.00),
    (2, 'Towel', 'Bath towel', 'piece', 300, 75, 10.00),
    (3, 'Disinfectant', 'Surface disinfectant', 'bottle', 50, 10, 5.00);

-- Insert sample services
INSERT INTO services (name, description, price, category)
VALUES 
    ('Room Service', 'Food delivered to room', 10.00, 'Food'),
    ('Laundry', 'Clothes washing and ironing', 20.00, 'Housekeeping'),
    ('Spa Treatment', 'Full body massage', 80.00, 'Wellness'),
    ('Airport Transfer', 'Transportation to/from airport', 50.00, 'Transport');