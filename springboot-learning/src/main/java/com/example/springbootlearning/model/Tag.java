package com.example.springbootlearning.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * 📘 BÀI 10 — Tag Entity (@ManyToMany Relationship)
 *
 * Mối quan hệ:
 *   - @ManyToMany với Product (Nhiều Tag gắn cho nhiều Product) -> INVERSE SIDE (mappedBy = "tags").
 */
@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    /**
     * 📖 INVERSE SIDE:
     * - mappedBy = "tags": Trỏ tới trường "tags" trong Product.java.
     * - Dùng Set thay vì List để tối ưu SQL khi xóa liên kết.
     */
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<Product> products = new HashSet<>();

    public Tag() {
    }

    public Tag(String name) {
        this.name = name;
    }

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

    public Set<Product> getProducts() {
        return products;
    }

    public void setProducts(Set<Product> products) {
        this.products = products;
    }

    // ===== Chuẩn equals và hashCode cho JPA Entity =====
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag tag)) return false;
        return id != null && id.equals(tag.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Tag{id=" + id + ", name='" + name + "'}";
    }
}
