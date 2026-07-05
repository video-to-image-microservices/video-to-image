package video.to.image.management_ms.infra.adapters.outbound.storage;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import video.to.image.management_ms.core.domain.entities.StoredVideo;
import video.to.image.management_ms.infra.cache.CachedStoredVideo;
import video.to.image.management_ms.infra.cache.RedisCacheService;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class S3VideoStorageAdapterTest {

    private final S3Client s3Client = mock(S3Client.class);
    private final RedisCacheService redisCacheService = mock(RedisCacheService.class);
    private final S3VideoStorageAdapter adapter = new S3VideoStorageAdapter(
            s3Client,
            redisCacheService,
            "videos-bucket",
            Duration.ofMinutes(5),
            1024L
    );

    @Test
    void shouldReturnStoredVideoFromRedisCacheWithoutQueryingS3() throws Exception {
        CachedStoredVideo cached = new CachedStoredVideo("zip".getBytes(), "application/zip", 3L, "video.zip");
        when(redisCacheService.<CachedStoredVideo>get("management-ms:s3-file:videos-bucket:user/video.zip"))
                .thenReturn(Optional.of(cached));

        StoredVideo result = adapter.download("user/video.zip", "video.zip");

        assertThat(result.fileName()).isEqualTo("video.zip");
        assertThat(result.contentType()).isEqualTo("application/zip");
        assertThat(result.content().readAllBytes()).isEqualTo("zip".getBytes());
        verify(s3Client, never()).getObject(any(GetObjectRequest.class));
    }
}