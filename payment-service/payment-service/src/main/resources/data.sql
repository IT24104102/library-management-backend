-- Payment Service Database Sample Data
-- This file will be executed when the application starts to populate initial data

-- Sample Fine data (optional - these will be created dynamically)
-- INSERT INTO fines (id, user_id, loan_id, book_isbn, amount, type, status, description, created_date, updated_date) VALUES
-- (1, 1, 1, '978-0134685991', 5.50, 'OVERDUE', 'PENDING', 'Late return - 5 days overdue', '2024-01-20 10:00:00', '2024-01-20 10:00:00'),
-- (2, 2, 2, '978-0321573513', 12.75, 'OVERDUE', 'PENDING', 'Late return - 15 days overdue', '2024-01-15 14:30:00', '2024-01-15 14:30:00'),
-- (3, 3, 3, '978-0321741684', 25.00, 'DAMAGE', 'PAID', 'Book damage - cover torn', '2024-01-18 09:15:00', '2024-01-19 16:45:00');

-- Sample Payment data (optional - these will be created when payments are processed)
-- INSERT INTO payments (id, fine_id, user_id, amount, payment_method, transaction_id, payment_date, status, notes) VALUES
-- (1, 3, 3, 25.00, 'CARD', 'TXN-001-2024-01-19', '2024-01-19 16:45:00', 'COMPLETED', 'Payment processed successfully via credit card');

-- You can add more sample data here if needed for testing