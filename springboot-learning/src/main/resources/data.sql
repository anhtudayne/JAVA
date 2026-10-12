-- ===========================================
-- 📘 BÀI 10: Seed data cho Database H2 (Entity Relationships)
-- File này tự động được nạp khi ứng dụng khởi động
-- ===========================================

-- 1. Seed data cho bảng products
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

-- 2. Seed data cho bảng users
INSERT INTO users (name, email) VALUES ('Nguyễn Văn A', 'a@gmail.com');
INSERT INTO users (name, email) VALUES ('Trần Thị B', 'b@gmail.com');
INSERT INTO users (name, email) VALUES ('Lê Văn C', 'c@gmail.com');

-- 3. Seed data cho bảng orders (@ManyToOne với users)
INSERT INTO orders (description, total_amount, status, created_at, user_id)
VALUES ('Đơn hàng MacBook Pro M3 cho công việc đồ họa', 2499.99, 'DELIVERED', CURRENT_TIMESTAMP, 1);

INSERT INTO orders (description, total_amount, status, created_at, user_id)
VALUES ('Đơn hàng phụ kiện AirPods Pro 3', 249.99, 'SHIPPED', CURRENT_TIMESTAMP, 1);

INSERT INTO orders (description, total_amount, status, created_at, user_id)
VALUES ('Đơn hàng smartphone iPhone 16 Pro', 1199.99, 'PENDING', CURRENT_TIMESTAMP, 2);

INSERT INTO orders (description, total_amount, status, created_at, user_id)
VALUES ('Đơn hàng bàn phím cơ và phụ kiện công nghệ', 89.99, 'CONFIRMED', CURRENT_TIMESTAMP, 3);

-- 4. Seed data cho bảng tags
INSERT INTO tags (name) VALUES ('Flagship');
INSERT INTO tags (name) VALUES ('Budget');
INSERT INTO tags (name) VALUES ('Gaming');
INSERT INTO tags (name) VALUES ('Apple');
INSERT INTO tags (name) VALUES ('Portable');

-- 5. Seed data cho bảng trung gian product_tags (@ManyToMany)
INSERT INTO product_tags (product_id, tag_id) VALUES (1, 1); -- MacBook Pro M3 -> Flagship
INSERT INTO product_tags (product_id, tag_id) VALUES (1, 4); -- MacBook Pro M3 -> Apple
INSERT INTO product_tags (product_id, tag_id) VALUES (2, 1); -- iPhone 16 Pro -> Flagship
INSERT INTO product_tags (product_id, tag_id) VALUES (2, 4); -- iPhone 16 Pro -> Apple
INSERT INTO product_tags (product_id, tag_id) VALUES (3, 1); -- Samsung Galaxy S25 -> Flagship
INSERT INTO product_tags (product_id, tag_id) VALUES (5, 4); -- AirPods Pro 3 -> Apple
INSERT INTO product_tags (product_id, tag_id) VALUES (5, 5); -- AirPods Pro 3 -> Portable
INSERT INTO product_tags (product_id, tag_id) VALUES (6, 2); -- Keychron K8 -> Budget
