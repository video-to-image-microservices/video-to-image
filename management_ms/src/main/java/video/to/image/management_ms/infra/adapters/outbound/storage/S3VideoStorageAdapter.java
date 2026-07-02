package video.to.image.management_ms.infra.adapters.outbound.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import video.to.image.management_ms.core.application.ports.out.VideoStorageOutputPort;
import video.to.image.management_ms.core.domain.entities.StoredVideo;
import video.to.image.management_ms.infra.cache.CachedStoredVideo;
import video.to.image.management_ms.infra.cache.RedisCacheService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

@Component
public class S3VideoStorageAdapter implements VideoStorageOutputPort {

    private static final String S3_FILE_CACHE_PREFIX = "management-ms:s3-file:";

    private final S3Client s3Client;
    private final RedisCacheService redisCacheService;
    private final String bucketName;
    private final Duration cacheTtl;
    private final long maxCacheBytes;

    public S3VideoStorageAdapter(
            S3Client s3Client,
            RedisCacheService redisCacheService,
            @Value("${app.s3.video-bucket}") String bucketName,
            @Value("${app.cache.s3-files.ttl}") Duration cacheTtl,
            @Value("${app.cache.s3-files.max-bytes}") long maxCacheBytes
    ) {
        this.s3Client = s3Client;
        this.redisCacheService = redisCacheService;
        this.bucketName = bucketName;
        this.cacheTtl = cacheTtl;
        this.maxCacheBytes = maxCacheBytes;
    }

    @Override
    public void upload(String key, InputStream content, long contentLength, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .contentLength(contentLength)
                .build();

        s3Client.putObject(request, RequestBody.fromInputStream(content, contentLength));
        redisCacheService.evict(cacheKey(key));
    }

    @Override
    public StoredVideo download(String key, String fileName) {
        String cacheKey = cacheKey(key);
        return redisCacheService.<CachedStoredVideo>get(cacheKey)
                .map(this::toStoredVideo)
                .orElseGet(() -> downloadFromS3AndCache(key, fileName, cacheKey));
    }

    private StoredVideo downloadFromS3AndCache(String key, String fileName, String cacheKey) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try (ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request)) {
            byte[] content = response.readAllBytes();
            String contentType = response.response().contentType();
            long contentLength = content.length;

            if (contentLength <= maxCacheBytes) {
                redisCacheService.put(cacheKey, new CachedStoredVideo(content, contentType, contentLength, fileName), cacheTtl);
            }

            return new StoredVideo(new ByteArrayInputStream(content), contentType, contentLength, fileName);
        } catch (IOException ex) {
            throw new IllegalStateException("Erro ao ler arquivo do S3", ex);
        }
    }

    private StoredVideo toStoredVideo(CachedStoredVideo cached) {
        return new StoredVideo(
                new ByteArrayInputStream(cached.content()),
                cached.contentType(),
                cached.contentLength(),
                cached.fileName()
        );
    }

    private String cacheKey(String key) {
        return S3_FILE_CACHE_PREFIX + bucketName + ":" + key;
    }
}