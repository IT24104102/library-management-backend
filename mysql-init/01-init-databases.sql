-- Initialize all databases for the library management system

CREATE DATABASE IF NOT EXISTS library_user_db;
CREATE DATABASE IF NOT EXISTS book_service_db;
CREATE DATABASE IF NOT EXISTS borrow_service_db;
CREATE DATABASE IF NOT EXISTS library_payment_db;
CREATE DATABASE IF NOT EXISTS late_reminders_db;
CREATE DATABASE IF NOT EXISTS room_service_db;

-- Grant permissions
GRANT ALL PRIVILEGES ON library_user_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON book_service_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON borrow_service_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON library_payment_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON late_reminders_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON room_service_db.* TO 'root'@'%';

FLUSH PRIVILEGES;