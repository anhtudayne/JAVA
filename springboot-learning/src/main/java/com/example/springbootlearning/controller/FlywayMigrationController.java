package com.example.springbootlearning.controller;

import com.example.springbootlearning.dto.response.ApiResponse;
import com.example.springbootlearning.dto.response.MigrationInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.output.RepairResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 📘 BÀI 13 — FlywayMigrationController
 *
 * REST Controller phục vụ kiểm tra, giám sát trạng thái và lịch sử thực thi
 * của Flyway Database Migration trực tiếp qua HTTP API và Swagger UI.
 */
@Tag(name = "Bài 13 - Flyway Migration", description = "API quản lý và giám sát lịch sử Database Migration với Flyway")
@RestController
@RequestMapping("/api/v1/migrations")
public class FlywayMigrationController {

    private final Flyway flyway;

    public FlywayMigrationController(Flyway flyway) {
        this.flyway = flyway;
    }

    @Operation(
            summary = "Lấy toàn bộ lịch sử Migration",
            description = "Truy xuất danh sách tất cả các migration scripts (Versioned, Repeatable) đã và đang được quản lý bởi Flyway"
    )
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<MigrationInfoResponse>>> getMigrationHistory() {
        MigrationInfo[] migrations = flyway.info().all();
        List<MigrationInfoResponse> historyList = new ArrayList<>();

        for (MigrationInfo info : migrations) {
            historyList.add(new MigrationInfoResponse(
                    info.getVersion() != null ? info.getVersion().getVersion() : "Repeatable (No version)",
                    info.getDescription(),
                    info.getType() != null ? info.getType().name() : "N/A",
                    info.getScript(),
                    info.getChecksum(),
                    info.getInstalledOn(),
                    info.getExecutionTime(),
                    info.getState() != null ? info.getState().name() : "UNKNOWN"
            ));
        }

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách lịch sử Flyway migration thành công (tổng cộng " + historyList.size() + " bản ghi)",
                historyList
        ));
    }

    @Operation(
            summary = "Kiểm tra phiên bản Schema hiện tại",
            description = "Trả về phiên bản (current version) và trạng thái của Database Schema hiện tại"
    )
    @GetMapping("/current-version")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentSchemaVersion() {
        MigrationInfo current = flyway.info().current();
        Map<String, Object> versionData = new LinkedHashMap<>();

        if (current != null) {
            versionData.put("currentVersion", current.getVersion() != null ? current.getVersion().getVersion() : "None");
            versionData.put("description", current.getDescription());
            versionData.put("state", current.getState().name());
            versionData.put("installedOn", current.getInstalledOn());
            versionData.put("script", current.getScript());
        } else {
            versionData.put("currentVersion", "Chưa có migration nào được áp dụng");
        }

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy thông tin phiên bản schema hiện tại thành công",
                versionData
        ));
    }

    @Operation(
            summary = "Kích hoạt lệnh Flyway Repair",
            description = "Chạy lệnh flyway.repair() để xóa các bản ghi migration lỗi và căn chỉnh lại checksum nếu có sửa đổi script hợp lệ"
    )
    @PostMapping("/repair")
    public ResponseEntity<ApiResponse<Map<String, Object>>> repairMigrations() {
        RepairResult repairResult = flyway.repair();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("repairSummary", "Flyway repair hoàn tất thành công");
        result.put("migrationsRemoved", repairResult.repairActions);
        result.put("flywayVersion", repairResult.flywayVersion);

        return ResponseEntity.ok(ApiResponse.success(
                "Thực thi flyway.repair() thành công!",
                result
        ));
    }
}
