-- Insert mock guests
INSERT INTO guests (first_name, last_name, email, phone, address) 
VALUES 
('Sarah', 'Johnson', 'sarah.j@example.com', '555-6789', '123 Maple St, Boston, MA'),
('Michael', 'Chen', 'mchen@example.com', '555-3456', '45 Oak Ave, New York, NY'),
('Emma', 'Rodriguez', 'emma.r@example.com', '555-7890', '78 Pine Rd, Miami, FL'),
('David', 'Williams', 'dwilliams@example.com', '555-2345', '92 Cedar Ln, Chicago, IL'),
('Olivia', 'Taylor', 'otaylor@example.com', '555-8901', '56 Birch Dr, Seattle, WA');

-- Insert mock reservations
INSERT INTO reservations (guest_id, check_in_date, check_out_date, status, total_guests, special_requests, created_by)
VALUES 
(2, '2025-04-10', '2025-04-14', 'Confirmed', 2, 'High floor room preferred', 1),
(3, '2025-04-12', '2025-04-15', 'Confirmed', 3, 'Extra pillows', 1),
(4, '2025-04-15', '2025-04-17', 'Confirmed', 1, 'Early check-in requested', 1),
(5, '2025-04-18', '2025-04-22', 'Confirmed', 4, 'Connecting rooms if possible', 1),
(6, '2025-04-20', '2025-04-25', 'Confirmed', 2, 'Anniversary celebration - any special touches appreciated', 1);

-- Assign rooms to reservations
INSERT INTO reservation_rooms (reservation_id, room_id, rate_per_night)
VALUES 
(2, 2, 100.00),
(3, 3, 100.00),
(4, 4, 150.00),
(5, 5, 250.00),
(6, 6, 200.00);

-- Insert additional test data for hotel management system

-- Insert additional room types if needed
INSERT INTO room_types (name, description, base_price, capacity, amenities)
VALUES 
('Executive', 'Premium room with office space', 300.00, 2, 'WiFi, TV, Office Desk, Mini Bar, King Bed'),
('Penthouse', 'Luxury suite with panoramic views', 500.00, 4, 'WiFi, Smart TV, Kitchen, Jacuzzi, Balcony');

-- Insert more rooms
INSERT INTO rooms (room_number, type_id, floor, status, last_cleaned)
VALUES 
('201', 1, 2, 'Available', NOW()),
('202', 1, 2, 'Available', NOW()),
('203', 1, 2, 'Available', NOW()),
('301', 2, 3, 'Available', NOW()),
('302', 2, 3, 'Available', NOW()),
('401', 3, 4, 'Available', NOW()),
('501', 4, 5, 'Available', NOW()),
('601', 5, 6, 'Available', NOW());

-- Insert inventory categories
INSERT INTO inventory_categories (name, description) 
VALUES 
('Toiletries', 'Bathroom supplies'),
('Linens', 'Bed sheets, towels, etc.'),
('Cleaning', 'Cleaning supplies'),
('Food', 'Food and beverage items'),
('Office', 'Office supplies');

-- Insert inventory items
INSERT INTO inventory_items (category_id, name, description, unit, current_quantity, min_quantity, cost_per_unit, supplier)
VALUES 
(1, 'Shampoo', 'Hotel branded shampoo', 'bottle', 200, 50, 1.50, 'Supply Co'),
(1, 'Conditioner', 'Hotel branded conditioner', 'bottle', 200, 50, 1.50, 'Supply Co'),
(1, 'Bath Soap', 'Luxury bath soap', 'bar', 300, 75, 0.75, 'Supply Co'),
(2, 'Bath Towels', 'Standard bath towels', 'piece', 150, 50, 5.00, 'Linen Supply'),
(2, 'Hand Towels', 'Standard hand towels', 'piece', 200, 50, 3.00, 'Linen Supply'),
(2, 'Bed Sheets', 'Queen size sheets', 'set', 100, 30, 20.00, 'Linen Supply'),
(3, 'All-Purpose Cleaner', 'General cleaner', 'bottle', 50, 10, 3.25, 'Clean Co'),
(3, 'Glass Cleaner', 'For windows and mirrors', 'bottle', 40, 10, 2.75, 'Clean Co'),
(4, 'Coffee', 'Premium coffee blend', 'kg', 25, 5, 12.00, 'Food Supply Inc'),
(5, 'Pens', 'Hotel branded pens', 'box', 30, 5, 10.00, 'Office Pro');

