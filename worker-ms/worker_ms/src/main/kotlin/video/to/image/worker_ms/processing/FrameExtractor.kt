package video.to.image.worker_ms.processing

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path

@Service
class FrameExtractor(
    @Value("\${app.processing.fps}") private val fps: Int,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun extract(videoPath: Path, framesDir: Path) {
        Files.createDirectories(framesDir)
        val outputPattern = framesDir.resolve("frame_%04d.jpg").toAbsolutePath().toString()

        val command = listOf(
            "ffmpeg",
            "-y",
            "-i", videoPath.toAbsolutePath().toString(),
            "-vf", "fps=$fps",
            outputPattern,
        )

        log.info("Extracting frames with ffmpeg: fps={}, output={}", fps, framesDir)
        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw IllegalStateException("ffmpeg failed with exit code $exitCode: $output")
        }

        val frameCount = Files.list(framesDir).use { stream ->
            stream.filter { Files.isRegularFile(it) }.count()
        }
        if (frameCount == 0L) {
            throw IllegalStateException("ffmpeg produced no frames for $videoPath")
        }
        log.info("Extracted {} frame(s) from {}", frameCount, videoPath.fileName)
    }
}
