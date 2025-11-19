package com.example.smartlearn.controller.video;

import com.example.smartlearn.exception.ResourceNotFoundException;
import com.example.smartlearn.model.ClassResource;
import com.example.smartlearn.repository.ClassResourceRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final ClassResourceRepository resourceRepository;

    @GetMapping("/{videoId}")
    public ResponseEntity<VideoDetailDTO> getVideoDetails(
            @PathVariable Long videoId
    ) {
        ClassResource resource = resourceRepository.findByResourceId(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("视频不存在"));
        System.out.println('A');
        System.out.println(videoId);

        return ResponseEntity.ok(new VideoDetailDTO(
                resource.getResourceId(),
                resource.getName(),
                resource.getUrl(),
                resource.getDuration(),
                resource.getDescription()
        ));
    }

    @Data
    @AllArgsConstructor
    public static class VideoDetailDTO {
        private Long videoId;
        private String title;
        private String url;
        private Integer durationSeconds;
        private String description;
    }
}