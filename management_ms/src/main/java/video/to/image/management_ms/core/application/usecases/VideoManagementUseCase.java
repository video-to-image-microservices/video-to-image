package video.to.image.management_ms.core.application.usecases;

import video.to.image.management_ms.core.application.ports.in.VideoManagementUseCaseInputPort;
import video.to.image.management_ms.core.application.ports.out.ProcessQueueOutputPort;
import video.to.image.management_ms.core.application.ports.out.UserRepositoryOutputPort;
import video.to.image.management_ms.core.application.ports.out.VideoProcessRepositoryOutputPort;
import video.to.image.management_ms.core.application.ports.out.VideoStorageOutputPort;
import video.to.image.management_ms.core.domain.entities.StoredVideo;
import video.to.image.management_ms.core.domain.entities.VideoProcess;
import video.to.image.management_ms.core.domain.enums.VideoProcessingStatus;
import video.to.image.management_ms.core.domain.exceptions.NotFoundException;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class VideoManagementUseCase implements VideoManagementUseCaseInputPort {

    private final UserRepositoryOutputPort userRepository;
    private final VideoProcessRepositoryOutputPort videoProcessRepository;
    private final VideoStorageOutputPort videoStorage;
    private final ProcessQueueOutputPort processQueue;

    public VideoManagementUseCase(
            UserRepositoryOutputPort userRepository,
            VideoProcessRepositoryOutputPort videoProcessRepository,
            VideoStorageOutputPort videoStorage,
            ProcessQueueOutputPort processQueue
    ) {
        this.userRepository = userRepository;
        this.videoProcessRepository = videoProcessRepository;
        this.videoStorage = videoStorage;
        this.processQueue = processQueue;
    }

    @Override
    public VideoProcess upload(UUID userId, String originalFileName, String contentType, long fileSize, InputStream content) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Usuario nao encontrado para upload de video");
        }

        UUID videoProcessId = UUID.randomUUID();
        String safeFileName = sanitizeFileName(originalFileName);
        String storageKey = userId + "/" + videoProcessId + "/" + safeFileName;
        String zipFileName = buildZipFileName(safeFileName);
        String zipStorageKey = userId + "/" + videoProcessId + "/generated/" + zipFileName;
        String normalizedContentType = contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType;

        videoStorage.upload(storageKey, content, fileSize, normalizedContentType);

        Instant now = Instant.now();
        VideoProcess videoProcess = new VideoProcess();
        videoProcess.setId(videoProcessId);
        videoProcess.setUserId(userId);
        videoProcess.setOriginalFileName(safeFileName);
        videoProcess.setStorageKey(storageKey);
        videoProcess.setZipStorageKey(zipStorageKey);
        videoProcess.setZipFileName(zipFileName);
        videoProcess.setContentType(normalizedContentType);
        videoProcess.setFileSize(fileSize);
        videoProcess.setStatus(VideoProcessingStatus.RECEIVED);
        videoProcess.setCreatedAt(now);
        videoProcess.setUpdatedAt(now);

        VideoProcess saved = videoProcessRepository.save(videoProcess);
        processQueue.publish(saved);
        return saved;
    }

    @Override
    public StoredVideo download(UUID userId, String originalFileName) {
        VideoProcess videoProcess = findOwnedVideo(userId, originalFileName);
        if (videoProcess.getStatus() != VideoProcessingStatus.PROCESSED) {
            throw new NotFoundException("Zip ainda nao esta pronto para download");
        }
        if (videoProcess.getZipStorageKey() == null || videoProcess.getZipStorageKey().isBlank()) {
            throw new NotFoundException("Zip gerado nao encontrado no S3");
        }

        return videoStorage.download(videoProcess.getZipStorageKey(), videoProcess.getZipFileName());
    }

    @Override
    public VideoProcess getStatus(UUID userId, String originalFileName) {
        return findOwnedVideo(userId, originalFileName);
    }

    @Override
    public List<VideoProcess> listStatus(UUID userId) {
        return videoProcessRepository.findByUserId(userId);
    }

    private VideoProcess findOwnedVideo(UUID userId, String originalFileName) {
        String safeFileName = sanitizeFileName(originalFileName);
        return videoProcessRepository.findByUserIdAndOriginalFileName(userId, safeFileName)
                .orElseThrow(() -> new NotFoundException("Video nao encontrado"));
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Nome do arquivo e obrigatorio");
        }

        String normalizedFileName = fileName.replace("\\", "/");
        return normalizedFileName.substring(normalizedFileName.lastIndexOf("/") + 1);
    }

    private String buildZipFileName(String originalFileName) {
        int extensionIndex = originalFileName.lastIndexOf(".");
        String baseName = extensionIndex > 0 ? originalFileName.substring(0, extensionIndex) : originalFileName;
        return baseName + ".zip";
    }
}
