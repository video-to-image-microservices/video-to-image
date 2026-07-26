package video.to.image.worker_ms.messaging

import java.util.UUID

data class ProcessVideoEvent(
    val videoProcessId: UUID,
    val userId: UUID,
    val bucket: String,
    val storageKey: String,
    val outputZipKey: String,
    val originalFileName: String,
    val contentType: String,
)
