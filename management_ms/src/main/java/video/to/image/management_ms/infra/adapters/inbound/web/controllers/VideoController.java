package video.to.image.management_ms.infra.adapters.inbound.web.controllers;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import video.to.image.management_ms.core.application.ports.in.VideoManagementUseCaseInputPort;
import video.to.image.management_ms.core.domain.entities.StoredVideo;
import video.to.image.management_ms.core.domain.entities.VideoProcess;
import video.to.image.management_ms.infra.adapters.inbound.web.dtos.VideoStatusResponse;
import video.to.image.management_ms.infra.adapters.inbound.web.dtos.VideoUploadResponse;
import video.to.image.management_ms.infra.security.AuthenticatedUser;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/videos")
public class VideoController {

    private final VideoManagementUseCaseInputPort videoManagementUseCase;

    public VideoController(VideoManagementUseCaseInputPort videoManagementUseCase) {
        this.videoManagementUseCase = videoManagementUseCase;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VideoUploadResponse> upload(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        VideoProcess videoProcess = videoManagementUseCase.upload(
                user.userId(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                file.getInputStream()
        );

        return ResponseEntity.accepted().body(toUploadResponse(videoProcess));
    }

    @GetMapping("/{fileName}/download")
    public ResponseEntity<InputStreamResource> download(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String fileName
    ) {
        StoredVideo storedVideo = videoManagementUseCase.download(user.userId(), fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(storedVideo.contentType()))
                .contentLength(storedVideo.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(storedVideo.fileName())
                        .build()
                        .toString())
                .body(new InputStreamResource(storedVideo.content()));
    }

    @GetMapping("/{fileName}/status")
    public ResponseEntity<VideoStatusResponse> status(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String fileName
    ) {
        return ResponseEntity.ok(toStatusResponse(videoManagementUseCase.getStatus(user.userId(), fileName)));
    }

    @GetMapping("/status")
    public ResponseEntity<List<VideoStatusResponse>> listStatus(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(videoManagementUseCase.listStatus(user.userId())
                .stream()
                .map(this::toStatusResponse)
                .toList());
    }

    private VideoUploadResponse toUploadResponse(VideoProcess videoProcess) {
        return new VideoUploadResponse(
                videoProcess.getId(),
                videoProcess.getUserId(),
                videoProcess.getOriginalFileName(),
                videoProcess.getZipFileName(),
                videoProcess.getZipStorageKey(),
                videoProcess.getStatus()
        );
    }

    private VideoStatusResponse toStatusResponse(VideoProcess videoProcess) {
        return new VideoStatusResponse(
                videoProcess.getId(),
                videoProcess.getUserId(),
                videoProcess.getOriginalFileName(),
                videoProcess.getZipFileName(),
                videoProcess.getZipStorageKey(),
                videoProcess.getStatus(),
                videoProcess.getUpdatedAt()
        );
    }
}
