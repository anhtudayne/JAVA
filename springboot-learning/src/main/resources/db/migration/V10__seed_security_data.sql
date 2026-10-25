-- ===========================================
-- 📘 BÀI 18: Flyway Migration V10
-- Seed dữ liệu Role, Admin User và Phân quyền User_Roles
-- ===========================================

-- 1. Seed danh sách Roles
INSERT INTO roles (name) VALUES ('ROLE_USER');     -- ID 1
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');    -- ID 2
INSERT INTO roles (name) VALUES ('ROLE_MANAGER');  -- ID 3

-- 2. Seed Admin User (password: 'admin123')
INSERT INTO users (name, email, password, active)
VALUES ('System Administrator', 'admin@gmail.com', '$2a$10$Rrzp5flRjVKu.0f2LTLsK..seBrEpdhl1kAjKDJo/UuWRE0W12TXK', TRUE);

-- 3. Phân quyền User - Role
-- User 1 (a@gmail.com) có role USER và MANAGER
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (1, 3);

-- User 2 (b@gmail.com) có role USER
INSERT INTO user_roles (user_id, role_id) VALUES (2, 1);

-- User 3 (c@gmail.com) có role USER
INSERT INTO user_roles (user_id, role_id) VALUES (3, 1);

-- User 4 (admin@gmail.com) có role ADMIN và USER
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'admin@gmail.com' AND r.name IN ('ROLE_ADMIN', 'ROLE_USER');
