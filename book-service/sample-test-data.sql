-- Sample test data for book-service
-- Run this after the service has created the database schema

-- Sample Books for Testing
INSERT IGNORE INTO books (isbn, title, author, publisher, publication_year, genre, description, total_copies, available_copies, status, shelf_location, language, pages, created_at, updated_at) VALUES
-- Computer Science Books
('9780132350884', 'Clean Architecture: A Craftsman''s Guide to Software Structure and Design', 'Robert C. Martin', 'Prentice Hall', 2017, 'Computer Science', 'A comprehensive guide to software architecture and design principles', 5, 5, 'AVAILABLE', 'CS-ARCH-001', 'English', 432, NOW(), NOW()),
('9780201633610', 'Design Patterns: Elements of Reusable Object-Oriented Software', 'Gang of Four', 'Addison-Wesley', 1994, 'Computer Science', 'Classic book on software design patterns', 3, 3, 'AVAILABLE', 'CS-PATTERNS-001', 'English', 395, NOW(), NOW()),
('9780134685991', 'Effective Java', 'Joshua Bloch', 'Addison-Wesley', 2017, 'Programming', 'Best practices for Java programming language', 4, 4, 'AVAILABLE', 'PROG-JAVA-001', 'English', 412, NOW(), NOW()),

-- Fiction Books
('9780439708180', 'Harry Potter and the Sorcerer''s Stone', 'J.K. Rowling', 'Scholastic Inc.', 1997, 'Fantasy', 'A young wizard''s journey begins at Hogwarts School of Witchcraft and Wizardry', 6, 6, 'AVAILABLE', 'FICTION-HP-001', 'English', 320, NOW(), NOW()),
('9780061120084', 'To Kill a Mockingbird', 'Harper Lee', 'Harper Perennial', 1960, 'Fiction', 'A classic American novel about racial injustice and childhood innocence', 4, 4, 'AVAILABLE', 'FICTION-CLASSIC-001', 'English', 376, NOW(), NOW()),
('9781984801258', '1984', 'George Orwell', 'Signet Classic', 1949, 'Dystopian Fiction', 'A dystopian social science fiction novel about totalitarian control', 5, 5, 'AVAILABLE', 'FICTION-DYSTOPIAN-001', 'English', 328, NOW(), NOW()),

-- Science Books
('9780134093413', 'Campbell Biology', 'Jane Reece', 'Pearson', 2016, 'Biology', 'Comprehensive introduction to biology concepts', 3, 3, 'AVAILABLE', 'SCIENCE-BIO-001', 'English', 1488, NOW(), NOW()),
('9780321976499', 'Calculus: Early Transcendentals', 'James Stewart', 'Cengage Learning', 2015, 'Mathematics', 'Comprehensive calculus textbook', 4, 4, 'AVAILABLE', 'MATH-CALC-001', 'English', 1368, NOW(), NOW()),

-- Business Books
('9780307887894', 'The Lean Startup', 'Eric Ries', 'Crown Business', 2011, 'Business', 'How today''s entrepreneurs use continuous innovation to create radically successful businesses', 3, 3, 'AVAILABLE', 'BUSINESS-STARTUP-001', 'English', 336, NOW(), NOW()),
('9781591846444', 'Good to Great', 'Jim Collins', 'HarperBusiness', 2001, 'Management', 'Why some companies make the leap and others don''t', 2, 2, 'AVAILABLE', 'BUSINESS-MGMT-001', 'English', 320, NOW(), NOW());