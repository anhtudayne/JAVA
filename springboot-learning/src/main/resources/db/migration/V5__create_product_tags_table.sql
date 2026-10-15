-- ===========================================
-- 📘 BÀI 13: Flyway Migration V5
-- Tạo bảng trung gian product_tags cho quan hệ @ManyToMany
-- ===========================================

CREATE TABLE product_tags (
    product_id BIGINT NOT NULL,
    tag_id     BIGINT NOT NULL,
    PRIMARY KEY (product_id, tag_id),
    CONSTRAINT fk_product_tags_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_product_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

CREATE INDEX idx_product_tags_product_id ON product_tags(product_id);
CREATE INDEX idx_product_tags_tag_id ON product_tags(tag_id);
