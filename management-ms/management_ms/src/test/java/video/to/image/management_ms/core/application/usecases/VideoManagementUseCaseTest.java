package video.to.image.management_ms.core.application.usecases;

import org.junit.jupiter.api.Test;
import video.to.image.management_ms.core.application.ports.out.ProcessQueueOutputPort;
import video.to.image.management_ms.core.application.ports.out.UserRepositoryOutputPort;
import video.to.image.management_ms.core.application.ports.out.VideoProcessRepositoryOutputPort;
import video.to.image.management_ms.core.application.ports.out.VideoStorageOutputPort;
import video.to.image.management_ms.core.domain.entities.StoredVideo;
import video.to.image.management_ms.core.domain.entities.VideoProcess;
import video.to.image.management_ms.core.domain.enums.VideoProcessingStatus;
import video.to.image.management_ms.core.domain.exceptions.NotFoundException;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VideoManagementUseCaseTest {

    private final UserRepositoryOutputPort userRepository = mock(UserRepositoryOutputPort.class);
    private final VideoProcessRepositoryOutputPort videoProcessRepository = mock(VideoProcessRepositoryOutputPort.class);
    private final VideoStorageOutputPort videoStorage = mock(VideoStorageOutputPort.class);
    private final ProcessQueueOutputPort processQueue = mock(ProcessQueueOutputPort.class);
    private final VideoManagementUseCase useCase = new VideoManagementUseCase(
            userRepository,
            videoProcessRepository,
            videoStorage,
            processQueue
    );

    @Test
    void shouldUploadVideoAndPublishProcessEvent() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(true);
        when(videoProcessRepository.save(any(VideoProcess.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VideoProcess result = useCase.upload(
                userId,
                "video.mp4",
                "video/mp4",
                4L,
                new ByteArrayInputStream(new byte[]{1, 2, 3, 4})
        );

        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getOriginalFileName()).isEqualTo("video.mp4");
        assertThat(result.getZipFileName()).isEqualTo("video.zip");
        assertThat(result.getStatus()).isEqualTo(VideoProcessingStatus.RECEIVED);
        assertThat(result.getStorageKey()).contains(userId.toString()).contains("video.mp4");
        assertThat(result.getZipStorageKey()).contains(userId.toString()).contains("generated/video.zip");

        verify(videoStorage).upload(eq(result.getStorageKey()), any(), eq(4L), eq("video/mp4"));
        verify(videoProcessRepository).save(any(VideoProcess.class));
        verify(processQueue).publish(result);
    }

    @Test
    void shouldRejectUploadWhenUserWasNotConsumedFromQueue() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> useCase.upload(
                userId,
                "video.mp4",
                "video/mp4",
                4L,
                new ByteArrayInputStream(new byte[]{1, 2, 3, 4})
        )).isInstanceOf(NotFoundException.class);

        verify(videoStorage, never()).upload(any(), any(), anyLong(), any());
        verify(videoProcessRepository, never()).save(any());
        verify(processQueue, never()).publish(any());
    }

    @Test
    void shouldReturnStatusForOwnedVideo() {
        UUID userId = UUID.randomUUID();
        VideoProcess videoProcess = new VideoProcess();
        videoProcess.setUserId(userId);
        videoProcess.setOriginalFileName("video.mp4");
        videoProcess.setStatus(VideoProcessingStatus.PROCESSING);

        when(videoProcessRepository.findByUserIdAndOriginalFileName(userId, "video.mp4"))
                .thenReturn(Optional.of(videoProcess));

        VideoProcess result = useCase.getStatus(userId, "video.mp4");

        assertThat(result.getStatus()).isEqualTo(VideoProcessingStatus.PROCESSING);
    }

    @Test
    void shouldListStatusForUserVideos() {
        UUID userId = UUID.randomUUID();
        VideoProcess videoProcess = new VideoProcess();
        videoProcess.setUserId(userId);
        videoProcess.setOriginalFileName("video.mp4");

        when(videoProcessRepository.findByUserId(userId)).thenReturn(List.of(videoProcess));

        List<VideoProcess> result = useCase.listStatus(userId);

        assertThat(result).containsExactly(videoProcess);
    }

    @Test
    void shouldDownloadProcessedZipFromS3InsteadOfOriginalVideo() {
        UUID userId = UUID.randomUUID();
        VideoProcess videoProcess = new VideoProcess();
        videoProcess.setUserId(userId);
        videoProcess.setOriginalFileName("video.mp4");
        videoProcess.setStorageKey("user/process/video.mp4");
        videoProcess.setZipStorageKey("user/process/generated/video.zip");
        videoProcess.setZipFileName("video.zip");
        videoProcess.setStatus(VideoProcessingStatus.PROCESSED);

        StoredVideo storedVideo = new StoredVideo(new ByteArrayInputStream(new byte[]{1}), "application/zip", 1L, "video.zip");
        when(videoProcessRepository.findByUserIdAndOriginalFileName(userId, "video.mp4"))
                .thenReturn(Optional.of(videoProcess));
        when(videoStorage.download("user/process/generated/video.zip", "video.zip")).thenReturn(storedVideo);

        StoredVideo result = useCase.download(userId, "video.mp4");

        assertThat(result.fileName()).isEqualTo("video.zip");
        verify(videoStorage).download("user/process/generated/video.zip", "video.zip");
    }

    @Test
    void shouldRejectDownloadWhenZipIsNotProcessedYet() {
        UUID userId = UUID.randomUUID();
        VideoProcess videoProcess = new VideoProcess();
        videoProcess.setUserId(userId);
        videoProcess.setOriginalFileName("video.mp4");
        videoProcess.setZipStorageKey("user/process/generated/video.zip");
        videoProcess.setZipFileName("video.zip");
        videoProcess.setStatus(VideoProcessingStatus.PROCESSING);

        when(videoProcessRepository.findByUserIdAndOriginalFileName(userId, "video.mp4"))
                .thenReturn(Optional.of(videoProcess));

        assertThatThrownBy(() -> useCase.download(userId, "video.mp4"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Zip ainda nao esta pronto");

        verify(videoStorage, never()).download(any(), any());
    }
}
