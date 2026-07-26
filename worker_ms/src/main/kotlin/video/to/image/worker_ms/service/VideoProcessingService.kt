package video.to.image.worker_ms.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import video.to.image.worker_ms.messaging.ProcessVideoEvent
import video.to.image.worker_ms.messaging.VideoProcessingStatus
import video.to.image.worker_ms.messaging.VideoStatusEvent
import video.to.image.worker_ms.messaging.VideoStatusPublisher
import video.to.image.worker_ms.processing.FrameExtractor
import video.to.image.worker_ms.processing.ZipService
import video.to.image.worker_ms.storage.S3StorageService
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists

@Service
class VideoProcessingService(
    private val s3StorageService: S3StorageService,
    private val frameExtractor: FrameExtractor,
    private val zipService: ZipService,
    private val videoStatusPublisher: VideoStatusPublisher,
    @Value("\${app.processing.work-dir}") private val workDir: String,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun process(event: ProcessVideoEvent) {
        val jobDir = Path.of(workDir, event.videoProcessId.toString())
        try {
            publishStatus(event, VideoProcessingStatus.PROCESSING, null, null)

            Files.createDirectories(jobDir)
            val videoPath = jobDir.resolve(sanitizeFileName(event.originalFileName))
            val framesDir = jobDir.resolve("frames")
            val zipFileName = event.outputZipKey.substringAfterLast('/')
            val zipPath = jobDir.resolve(zipFileName)

            log.info("Downloading video from s3://{}/{}", event.bucket, event.storageKey)
            s3StorageService.download(event.bucket, event.storageKey, videoPath)

            frameExtractor.extract(videoPath, framesDir)
            zipService.zipDirectory(framesDir, zipPath)

            log.info("Uploading zip to s3://{}/{}", event.bucket, event.outputZipKey)
            s3StorageService.upload(event.bucket, event.outputZipKey, zipPath, "application/zip")

            publishStatus(event, VideoProcessingStatus.PROCESSED, event.outputZipKey, zipFileName)
            log.info("Video processing completed: videoProcessId={}", event.videoProcessId)
        } catch (ex: Exception) {
            log.error("Video processing failed: videoProcessId={}", event.videoProcessId, ex)
            publishStatus(event, VideoProcessingStatus.FAILED, null, null)
        } finally {
            cleanup(jobDir)
        }
    }

    private fun publishStatus(
        event: ProcessVideoEvent,
        status: VideoProcessingStatus,
        zipStorageKey: String?,
        zipFileName: String?,
    ) {
        videoStatusPublisher.publish(
            VideoStatusEvent(
                videoProcessId = event.videoProcessId,
                status = status,
                zipStorageKey = zipStorageKey,
                zipFileName = zipFileName,
            )
        )
    }

    private fun sanitizeFileName(fileName: String): String {
        val sanitized = fileName.substringAfterLast('/').substringAfterLast('\\')
        return sanitized.ifBlank { "video.bin" }
    }

    private fun cleanup(jobDir: Path) {
        if (!Files.exists(jobDir)) {
            return
        }
        try {
            Files.walk(jobDir)
                .sorted(Comparator.reverseOrder())
                .forEach { it.deleteIfExists() }
        } catch (ex: Exception) {
            log.warn("Failed to cleanup work dir {}: {}", jobDir, ex.message)
        }
    }
}
