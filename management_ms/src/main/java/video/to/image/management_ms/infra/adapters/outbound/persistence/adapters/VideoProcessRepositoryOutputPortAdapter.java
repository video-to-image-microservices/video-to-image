package video.to.image.management_ms.infra.adapters.outbound.persistence.adapters;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import video.to.image.management_ms.core.application.ports.out.VideoProcessRepositoryOutputPort;
import video.to.image.management_ms.core.domain.entities.VideoProcess;
import video.to.image.management_ms.infra.adapters.outbound.persistence.jparepositories.JpaVideoProcessRepository;
import video.to.image.management_ms.infra.adapters.outbound.persistence.mappers.JpaVideoProcessMapper;
import video.to.image.management_ms.infra.cache.RedisCacheService;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class VideoProcessRepositoryOutputPortAdapter implements VideoProcessRepositoryOutputPort {

    private static final String ID_CACHE_PREFIX = "management-ms:video-process:id:";
    private static final String USER_FILE_CACHE_PREFIX = "management-ms:video-process:user-file:";
    private static final String USER_LIST_CACHE_PREFIX = "management-ms:video-process:user-list:";

    private final JpaVideoProcessRepository repository;
    private final RedisCacheService redisCacheService;
    private final Duration cacheTtl;

    public VideoProcessRepositoryOutputPortAdapter(
            JpaVideoProcessRepository repository,
            RedisCacheService redisCacheService,
            @Value("${app.cache.video-process.ttl}") Duration cacheTtl
    ) {
        this.repository = repository;
        this.redisCacheService = redisCacheService;
        this.cacheTtl = cacheTtl;
    }

    @Override
    @Transactional
    public VideoProcess save(VideoProcess videoProcess) {
        VideoProcess saved = JpaVideoProcessMapper.toDomain(repository.save(JpaVideoProcessMapper.toJpa(videoProcess)));
        cacheVideoProcess(saved);
        redisCacheService.evict(userListKey(saved.getUserId()));
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VideoProcess> findByUserIdAndOriginalFileName(UUID userId, String originalFileName) {
        String cacheKey = userFileKey(userId, originalFileName);
        Optional<VideoProcess> cached = redisCacheService.get(cacheKey);
        if (cached.isPresent()) {
            return cached;
        }

        Optional<VideoProcess> found = repository.findTopByUserIdAndOriginalFileNameOrderByCreatedAtDesc(userId, originalFileName)
                .map(JpaVideoProcessMapper::toDomain);
        found.ifPresent(this::cacheVideoProcess);
        return found;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoProcess> findByUserId(UUID userId) {
        String cacheKey = userListKey(userId);
        Optional<List<VideoProcess>> cached = redisCacheService.get(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }

        List<VideoProcess> videos = repository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(JpaVideoProcessMapper::toDomain)
                .toList();
        redisCacheService.put(cacheKey, videos, cacheTtl);
        videos.forEach(this::cacheVideoProcess);
        return videos;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VideoProcess> findById(UUID id) {
        String cacheKey = idKey(id);
        Optional<VideoProcess> cached = redisCacheService.get(cacheKey);
        if (cached.isPresent()) {
            return cached;
        }

        Optional<VideoProcess> found = repository.findById(id).map(JpaVideoProcessMapper::toDomain);
        found.ifPresent(this::cacheVideoProcess);
        return found;
    }

    private void cacheVideoProcess(VideoProcess videoProcess) {
        redisCacheService.put(idKey(videoProcess.getId()), videoProcess, cacheTtl);
        redisCacheService.put(userFileKey(videoProcess.getUserId(), videoProcess.getOriginalFileName()), videoProcess, cacheTtl);
    }

    private String idKey(UUID id) {
        return ID_CACHE_PREFIX + id;
    }

    private String userFileKey(UUID userId, String fileName) {
        return USER_FILE_CACHE_PREFIX + userId + ":" + fileName;
    }

    private String userListKey(UUID userId) {
        return USER_LIST_CACHE_PREFIX + userId;
    }
}