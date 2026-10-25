package com.example.springbootlearning.repository;

import com.example.springbootlearning.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 📘 BÀI 18 — RoleRepository
 * Quản lý truy vấn bảng roles trong Spring Data JPA.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);
}
