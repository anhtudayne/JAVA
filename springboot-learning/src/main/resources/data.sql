-- ===========================================
-- 📘 BÀI 9: Seed data cho Database H2
-- File này tự động được nạp khi ứng dụng khởi động
-- ===========================================

INSERT INTO products (name, description, price, category, stock, created_at, updated_at)
VALUES ('MacBook Pro M3', 'Laptop cao cấp của Apple trang bị chip M3', 2499.99, 'Laptop', 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO products (name, description, price, category, stock, created_at, updated_at)
VALUES ('iPhone 16 Pro', 'Điện thoại flagship Apple với camera nâng cấp', 1199.99, 'Smartphone', 100, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO products (name, description, price, category, stock, created_at, updated_at)
VALUES ('Samsung Galaxy S25', 'Flagship Android thế hệ mới của Samsung', 999.99, 'Smartphone', 80, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO products (name, description, price, category, stock, created_at, updated_at)
VALUES ('Dell XPS 15', 'Laptop mỏng nhẹ cao cấp dành cho lập trình viên', 1799.99, 'Laptop', 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO products (name, description, price, category, stock, created_at, updated_at)
VALUES ('AirPods Pro 3', 'Tai nghe chống ồn chủ động thế hệ mới', 249.99, 'Phụ kiện', 200, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO products (name, description, price, category, stock, created_at, updated_at)
VALUES ('Bàn phím Keychron K8', 'Bàn phím cơ không dây switch Gateron Pro', 89.99, 'Phụ kiện', 150, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Seed data cho users
INSERT INTO users (name, email) VALUES ('Nguyễn Văn A', 'a@gmail.com');
INSERT INTO users (name, email) VALUES ('Trần Thị B', 'b@gmail.com');
INSERT INTO users (name, email) VALUES ('Lê Văn C', 'c@gmail.com');

