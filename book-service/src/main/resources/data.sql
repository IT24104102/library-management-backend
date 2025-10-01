# Sample Books Data
# This file contains sample book data that can be loaded into the system for testing

# Book 1 - Programming/Computer Science
INSERT INTO books (isbn, title, author, publisher, publication_year, genre, description, total_copies, available_copies, status, shelf_location, language, pages, created_at, updated_at) 
VALUES ('978-0134685991', 'Effective Java', 'Joshua Bloch', 'Addison-Wesley', 2017, 'Programming', 'Best practices for Java programming language', 5, 5, 'AVAILABLE', 'CS-001', 'English', 412, NOW(), NOW());

# Book 2 - Programming/Computer Science  
INSERT INTO books (isbn, title, author, publisher, publication_year, genre, description, total_copies, available_copies, status, shelf_location, language, pages, created_at, updated_at) 
VALUES ('978-0134494166', 'Clean Code', 'Robert C. Martin', 'Prentice Hall', 2008, 'Programming', 'A handbook of agile software craftsmanship', 3, 3, 'AVAILABLE', 'CS-002', 'English', 464, NOW(), NOW());

# Book 3 - Fiction
INSERT INTO books (isbn, title, author, publisher, publication_year, genre, description, total_copies, available_copies, status, shelf_location, language, pages, created_at, updated_at) 
VALUES ('978-0547928227', 'The Hobbit', 'J.R.R. Tolkien', 'Houghton Mifflin Harcourt', 2012, 'Fiction', 'A fantasy adventure novel about Bilbo Baggins', 7, 7, 'AVAILABLE', 'FIC-001', 'English', 366, NOW(), NOW());

# Book 4 - Science
INSERT INTO books (isbn, title, author, publisher, publication_year, genre, description, total_copies, available_copies, status, shelf_location, language, pages, created_at, updated_at) 
VALUES ('978-0345391803', 'The Hitchhiker\'s Guide to the Galaxy', 'Douglas Adams', 'Del Rey', 1995, 'Science Fiction', 'A comedic science fiction series', 4, 4, 'AVAILABLE', 'SCI-001', 'English', 224, NOW(), NOW());

# Book 5 - Database
INSERT INTO books (isbn, title, author, publisher, publication_year, genre, description, total_copies, available_copies, status, shelf_location, language, pages, created_at, updated_at) 
VALUES ('978-0321884497', 'Database System Concepts', 'Abraham Silberschatz', 'McGraw-Hill Education', 2019, 'Database', 'Comprehensive database systems textbook', 6, 6, 'AVAILABLE', 'CS-003', 'English', 1376, NOW(), NOW());