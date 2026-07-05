package video.to.image.management_ms.infra.adapters.outbound.persistence.mappers;

import video.to.image.management_ms.core.domain.entities.VideoProcess;
import video.to.image.management_ms.infra.adapters.outbound.persistence.jpaentities.JpaVideoProcess;

public class JpaVideoProcessMapper {

    private JpaVideoProcessMapper() {
    }

    public static JpaVideoProcess toJpa(VideoProcess videoProcess) {
        JpaVideoProcess entity = new JpaVideoProcess();
        entity.setId(videoProcess.getId());
        entity.setUserId(videoProcess.getUserId());
        entity.setOriginalFileName(videoProcess.getOriginalFileName());
        entity.setStorageKey(videoProcess.getStorageKey());
        entity.setZipStorageKey(videoProcess.getZipStorageKey());
        entity.setZipFileName(videoProcess.getZipFileName());
        entity.setContentType(videoProcess.getContentType());
        entity.setFileSize(videoProcess.getFileSize());
        entity.setStatus(videoProcess.getStatus());
        entity.setCreatedAt(videoProcess.getCreatedAt());
        entity.setUpdatedAt(videoProcess.getUpdatedAt());
        return entity;
    }

    public static VideoProcess toDomain(JpaVideoProcess entity) {
        VideoProcess videoProcess = new VideoProcess();
        videoProcess.setId(entity.getId());
        videoProcess.setUserId(entity.getUserId());
        videoProcess.setOriginalFileName(entity.getOriginalFileName());
        videoProcess.setStorageKey(entity.getStorageKey());
        videoProcess.setZipStorageKey(entity.getZipStorageKey());
        videoProcess.setZipFileName(entity.getZipFileName());
        videoProcess.setContentType(entity.getContentType());
        videoProcess.setFileSize(entity.getFileSize());
        videoProcess.setStatus(entity.getStatus());
        videoProcess.setCreatedAt(entity.getCreatedAt());
        videoProcess.setUpdatedAt(entity.getUpdatedAt());
        return videoProcess;
    }
}
