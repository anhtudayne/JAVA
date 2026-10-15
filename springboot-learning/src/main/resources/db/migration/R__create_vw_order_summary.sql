-- ===========================================
-- 📘 BÀI 13: Flyway Repeatable Migration (R)
-- Script này tự động chạy lại mỗi khi nội dung file thay đổi (checksum thay đổi)
-- Rất thích hợp cho SQL Views, Stored Procedures, Functions
-- ===========================================

CREATE OR REPLACE VIEW vw_order_summary AS
SELECT 
    o.id AS order_id,
    o.description AS order_description,
    o.total_amount,
    o.status,
    o.created_at,
    u.id AS user_id,
    u.name AS user_name,
    u.email AS user_email
FROM orders o
JOIN users u ON o.user_id = u.id;
