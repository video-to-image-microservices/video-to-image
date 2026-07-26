package video.to.image.worker_ms.storage

import org.springframework.stereotype.Service
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.nio.file.Files
import java.nio.file.Path

@Service
class S3StorageService(
    private val s3Client: S3Client,
) {

    fun download(bucket: String, key: String, destination: Path) {
        Files.createDirectories(destination.parent)
        val request = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()
        s3Client.getObject(request, destination)
    }

    fun upload(bucket: String, key: String, source: Path, contentType: String) {
        val request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .contentLength(Files.size(source))
            .build()
        s3Client.putObject(request, RequestBody.fromFile(source))
    }
}
