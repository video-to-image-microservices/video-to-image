package video.to.image.management_ms.infra.adapters.outbound.persistence.jpaentities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import video.to.image.management_ms.core.domain.enums.VideoProcessingStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "video_process")
@Getter
@Setter
@NoArgsConstructor
public class JpaVideoProcess {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false, unique = true)
    private String storageKey;

    @Column(nullable = false, unique = true)
    private String zipStorageKey;

    @Column(nullable = false)
    private String zipFileName;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VideoProcessingStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
}
