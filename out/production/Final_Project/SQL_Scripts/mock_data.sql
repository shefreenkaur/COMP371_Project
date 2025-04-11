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