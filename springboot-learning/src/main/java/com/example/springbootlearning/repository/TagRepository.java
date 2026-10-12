package com.example.springbootlearning.repository;

import com.example.springbootlearning.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 📘 BÀI 10 — Tag Repository (Spring Data JPA)
 */
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
