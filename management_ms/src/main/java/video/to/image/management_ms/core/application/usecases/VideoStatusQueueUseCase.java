package video.to.image.management_ms.core.application.usecases;

import video.to.image.management_ms.core.application.ports.in.VideoStatusQueueUseCaseInputPort;
import video.to.image.management_ms.core.application.ports.out.VideoProcessRepositoryOutputPort;
import video.to.image.management_ms.core.domain.entities.VideoProcess;
import video.to.image.management_ms.core.domain.enums.VideoProcessingStatus;
import video.to.image.management_ms.core.domain.exceptions.NotFoundException;

import java.time.Instant;
import java.util.UUID;

public class VideoStatusQueueUseCase implements VideoStatusQueueUseCaseInputPort {

    private final VideoProcessRepositoryOutputPort videoProcessRepository;

    public VideoStatusQueueUseCase(VideoProcessRepositoryOutputPort videoProcessRepository) {
        this.videoProcessRepository = videoProcessRepository;
    }

    @Override
    public void updateStatus(UUID videoProcessId, VideoProcessingStatus status, String zipStorageKey, String zipFileName) {
        VideoProcess videoProcess = videoProcessRepository.findById(videoProcessId)
                .orElseThrow(() -> new NotFoundException("Processamento de video nao encontrado"));

        videoProcess.setStatus(status);
        if (zipStorageKey != null && !zipStorageKey.isBlank()) {
            videoProcess.setZipStorageKey(zipStorageKey);
        }
        if (zipFileName != null && !zipFileName.isBlank()) {
            videoProcess.setZipFileName(zipFileName);
        }
        videoProcess.setUpdatedAt(Instant.now());
        videoProcessRepository.save(videoProcess);
    }
}
