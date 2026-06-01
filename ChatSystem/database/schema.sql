-- 1. Xóa các bảng cũ theo thứ tự ngược để tránh lỗi khóa ngoại (Foreign Key)
DROP TABLE IF EXISTS group_members;
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS groups;
DROP TABLE IF EXISTS users;

-- 2. Tạo bảng Users
CREATE TABLE users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(255) NOT NULL
);

-- 3. Tạo bảng Groups
CREATE TABLE groups (
    group_name VARCHAR(50) PRIMARY KEY,
    created_by VARCHAR(50) REFERENCES users(username),
    password VARCHAR(255)
);

-- 4. Tạo bảng Group_Members
CREATE TABLE group_members (
    group_name VARCHAR(50) REFERENCES groups(group_name) ON DELETE CASCADE,
    username VARCHAR(50) REFERENCES users(username) ON DELETE CASCADE,
    PRIMARY KEY (group_name, username)
);

-- 5. Tạo bảng Messages (với cột msg_type đã được đổi tên để tránh từ khóa)
CREATE TABLE messages (
    id SERIAL PRIMARY KEY,
    msg_type VARCHAR(20),
    sender VARCHAR(50),
    target VARCHAR(50),
    group_name VARCHAR(50),
    file_data BYTEA,
    file_name VARCHAR(100),
    success BOOLEAN,
    info TEXT,
    content TEXT,
    timestamp TIMESTAMP
);

