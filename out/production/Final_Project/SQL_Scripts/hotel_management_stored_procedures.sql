-- Hotel Management System - Stored Procedures
-- Created by: Shefreen
-- Date: April 7, 2025

USE hotel_management;

-- ======= RESERVATION PROCEDURES =======

-- Procedure: Create a new reservation
DELIMITER //
CREATE PROCEDURE sp_create_reservation(
    IN p_first_name VARCHAR(50),
    IN p_last_name VARCHAR(50),
    IN p_email VARCHAR(100),
    IN p_phone VARCHAR(20),
    IN p_check_in_date DATE,
    IN p_check_out_date DATE,
    IN p_total_guests INT,
    IN p_room_type INT,
    IN p_special_requests TEXT,
    IN p_created_by INT,
    OUT p_reservation_id INT
)
BEGIN
    DECLARE v_guest_id INT;
    DECLARE v_room_id INT;
    DECLARE v_rate DECIMAL(10,2);
    
    -- Start transaction
    START TRANSACTION;
    
    -- Check if guest exists, if not create new guest
    SELECT guest_id INTO v_guest_id FROM guests 
    WHERE email = p_email LIMIT 1;
    
    IF v_guest_id IS NULL THEN
        INSERT INTO guests (first_name, last_name, email, phone)
        VALUES (p_first_name, p_last_name, p_email, p_phone);
        
        SET v_guest_id = LAST_INSERT_ID();
    END IF;
    
    -- Create reservation
    INSERT INTO reservations (guest_id, check_in_date, check_out_date, status, total_guests, special_requests, created_by)
    VALUES (v_guest_id, p_check_in_date, p_check_out_date, 'Confirmed', p_total_guests, p_special_requests, p_created_by);
    
    SET p_reservation_id = LAST_INSERT_ID();
    
    -- Find available room of requested type
    SELECT r.room_id, rt.base_price INTO v_room_id, v_rate
    FROM rooms r
    JOIN room_types rt ON r.type_id = rt.type_id
    WHERE r.type_id = p_room_type
    AND r.status = 'Available'
    AND r.room_id NOT IN (
        SELECT rr.room_id
        FROM reservation_rooms rr
        JOIN reservations res ON rr.reservation_id = res.reservation_id
        WHERE (p_check_in_date BETWEEN res.check_in_date AND DATE_SUB(res.check_out_date, INTERVAL 1 DAY))
        OR (p_check_out_date BETWEEN DATE_ADD(res.check_in_date, INTERVAL 1 DAY) AND res.check_out_date)
        OR (res.check_in_date BETWEEN p_check_in_date AND DATE_SUB(p_check_out_date, INTERVAL 1 DAY))
        OR (res.check_out_date BETWEEN DATE_ADD(p_check_in_date, INTERVAL 1 DAY) AND p_check_out_date)
    )
    LIMIT 1;
    
    IF v_room_id IS NULL THEN
        -- No available room of requested type
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No available room of requested type for given dates';
    ELSE
        -- Assign room to reservation
        INSERT INTO reservation_rooms (reservation_id, room_id, rate_per_night)
        VALUES (p_reservation_id, v_room_id, v_rate);
        
        COMMIT;
    END IF;
END //
DELIMITER ;

-- Procedure: Update reservation status
DELIMITER //
CREATE PROCEDURE sp_update_reservation_status(
    IN p_reservation_id INT,
    IN p_status VARCHAR(20)
)
BEGIN
    UPDATE reservations
    SET status = p_status,
        updated_at = NOW()
    WHERE reservation_id = p_reservation_id;
    
    -- If checked-in, update room status to occupied
    IF p_status = 'Checked-in' THEN
        UPDATE rooms r
        JOIN reservation_rooms rr ON r.room_id = rr.room_id
        SET r.status = 'Occupied'
        WHERE rr.reservation_id = p_reservation_id;
    
    -- If checked-out, update room status to cleaning and create housekeeping task
    ELSEIF p_status = 'Checked-out' THEN
        UPDATE rooms r
        JOIN reservation_rooms rr ON r.room_id = rr.room_id
        SET r.status = 'Cleaning'
        WHERE rr.reservation_id = p_reservation_id;
        
        INSERT INTO housekeeping_tasks (room_id, task_type, status, scheduled_date, created_by)
        SELECT rr.room_id, 'Regular Cleaning', 'Pending', NOW(), 1
        FROM reservation_rooms rr
        WHERE rr.reservation_id = p_reservation_id;
    END IF;
