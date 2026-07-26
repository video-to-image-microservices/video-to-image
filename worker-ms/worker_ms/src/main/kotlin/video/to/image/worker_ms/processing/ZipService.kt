package video.to.image.worker_ms.processing

import org.springframework.stereotype.Service
import java.io.BufferedOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Service
class ZipService {

    fun zipDirectory(sourceDir: Path, zipPath: Path) {
        Files.createDirectories(zipPath.parent)
        ZipOutputStream(BufferedOutputStream(Files.newOutputStream(zipPath))).use { zipOut ->
            Files.list(sourceDir).use { stream ->
                stream
                    .filter { Files.isRegularFile(it) }
                    .sorted()
                    .forEach { file ->
                        zipOut.putNextEntry(ZipEntry(file.fileName.toString()))
                        Files.copy(file, zipOut)
                        zipOut.closeEntry()
                    }
            }
        }
    }
}
