-- ===========================================
-- 📘 BÀI 13: Flyway Migration V7
-- Seed dữ liệu khởi tạo chuẩn cho môi trường phát triển
-- Quản lý dữ liệu mẫu có versioning thông qua Flyway
-- ===========================================

-- 1. Seed bảng users
INSERT INTO users (name, email) VALUES ('Nguyễn Văn A', 'a@gmail.com');
INSERT INTO users (name, email) VALUES ('Trần Thị B', 'b@gmail.com');
INSERT INTO users (name, email) VALUES ('Lê Văn C', 'c@gmail.com');

-- 2. Seed bảng products (kèm audit metadata)
INSERT INTO products (name, description, price, category, stock, created_at, updated_at, created_by, last_modified_by)
VALUES ('MacBook Pro M3', 'Laptop cao cấp của Apple trang bị chip M3', 2499.99, 'Laptop', 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SystemSeeder', 'SystemSeeder');

INSERT INTO products (name, description, price, category, stock, created_at, updated_at, created_by, last_modified_by)
VALUES ('iPhone 16 Pro', 'Điện thoại flagship Apple với camera nâng cấp', 1199.99, 'Smartphone', 100, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SystemSeeder', 'SystemSeeder');

INSERT INTO products (name, description, price, category, stock, created_at, updated_at, created_by, last_modified_by)
VALUES ('Samsung Galaxy S25', 'Flagship Android thế hệ mới của Samsung', 999.99, 'Smartphone', 80, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SystemSeeder', 'SystemSeeder');

INSERT INTO products (name, description, price, category, stock, created_at, updated_at, created_by, last_modified_by)
VALUES ('Dell XPS 15', 'Laptop mỏng nhẹ cao cấp dành cho lập trình viên', 1799.99, 'Laptop', 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SystemSeeder', 'SystemSeeder');

INSERT INTO products (name, description, price, category, stock, created_at, updated_at, created_by, last_modified_by)
VALUES ('AirPods Pro 3', 'Tai nghe chống ồn chủ động thế hệ mới', 249.99, 'Phụ kiện', 200, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SystemSeeder', 'SystemSeeder');

INSERT INTO products (name, description, price, category, stock, created_at, updated_at, created_by, last_modified_by)
VALUES ('Bàn phím Keychron K8', 'Bàn phím cơ không dây switch Gateron Pro', 89.99, 'Phụ kiện', 150, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SystemSeeder', 'SystemSeeder');

-- 3. Seed bảng orders (@ManyToOne users)
INSERT INTO orders (description, total_amount, status, created_at, user_id)
VALUES ('Đơn hàng MacBook Pro M3 cho công việc đồ họa', 2499.99, 'DELIVERED', CURRENT_TIMESTAMP, 1);

INSERT INTO orders (description, total_amount, status, created_at, user_id)
VALUES ('Đơn hàng phụ kiện AirPods Pro 3', 249.99, 'SHIPPED', CURRENT_TIMESTAMP, 1);

INSERT INTO orders (description, total_amount, status, created_at, user_id)
VALUES ('Đơn hàng smartphone iPhone 16 Pro', 1199.99, 'PENDING', CURRENT_TIMESTAMP, 2);

INSERT INTO orders (description, total_amount, status, created_at, user_id)
VALUES ('Đơn hàng bàn phím cơ và phụ kiện công nghệ', 89.99, 'CONFIRMED', CURRENT_TIMESTAMP, 3);

-- 4. Seed bảng tags
INSERT INTO tags (name) VALUES ('Flagship');
INSERT INTO tags (name) VALUES ('Budget');
INSERT INTO tags (name) VALUES ('Gaming');
INSERT INTO tags (name) VALUES ('Apple');
INSERT INTO tags (name) VALUES ('Portable');

-- 5. Seed bảng trung gian product_tags (@ManyToMany)
INSERT INTO product_tags (product_id, tag_id) VALUES (1, 1); -- MacBook Pro M3 -> Flagship
INSERT INTO product_tags (product_id, tag_id) VALUES (1, 4); -- MacBook Pro M3 -> Apple
INSERT INTO product_tags (product_id, tag_id) VALUES (2, 1); -- iPhone 16 Pro -> Flagship
INSERT INTO product_tags (product_id, tag_id) VALUES (2, 4); -- iPhone 16 Pro -> Apple
INSERT INTO product_tags (product_id, tag_id) VALUES (3, 1); -- Samsung Galaxy S25 -> Flagship
INSERT INTO product_tags (product_id, tag_id) VALUES (5, 4); -- AirPods Pro 3 -> Apple
INSERT INTO product_tags (product_id, tag_id) VALUES (5, 5); -- AirPods Pro 3 -> Portable
INSERT INTO product_tags (product_id, tag_id) VALUES (6, 2); -- Keychron K8 -> Budget

-- 6. Seed bảng audit_logs
INSERT INTO audit_logs (action, detail, entity_id, created_at)
VALUES ('FLYWAY_INITIALIZATION', 'Khởi tạo hệ thống cơ sở dữ liệu với Flyway Migration Version 7', NULL, CURRENT_TIMESTAMP);
