package com.example.springbootlearning.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * 📘 BÀI 10 — Product Entity (JPA & Hibernate ORM)
 *
 * Mở rộng:
 *   - @ManyToMany với Tag thông qua bảng trung gian `product_tags` (OWNING SIDE).
 */
@Entity
@Table(name = "products")
public class Product extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    /**
     * 🔑 OWNING SIDE của quan hệ @ManyToMany:
     * - @JoinTable: Khai báo bảng nối trung gian `product_tags`
     * - joinColumns: Khóa ngoại trỏ về bảng hiện tại (`products`)
     * - inverseJoinColumns: Khóa ngoại trỏ về bảng đối ứng (`tags`)
     * - Dùng Set<Tag> để tối ưu hiệu năng xóa liên kết (tránh delete all + re-insert)
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "product_tags",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    // ===== Constructors =====

    /**
     * BẮT BUỘC cho JPA/Hibernate (Reflection) và Jackson.
     */
    public Product() {
    }

    /**
     * Constructor thuận tiện cho việc tạo entity mới trước khi lưu.
     */
    public Product(Long id, String name, String description, Double price,
                   String category, Integer stock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.stock = stock;
    }

    // ===== Helper Methods cho quan hệ N-N =====
    public void addTag(Tag tag) {
        this.tags.add(tag);
        tag.getProducts().add(this);
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
        tag.getProducts().remove(this);
    }

    // ===== Getters & Setters =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    // ===== Chuẩn equals và hashCode cho JPA Entity =====
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product product)) return false;
        return id != null && id.equals(product.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", stock=" + stock +
                '}';
    }
}
