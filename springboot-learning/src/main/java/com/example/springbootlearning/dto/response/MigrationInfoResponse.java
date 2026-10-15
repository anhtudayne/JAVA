package com.example.springbootlearning.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

/**
 * 📘 BÀI 13 — MigrationInfoResponse DTO
 * Đại diện cho thông tin một bản ghi migration trong Flyway schema history
 */
@Schema(description = "Thông tin chi tiết một bản ghi migration từ Flyway")
public class MigrationInfoResponse {

    @Schema(description = "Số phiên bản migration", example = "1")
    private String version;

    @Schema(description = "Mô tả của migration", example = "create users table")
    private String description;

    @Schema(description = "Loại migration (SQL, JDBC, BASELINE...)", example = "SQL")
    private String type;

    @Schema(description = "Tên file script", example = "V1__create_users_table.sql")
    private String script;

    @Schema(description = "Mã băm kiểm tra tính toàn vẹn Checksum", example = "-1892841029")
    private Integer checksum;

    @Schema(description = "Thời điểm áp dụng migration", example = "2026-10-14T21:55:00.000Z")
    private Date installedOn;

    @Schema(description = "Thời gian thực thi (milliseconds)", example = "15")
    private Integer executionTime;

    @Schema(description = "Trạng thái migration (SUCCESS, PENDING, FAILED...)", example = "SUCCESS")
    private String state;

    public MigrationInfoResponse() {
    }

    public MigrationInfoResponse(String version, String description, String type, String script,
                                 Integer checksum, Date installedOn, Integer executionTime, String state) {
        this.version = version;
        this.description = description;
        this.type = type;
        this.script = script;
        this.checksum = checksum;
        this.installedOn = installedOn;
        this.executionTime = executionTime;
        this.state = state;
    }

    // Getters and Setters
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public Integer getChecksum() {
        return checksum;
    }

    public void setChecksum(Integer checksum) {
        this.checksum = checksum;
    }

    public Date getInstalledOn() {
        return installedOn;
    }

    public void setInstalledOn(Date installedOn) {
        this.installedOn = installedOn;
    }

    public Integer getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Integer executionTime) {
        this.executionTime = executionTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
