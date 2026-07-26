package video.to.image.worker_ms.messaging

import java.util.UUID

data class VideoStatusEvent(
    val videoProcessId: UUID,
    val status: VideoProcessingStatus,
    val zipStorageKey: String?,
    val zipFileName: String?,
)
