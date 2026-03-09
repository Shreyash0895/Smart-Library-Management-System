FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED BY 'root@123';

create database library;
use library;
show databases;

create database library_management;
use library_management;
show TABLES;
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    is_approved BOOLEAN DEFAULT FALSE
);
drop table users;


USE library_management;
-- Drop the old users table
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100),
    username VARCHAR(50) UNIQUE,
    password VARCHAR(100),
    role VARCHAR(20),
    type VARCHAR(20),
    email VARCHAR(100),
    phone VARCHAR(20),
    is_approved BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200),
    author VARCHAR(100),
    isbn VARCHAR(50),
    quantity INT DEFAULT 1,
    available INT DEFAULT 1
);

CREATE TABLE IF NOT EXISTS issued_books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT,
    user_id INT,
    issue_date DATE,
    due_date DATE,
    return_date DATE,
    fine DOUBLE DEFAULT 0
);

CREATE TABLE IF NOT EXISTS notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    message TEXT,
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS fines (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    amount DOUBLE,
    paid BOOLEAN DEFAULT FALSE,
    date DATE
);

USE library_management;

CREATE TABLE IF NOT EXISTS book_borrowings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT,
    user_id INT,
    borrow_date DATE,
    due_date DATE,
    return_date DATE,
    status VARCHAR(20) DEFAULT 'BORROWED',
    fine DOUBLE DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
USE library_management;

CREATE TABLE IF NOT EXISTS settings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    theme VARCHAR(20) DEFAULT 'Light',
    notifications_enabled TINYINT(1) DEFAULT 1,
    language VARCHAR(20) DEFAULT 'English',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS book_borrowings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT,
    user_id INT,
    borrow_date DATE,
    due_date DATE,
    return_date DATE,
    status VARCHAR(20) DEFAULT 'BORROWED',
    fine DOUBLE DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS book_requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT,
    user_id INT,
    request_date DATE,
    status VARCHAR(20) DEFAULT 'PENDING'
);

CREATE TABLE IF NOT EXISTS book_reviews (
    id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT,
    user_id INT,
    rating INT,
    review TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

USE library_management;

INSERT INTO books (title, author, isbn, quantity, available) VALUES
('The Great Gatsby', 'F. Scott Fitzgerald', '978-0743273565', 3, 3),
('To Kill a Mockingbird', 'Harper Lee', '978-0061935466', 2, 1),
('1984', 'George Orwell', '978-0451524935', 5, 0),
('Harry Potter', 'J.K. Rowling', '978-0439708180', 4, 2);

USE library_management;
UPDATE users SET password = 'vishwa@123' WHERE username = 'Vishwa_Leanrs';

ALTER TABLE users ADD COLUMN is_active TINYINT(1) DEFAULT 1;

SET SQL_SAFE_UPDATES = 0;
UPDATE users SET is_active = 1;
SET SQL_SAFE_UPDATES = 1;

SET SQL_SAFE_UPDATES = 0;

-- Fix all users
UPDATE users SET is_active = 1, is_approved = 1;

-- Check admin user exists with correct data
SELECT username, password, role, is_active, is_approved FROM users WHERE username = 'admin';

SET SQL_SAFE_UPDATES = 1;

INSERT INTO users (full_name, username, password, role, type, email, is_approved, is_active) 
VALUES ('Admin', 'admin', 'admin123', 'ADMIN', 'ADMIN', 'admin@library.com', 1, 1);

USE library_management;
ALTER TABLE users ADD COLUMN type VARCHAR(20);
SET SQL_SAFE_UPDATES = 0;
UPDATE users SET type = role;
SET SQL_SAFE_UPDATES = 1;

USE library_management;
INSERT INTO users (full_name, username, password, role, type, email, is_approved, is_active)
VALUES ('Sujal Jadhav', 'sujya7781', 'sujal@123', 'STUDENT', 'STUDENT', 'sujaljadhav@gmail.com', 1, 1);

USE library_management;
ALTER TABLE books ADD COLUMN available_quantity INT DEFAULT 0;
SET SQL_SAFE_UPDATES = 0;
UPDATE books SET available_quantity = available;
SET SQL_SAFE_UPDATES = 1;

USE library_management;
ALTER TABLE books ADD COLUMN book_id INT;
SET SQL_SAFE_UPDATES = 0;
UPDATE books SET book_id = id;
SET SQL_SAFE_UPDATES = 1;

USE library_management;
ALTER TABLE book_borrowings ADD COLUMN fine_amount DOUBLE DEFAULT 0;
ALTER TABLE book_borrowings ADD COLUMN borrowing_id INT;
SET SQL_SAFE_UPDATES = 0;
UPDATE book_borrowings SET borrowing_id = id;
SET SQL_SAFE_UPDATES = 1;

USE library_management;
ALTER TABLE book_borrowings ADD COLUMN fine_paid TINYINT(1) DEFAULT 0;
ALTER TABLE book_borrowings ADD COLUMN user_id INT;
ALTER TABLE book_borrowings ADD COLUMN book_id INT;

USE library_management;
ALTER TABLE books ADD COLUMN category VARCHAR(50) DEFAULT 'General';
ALTER TABLE books ADD COLUMN shelf_location VARCHAR(50) DEFAULT 'A1';

USE library_management;
ALTER TABLE users ADD COLUMN user_id INT;
SET SQL_SAFE_UPDATES = 0;
UPDATE users SET user_id = id;
SET SQL_SAFE_UPDATES = 1;

USE library_management;
SELECT id, username, full_name, role, is_approved FROM users;

USE library_management;
INSERT INTO users (full_name, username, password, role, type, email, is_approved, is_active)
VALUES ('Aarya Jambhulkar', 'aarya123', 'aarya123', 'STUDENT', 'STUDENT', 'aaryaj@gmail.com', 1, 1);

USE library_management;
ALTER TABLE notifications ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE notifications ADD COLUMN is_read TINYINT(1) DEFAULT 0;

USE library_management;
INSERT INTO users (full_name, username, password, role, type, email, is_approved, is_active)
VALUES ('Anil Deshmukh', 'anil_123', 'anil123', 'STUDENT', 'STUDENT', 'anildesh@gmail.com', 1, 1);

USE library_management;
INSERT INTO users (full_name, username, password, role, type, email, is_approved, is_active)
VALUES ('Vishaka Deshpande', 'Vishaka_Librarian', 'vish@123', 'LIBRARIAN', 'LIBRARIAN', 'vishakadesh@gmail.com', 1, 1);