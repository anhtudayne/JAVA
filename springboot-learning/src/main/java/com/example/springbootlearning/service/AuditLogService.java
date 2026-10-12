package com.example.springbootlearning.service;

import com.example.springbootlearning.model.AuditLog;
import com.example.springbootlearning.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 📘 BÀI 12 — AuditLogService (Minh họa Propagation.REQUIRES_NEW)
 *
 * Điểm cốt lõi:
 *   - @Transactional(propagation = Propagation.REQUIRES_NEW)
 *   - Tạo một Transaction MỚI, tạm dừng (suspend) transaction hiện tại của caller.
 *   - AuditLog được commit ĐỘC LẬP ngay khi method này hoàn thành.
 *   - Cho dù caller sau đó ném Exception và rollback, AuditLog vẫn tồn tại trong Database.
 */
@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Ghi nhật ký hệ thống độc lập với transaction bên ngoài.
     * Dùng Propagation.REQUIRES_NEW để đảm bảo audit log không bị rollback theo business logic.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog logAction(String action, String detail, Long entityId) {
        log.info("📝 [REQUIRES_NEW] Ghi nhận AuditLog độc lập: action={}, entityId={}", action, entityId);
        AuditLog auditLog = new AuditLog(action, detail, entityId);
        return auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc();
    }
}
