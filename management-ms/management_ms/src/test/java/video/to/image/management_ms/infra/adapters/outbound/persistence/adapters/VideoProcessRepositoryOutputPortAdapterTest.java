package video.to.image.management_ms.infra.adapters.outbound.persistence.adapters;

import org.junit.jupiter.api.Test;
import video.to.image.management_ms.core.domain.entities.VideoProcess;
import video.to.image.management_ms.infra.adapters.outbound.persistence.jparepositories.JpaVideoProcessRepository;
import video.to.image.management_ms.infra.cache.RedisCacheService;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VideoProcessRepositoryOutputPortAdapterTest {

    private final JpaVideoProcessRepository repository = mock(JpaVideoProcessRepository.class);
    private final RedisCacheService redisCacheService = mock(RedisCacheService.class);
    private final VideoProcessRepositoryOutputPortAdapter adapter = new VideoProcessRepositoryOutputPortAdapter(
            repository,
            redisCacheService,
            Duration.ofMinutes(10)
    );

    @Test
    void shouldReturnVideoProcessFromRedisCacheWithoutQueryingPostgres() {
        UUID videoProcessId = UUID.randomUUID();
        VideoProcess cachedVideoProcess = new VideoProcess();
        cachedVideoProcess.setId(videoProcessId);

        when(redisCacheService.<VideoProcess>get("management-ms:video-process:id:" + videoProcessId))
                .thenReturn(Optional.of(cachedVideoProcess));

        Optional<VideoProcess> result = adapter.findById(videoProcessId);

        assertThat(result).contains(cachedVideoProcess);
        verify(repository, never()).findById(any());
    }
}