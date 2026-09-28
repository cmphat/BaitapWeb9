-- ====================================================================
-- SCRIPT KHỞI TẠO CƠ SỞ DỮ LIỆU SQL SERVER: BaitapWeb9_Security
-- Môn: Lập trình Web - Bài tập Web 9: Spring Security 6
-- Giảng viên: ThS. Nguyễn Hữu Trung
-- ====================================================================

-- 1. Tạo Database nếu chưa tồn tại
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'BaitapWeb9_Security')
BEGIN
    CREATE DATABASE BaitapWeb9_Security;
END
GO

USE BaitapWeb9_Security;
GO

-- 2. Xóa các bảng cũ nếu đã tồn tại (theo thứ tự khóa ngoại)
IF OBJECT_ID('products', 'U') IS NOT NULL DROP TABLE products;
IF OBJECT_ID('otp_tokens', 'U') IS NOT NULL DROP TABLE otp_tokens;
IF OBJECT_ID('users', 'U') IS NOT NULL DROP TABLE users;
IF OBJECT_ID('roles', 'U') IS NOT NULL DROP TABLE roles;
GO

-- 3. Tạo bảng roles
CREATE TABLE roles (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL UNIQUE
);
GO

-- 4. Tạo bảng users
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL UNIQUE,
    email NVARCHAR(100) NOT NULL UNIQUE,
    password NVARCHAR(255) NOT NULL,
    enabled BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    role_id BIGINT NOT NULL,
    CONSTRAINT FK_Users_Roles FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);
GO

-- 5. Tạo bảng otp_tokens
CREATE TABLE otp_tokens (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    email NVARCHAR(100) NOT NULL,
    otp_hash NVARCHAR(255) NOT NULL,
    otp_type NVARCHAR(30) NOT NULL, -- REGISTRATION hoặc RESET_PASSWORD
    expiry_time DATETIME2 NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    used BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
GO

-- 6. Tạo bảng products
CREATE TABLE products (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(200) NOT NULL,
    price DECIMAL(18,2) NOT NULL,
    description NVARCHAR(MAX),
    image_url NVARCHAR(500),
    cloudinary_public_id NVARCHAR(200),
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    user_id BIGINT NOT NULL,
    CONSTRAINT FK_Products_Users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
GO

-- 7. Seed dữ liệu mẫu ban đầu
-- Mật khẩu cho user và admin: 123456 (được băm bằng BCrypt: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi)
INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');
GO

DECLARE @roleUserId BIGINT = (SELECT id FROM roles WHERE name = 'ROLE_USER');
DECLARE @roleAdminId BIGINT = (SELECT id FROM roles WHERE name = 'ROLE_ADMIN');

-- Chèn Admin (admin / 123456)
INSERT INTO users (username, email, password, enabled, role_id)
VALUES ('admin', 'admin@iotstar.vn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 1, @roleAdminId);

-- Chèn User (user / 123456)
INSERT INTO users (username, email, password, enabled, role_id)
VALUES ('user', 'user@iotstar.vn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 1, @roleUserId);

-- Chèn User 2 (user2 / 123456)
INSERT INTO users (username, email, password, enabled, role_id)
VALUES ('user2', 'user2@iotstar.vn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 1, @roleUserId);

DECLARE @adminId BIGINT = (SELECT id FROM users WHERE username = 'admin');
DECLARE @userId BIGINT = (SELECT id FROM users WHERE username = 'user');
DECLARE @user2Id BIGINT = (SELECT id FROM users WHERE username = 'user2');

-- Chèn một số sản phẩm mẫu
INSERT INTO products (name, price, description, image_url, user_id)
VALUES 
(N'Laptop Dell XPS 15', 35000000, N'Laptop cao cấp chuyên đồ họa và lập trình', 'https://images.unsplash.com/photo-1593642632823-8f785ba67e45', @adminId),
(N'Bàn phím cơ Keychron K2', 1850000, N'Bàn phím cơ Bluetooth gõ êm, layout 75%', 'https://images.unsplash.com/photo-1587829741301-dc798b83add3', @userId),
(N'Chuột Logitech MX Master 3S', 2200000, N'Chuột công thái học cao cấp cho lập trình viên', 'https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7', @userId),
(N'Tai nghe Sony WH-1000XM5', 6990000, N'Tai nghe chống ồn chủ động đỉnh cao', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e', @user2Id);
GO

PRINT N'Khởi tạo CSDL BaitapWeb9_Security và dữ liệu mẫu thành công!';
