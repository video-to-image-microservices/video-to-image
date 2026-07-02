package video.to.image.management_ms.core.application.usecases;

import org.junit.jupiter.api.Test;
import video.to.image.management_ms.core.application.ports.out.VideoProcessRepositoryOutputPort;
import video.to.image.management_ms.core.domain.entities.VideoProcess;
import video.to.image.management_ms.core.domain.enums.VideoProcessingStatus;
import video.to.image.management_ms.core.domain.exceptions.NotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VideoStatusQueueUseCaseTest {

    private final VideoProcessRepositoryOutputPort videoProcessRepository = mock(VideoProcessRepositoryOutputPort.class);
    private final VideoStatusQueueUseCase useCase = new VideoStatusQueueUseCase(videoProcessRepository);

    @Test
    void shouldUpdateVideoStatusFromQueueEvent() {
        UUID videoProcessId = UUID.randomUUID();
        VideoProcess videoProcess = new VideoProcess();
        videoProcess.setId(videoProcessId);
        videoProcess.setStatus(VideoProcessingStatus.RECEIVED);

        when(videoProcessRepository.findById(videoProcessId)).thenReturn(Optional.of(videoProcess));

        useCase.updateStatus(videoProcessId, VideoProcessingStatus.PROCESSED, "generated/video.zip", "video.zip");

        assertThat(videoProcess.getStatus()).isEqualTo(VideoProcessingStatus.PROCESSED);
        assertThat(videoProcess.getZipStorageKey()).isEqualTo("generated/video.zip");
        assertThat(videoProcess.getZipFileName()).isEqualTo("video.zip");
        assertThat(videoProcess.getUpdatedAt()).isNotNull();
        verify(videoProcessRepository).save(videoProcess);
    }

    @Test
    void shouldRejectStatusUpdateWhenVideoProcessDoesNotExist() {
        UUID videoProcessId = UUID.randomUUID();
        when(videoProcessRepository.findById(videoProcessId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.updateStatus(
                videoProcessId,
                VideoProcessingStatus.PROCESSED,
                "generated/video.zip",
                "video.zip"
        ))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Processamento de video nao encontrado");

        verify(videoProcessRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