END //
DELIMITER ;

-- Procedure: Cancel reservation
DELIMITER //
CREATE PROCEDURE sp_cancel_reservation(
    IN p_reservation_id INT
)
BEGIN
    UPDATE reservations
    SET status = 'Cancelled',
        updated_at = NOW()
    WHERE reservation_id = p_reservation_id;
END //
DELIMITER ;

-- Procedure: Get reservations by date range
DELIMITER //
CREATE PROCEDURE sp_get_reservations_by_date_range(
    IN p_start_date DATE,
    IN p_end_date DATE
)
BEGIN
    SELECT 
        r.reservation_id,
        CONCAT(g.first_name, ' ', g.last_name) AS guest_name,
        g.email,
        g.phone,
        r.check_in_date,
        r.check_out_date,
        r.status,
        r.total_guests,
        GROUP_CONCAT(rm.room_number) AS rooms,
        r.special_requests,
        r.created_at
    FROM 
        reservations r
    JOIN 
        guests g ON r.guest_id = g.guest_id
    JOIN 
        reservation_rooms rr ON r.reservation_id = rr.reservation_id
    JOIN 
        rooms rm ON rr.room_id = rm.room_id
    WHERE 
        (r.check_in_date BETWEEN p_start_date AND p_end_date)
        OR (r.check_out_date BETWEEN p_start_date AND p_end_date)
        OR (p_start_date BETWEEN r.check_in_date AND r.check_out_date)
    GROUP BY 
        r.reservation_id
    ORDER BY 
        r.check_in_date;
END //
DELIMITER ;

-- ======= ROOM MANAGEMENT PROCEDURES =======

-- Procedure: Get available rooms by date range and type
DELIMITER //
CREATE PROCEDURE sp_get_available_rooms(
    IN p_check_in_date DATE,
    IN p_check_out_date DATE,
    IN p_room_type_id INT
)
BEGIN
    SELECT 
        r.room_id,
        r.room_number,
        rt.name AS room_type,
        rt.base_price,
        rt.capacity,
        rt.amenities
    FROM 
        rooms r
    JOIN 
        room_types rt ON r.type_id = rt.type_id
    WHERE 
        r.status = 'Available'
        AND (p_room_type_id IS NULL OR r.type_id = p_room_type_id)
        AND r.room_id NOT IN (
            SELECT rr.room_id
            FROM reservation_rooms rr
            JOIN reservations res ON rr.reservation_id = res.reservation_id
            WHERE res.status IN ('Confirmed', 'Checked-in')
                AND (
                    (p_check_in_date BETWEEN res.check_in_date AND DATE_SUB(res.check_out_date, INTERVAL 1 DAY))
                    OR (p_check_out_date BETWEEN DATE_ADD(res.check_in_date, INTERVAL 1 DAY) AND res.check_out_date)
                    OR (res.check_in_date BETWEEN p_check_in_date AND DATE_SUB(p_check_out_date, INTERVAL 1 DAY))
                    OR (res.check_out_date BETWEEN DATE_ADD(p_check_in_date, INTERVAL 1 DAY) AND p_check_out_date)
                )
        );
END //
DELIMITER ;

