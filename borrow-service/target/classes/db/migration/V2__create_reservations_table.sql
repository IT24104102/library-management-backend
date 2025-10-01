-- Create reservations table for the new reservation system
-- This allows students to reserve books and librarians to create loans

CREATE TABLE IF NOT EXISTS reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_isbn VARCHAR(20) NOT NULL,
    reservation_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    notes VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_book_isbn (book_isbn),
    INDEX idx_status (status),
    INDEX idx_reservation_date (reservation_date),
    INDEX idx_expiry_date (expiry_date),
    
    -- Ensure a user can only have one active reservation per book
    UNIQUE KEY unique_active_reservation (user_id, book_isbn, status)
);

-- Add some comments to document the table
ALTER TABLE reservations COMMENT = 'Stores book reservations made by students';

-- Sample data for testing (optional)
-- INSERT INTO reservations (user_id, book_isbn, reservation_date, expiry_date, status, notes) VALUES
-- (1, '978-0134685991', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'ACTIVE', 'Test reservation'),
-- (2, '978-0134494166', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'ACTIVE', 'Test reservation 2');