-- Insert services
INSERT INTO services (name, description, price, category)
VALUES 
('Room Service', 'Food delivery to room', 10.00, 'Food'),
('Laundry Service', 'Clothes washing and ironing', 25.00, 'Housekeeping'),
('Airport Shuttle', 'Transportation to/from airport', 30.00, 'Transportation'),
('Spa Treatment', 'Full body massage', 75.00, 'Wellness'),
('Late Checkout', 'Extended checkout time', 50.00, 'Room');

-- Insert additional guests
INSERT INTO guests (first_name, last_name, email, phone, address)
VALUES 
('James', 'Wilson', 'jwilson@example.com', '555-4567', '123 Pine St, Austin, TX'),
('Jennifer', 'Garcia', 'jgarcia@example.com', '555-5678', '456 Oak Ave, Portland, OR'),
('Robert', 'Martinez', 'rmartinez@example.com', '555-6789', '789 Elm Rd, San Francisco, CA'),
('Patricia', 'Anderson', 'panderson@example.com', '555-7890', '321 Maple Dr, Denver, CO'),
('Thomas', 'Thomas', 'tthomas@example.com', '555-8901', '654 Cedar Ln, Nashville, TN');

-- Insert reservations spanning different dates
INSERT INTO reservations (guest_id, check_in_date, check_out_date, status, total_guests, special_requests, created_by)
VALUES 
(2, '2025-04-25', '2025-04-28', 'Confirmed', 2, 'Non-smoking room preferred', 1),
(3, '2025-05-01', '2025-05-05', 'Confirmed', 1, 'High floor requested', 1),
(4, '2025-05-10', '2025-05-12', 'Confirmed', 2, 'Early check-in if possible', 1),
(5, '2025-05-15', '2025-05-20', 'Confirmed', 3, 'Crib for infant needed', 1),
(6, '2025-05-22', '2025-05-23', 'Confirmed', 2, 'Celebrating anniversary', 1),
(7, '2025-06-01', '2025-06-05', 'Confirmed', 4, 'Near elevator', 1);

-- Assign rooms to reservations
INSERT INTO reservation_rooms (reservation_id, room_id, rate_per_night)
VALUES 
(2, 3, 100.00),
(3, 4, 150.00),
(4, 5, 150.00),
(5, 6, 250.00),
(6, 7, 300.00),
(7, 8, 500.00);

-- Create some bills
INSERT INTO billing (reservation_id, total_amount, tax_amount, grand_total, payment_status, created_by)
VALUES 
(1, 100.00, 10.00, 110.00, 'Paid', 1),
(2, 300.00, 30.00, 330.00, 'Paid', 1),
(3, 600.00, 60.00, 660.00, 'Pending', 1);

-- Add bill items
INSERT INTO bill_items (bill_id, item_type, description, quantity, unit_price, total_price)
VALUES 
(1, 'Room', 'Standard Room', 1, 100.00, 100.00),
(2, 'Room', 'Standard Room', 3, 100.00, 300.00),
(3, 'Room', 'Deluxe Room', 4, 150.00, 600.00);

-- Create housekeeping tasks
INSERT INTO housekeeping_tasks (room_id, task_type, status, scheduled_date, created_by)
VALUES 
(1, 'Regular Cleaning', 'Pending', NOW(), 1),
(2, 'Deep Cleaning', 'Pending', NOW(), 1),
(3, 'Maintenance', 'Pending', NOW(), 1);