-- Procedure: Update room status
DELIMITER //
CREATE PROCEDURE sp_update_room_status(
    IN p_room_id INT,
    IN p_status VARCHAR(20)
)
BEGIN
    UPDATE rooms
    SET status = p_status,
        last_cleaned = CASE WHEN p_status = 'Available' THEN NOW() ELSE last_cleaned END
    WHERE room_id = p_room_id;
END //
DELIMITER ;

-- ======= INVENTORY MANAGEMENT PROCEDURES =======

-- Procedure: Add inventory item
DELIMITER //
CREATE PROCEDURE sp_add_inventory_item(
    IN p_category_id INT,
    IN p_name VARCHAR(100),
    IN p_description TEXT,
    IN p_unit VARCHAR(20),
    IN p_current_quantity INT,
    IN p_min_quantity INT,
    IN p_cost_per_unit DECIMAL(10,2),
    IN p_supplier VARCHAR(100),
    OUT p_item_id INT
)
BEGIN
    INSERT INTO inventory_items (
        category_id, name, description, unit, 
        current_quantity, min_quantity, cost_per_unit, supplier, last_restocked
    )
    VALUES (
        p_category_id, p_name, p_description, p_unit,
        p_current_quantity, p_min_quantity, p_cost_per_unit, p_supplier, NOW()
    );
    
    SET p_item_id = LAST_INSERT_ID();
    
    -- Record the initial stock
    INSERT INTO inventory_transactions (
        item_id, quantity, transaction_type, notes, performed_by
    )
    VALUES (
        p_item_id, p_current_quantity, 'In', 'Initial stock', 1
    );
END //
DELIMITER ;

-- Procedure: Update inventory quantity
DELIMITER //
CREATE PROCEDURE sp_update_inventory(
    IN p_item_id INT,
    IN p_quantity INT,
    IN p_transaction_type VARCHAR(3),
    IN p_related_to VARCHAR(50),
    IN p_related_id INT,
    IN p_notes TEXT,
    IN p_performed_by INT
)
BEGIN
    DECLARE v_current_quantity INT;
    
    -- Get current quantity
    SELECT current_quantity INTO v_current_quantity
    FROM inventory_items
    WHERE item_id = p_item_id;
    
    -- Update quantity based on transaction type
    IF p_transaction_type = 'In' THEN
        UPDATE inventory_items
        SET current_quantity = current_quantity + p_quantity,
            last_restocked = CASE WHEN p_transaction_type = 'In' THEN NOW() ELSE last_restocked END
        WHERE item_id = p_item_id;
    ELSEIF p_transaction_type = 'Out' THEN
        IF v_current_quantity < p_quantity THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Insufficient inventory quantity';
        ELSE
            UPDATE inventory_items
            SET current_quantity = current_quantity - p_quantity
            WHERE item_id = p_item_id;
        END IF;
    END IF;
    
    -- Record transaction
    INSERT INTO inventory_transactions (
        item_id, quantity, transaction_type, related_to, related_id, notes, performed_by
    )
    VALUES (
        p_item_id, p_quantity, p_transaction_type, p_related_to, p_related_id, p_notes, p_performed_by
    );
END //
DELIMITER ;

-- Procedure: Get low stock items
DELIMITER //
CREATE PROCEDURE sp_get_low_stock_items()
BEGIN
    SELECT 
        i.item_id,
        i.name,
        ic.name AS category,
        i.current_quantity,
        i.min_quantity,
        i.unit,
        i.cost_per_unit,
        i.supplier
    FROM 
        inventory_items i
    JOIN 
        inventory_categories ic ON i.category_id = ic.category_id
    WHERE 
        i.current_quantity <= i.min_quantity
    ORDER BY 
        (i.current_quantity / i.min_quantity) ASC;
END //
DELIMITER ;

-- ======= BILLING PROCEDURES =======

