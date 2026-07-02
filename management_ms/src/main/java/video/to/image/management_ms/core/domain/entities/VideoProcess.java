package video.to.image.management_ms.core.domain.entities;

import video.to.image.management_ms.core.domain.enums.VideoProcessingStatus;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class VideoProcess implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private UUID userId;
    private String originalFileName;
    private String storageKey;
    private String zipStorageKey;
    private String zipFileName;
    private String contentType;
    private long fileSize;
    private VideoProcessingStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public String getZipStorageKey() {
        return zipStorageKey;
    }

    public void setZipStorageKey(String zipStorageKey) {
        this.zipStorageKey = zipStorageKey;
    }

    public String getZipFileName() {
        return zipFileName;
    }

    public void setZipFileName(String zipFileName) {
        this.zipFileName = zipFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public VideoProcessingStatus getStatus() {
        return status;
    }

    public void setStatus(VideoProcessingStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
