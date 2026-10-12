package com.example.springbootlearning.repository;

import com.example.springbootlearning.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 📘 BÀI 12 — AuditLog Repository
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findAllByOrderByCreatedAtDesc();
}