-- Procedure: Create bill for reservation
DELIMITER //
CREATE PROCEDURE sp_create_bill(
    IN p_reservation_id INT,
    IN p_created_by INT,
    OUT p_bill_id INT
)
BEGIN
    DECLARE v_total_amount DECIMAL(10,2) DEFAULT 0;
    DECLARE v_tax_rate DECIMAL(5,2) DEFAULT 0.1; -- 10% tax
    DECLARE v_tax_amount DECIMAL(10,2);
    DECLARE v_grand_total DECIMAL(10,2);
    
    -- Calculate room charges
    SELECT 
        SUM(rr.rate_per_night * DATEDIFF(r.check_out_date, r.check_in_date)) 
    INTO v_total_amount
    FROM 
        reservations r
    JOIN 
        reservation_rooms rr ON r.reservation_id = rr.reservation_id
    WHERE 
        r.reservation_id = p_reservation_id;
    
    -- Calculate tax and grand total
    SET v_tax_amount = v_total_amount * v_tax_rate;
    SET v_grand_total = v_total_amount + v_tax_amount;
    
    -- Create the bill
    INSERT INTO billing (
        reservation_id, total_amount, tax_amount, grand_total, 
        payment_status, created_by
    )
    VALUES (
        p_reservation_id, v_total_amount, v_tax_amount, v_grand_total,
        'Pending', p_created_by
    );
    
    SET p_bill_id = LAST_INSERT_ID();
    
    -- Add room charges as bill items
    INSERT INTO bill_items (
        bill_id, item_type, description, quantity, unit_price, total_price
    )
    SELECT 
        p_bill_id, 
        'Room', 
        CONCAT('Room ', rm.room_number, ' (', rt.name, ')'), 
        DATEDIFF(r.check_out_date, r.check_in_date),
        rr.rate_per_night,
        rr.rate_per_night * DATEDIFF(r.check_out_date, r.check_in_date)
    FROM 
        reservations r
    JOIN 
        reservation_rooms rr ON r.reservation_id = rr.reservation_id
    JOIN 
        rooms rm ON rr.room_id = rm.room_id
    JOIN 
        room_types rt ON rm.type_id = rt.type_id
    WHERE 
        r.reservation_id = p_reservation_id;
END //
DELIMITER ;

-- Procedure: Add service to bill
DELIMITER //
CREATE PROCEDURE sp_add_service_to_bill(
    IN p_bill_id INT,
    IN p_service_id INT,
    IN p_quantity INT,
    IN p_notes VARCHAR(255)
)
BEGIN
    DECLARE v_service_name VARCHAR(100);
    DECLARE v_service_price DECIMAL(10,2);
    DECLARE v_total_price DECIMAL(10,2);
    
    -- Get service details
    SELECT name, price INTO v_service_name, v_service_price
    FROM services
    WHERE service_id = p_service_id;
    
    SET v_total_price = v_service_price * p_quantity;
    
    -- Add service to bill items
    INSERT INTO bill_items (
        bill_id, item_type, description, quantity, unit_price, total_price
    )
    VALUES (
        p_bill_id, 'Service', v_service_name, p_quantity, v_service_price, v_total_price
    );
    
    -- Update bill totals
    UPDATE billing
    SET total_amount = total_amount + v_total_price,
        tax_amount = (total_amount + v_total_price) * 0.1,
        grand_total = (total_amount + v_total_price) * 1.1
    WHERE bill_id = p_bill_id;
END //
DELIMITER ;

-- Procedure: Update payment status
DELIMITER //
CREATE PROCEDURE sp_update_payment_status(
    IN p_bill_id INT,
    IN p_payment_status VARCHAR(20),
    IN p_payment_method VARCHAR(50),
    IN p_notes TEXT
)
BEGIN
    UPDATE billing
    SET payment_status = p_payment_status,
        payment_method = p_payment_method,
        notes = p_notes
    WHERE bill_id = p_bill_id;
END //
DELIMITER ;

-- ======= REPORTING PROCEDURES =======

