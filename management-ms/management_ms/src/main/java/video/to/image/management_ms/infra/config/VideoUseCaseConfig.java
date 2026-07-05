package video.to.image.management_ms.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import video.to.image.management_ms.core.application.ports.out.ProcessQueueOutputPort;
import video.to.image.management_ms.core.application.ports.out.UserRepositoryOutputPort;
import video.to.image.management_ms.core.application.ports.out.VideoProcessRepositoryOutputPort;
import video.to.image.management_ms.core.application.ports.out.VideoStorageOutputPort;
import video.to.image.management_ms.core.application.usecases.VideoManagementUseCase;
import video.to.image.management_ms.core.application.usecases.VideoStatusQueueUseCase;

@Configuration
public class VideoUseCaseConfig {

    @Bean
    public VideoManagementUseCase videoManagementUseCase(
            UserRepositoryOutputPort userRepository,
            VideoProcessRepositoryOutputPort videoProcessRepository,
            VideoStorageOutputPort videoStorage,
            ProcessQueueOutputPort processQueue
    ) {
        return new VideoManagementUseCase(userRepository, videoProcessRepository, videoStorage, processQueue);
    }

    @Bean
    public VideoStatusQueueUseCase videoStatusQueueUseCase(VideoProcessRepositoryOutputPort videoProcessRepository) {
        return new VideoStatusQueueUseCase(videoProcessRepository);
    }
}
