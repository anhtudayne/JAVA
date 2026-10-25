-- ===========================================
-- 📘 BÀI 18: Flyway Migration V8
-- Thêm cột password và active cho bảng users
-- ===========================================

-- Mật khẩu mặc định cho các user hiện có là: 'password123'
ALTER TABLE users ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT '$2a$10$3bnsc1shl5iTsG/qYNiNS.xePHaivdzeAxvFdX.X6DJ3F4mBcIZPW';
ALTER TABLE users ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