-- Procedure: Get occupancy report
DELIMITER //
CREATE PROCEDURE sp_get_occupancy_report(
    IN p_start_date DATE,
    IN p_end_date DATE
)
BEGIN
    -- Calculate total rooms and days in period
    DECLARE v_total_rooms INT;
    DECLARE v_total_days INT;
    DECLARE v_total_room_days INT;
    
    SELECT COUNT(*) INTO v_total_rooms FROM rooms;
    SET v_total_days = DATEDIFF(p_end_date, p_start_date) + 1;
    SET v_total_room_days = v_total_rooms * v_total_days;
    
    -- Get occupied room days
    SELECT 
        COUNT(*) AS occupied_rooms,
        ROUND((COUNT(*) / v_total_room_days) * 100, 2) AS occupancy_rate,
        SUM(rr.rate_per_night) AS total_revenue,
        ROUND(SUM(rr.rate_per_night) / COUNT(*), 2) AS average_daily_rate,
        ROUND(SUM(rr.rate_per_night) / v_total_rooms, 2) AS revenue_per_available_room
    FROM 
        reservations r
    JOIN 
        reservation_rooms rr ON r.reservation_id = rr.reservation_id
    WHERE 
        r.status IN ('Confirmed', 'Checked-in', 'Checked-out')
        AND (
            (r.check_in_date BETWEEN p_start_date AND p_end_date)
            OR (r.check_out_date BETWEEN p_start_date AND p_end_date)
            OR (p_start_date BETWEEN r.check_in_date AND r.check_out_date)
        );
END //
DELIMITER ;

-- Procedure: Get revenue report by room type
DELIMITER //
CREATE PROCEDURE sp_get_revenue_by_room_type(
    IN p_start_date DATE,
    IN p_end_date DATE
)
BEGIN
    SELECT 
        rt.name AS room_type,
        COUNT(DISTINCT r.room_id) AS room_count,
        COUNT(DISTINCT rr.reservation_id) AS reservation_count,
        SUM(bi.total_price) AS total_revenue,
        ROUND(SUM(bi.total_price) / COUNT(DISTINCT rr.reservation_id), 2) AS average_revenue_per_booking
    FROM 
        room_types rt
    JOIN 
        rooms r ON rt.type_id = r.type_id
    JOIN 
        reservation_rooms rr ON r.room_id = rr.room_id
    JOIN 
        reservations res ON rr.reservation_id = res.reservation_id
    JOIN 
        billing b ON res.reservation_id = b.reservation_id
    JOIN 
        bill_items bi ON b.bill_id = bi.bill_id
    WHERE 
        bi.item_type = 'Room'
        AND res.check_in_date BETWEEN p_start_date AND p_end_date
    GROUP BY 
        rt.type_id
    ORDER BY 
        total_revenue DESC;
END //
DELIMITER ;

-- Procedure: Get inventory usage report
DELIMITER //
CREATE PROCEDURE sp_get_inventory_usage_report(
    IN p_start_date DATE,
    IN p_end_date DATE
)
BEGIN
    SELECT 
        i.name AS item_name,
        ic.name AS category,
        SUM(CASE WHEN it.transaction_type = 'In' THEN it.quantity ELSE 0 END) AS total_in,
        SUM(CASE WHEN it.transaction_type = 'Out' THEN it.quantity ELSE 0 END) AS total_out,
        i.current_quantity AS current_stock,
        i.unit,
        ROUND(SUM(CASE WHEN it.transaction_type = 'Out' THEN it.quantity * i.cost_per_unit ELSE 0 END), 2) AS total_cost
    FROM 
        inventory_items i
    JOIN 
        inventory_categories ic ON i.category_id = ic.category_id
    LEFT JOIN 
        inventory_transactions it ON i.item_id = it.item_id
        AND DATE(it.transaction_date) BETWEEN p_start_date AND p_end_date
    GROUP BY 
        i.item_id
    ORDER BY 
        total_cost DESC;
END //
DELIMITER ;