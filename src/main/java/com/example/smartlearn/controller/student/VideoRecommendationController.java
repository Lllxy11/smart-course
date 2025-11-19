package com.example.smartlearn.controller.student;

import com.example.smartlearn.dto.VideoRecommendationDTO;
import com.example.smartlearn.service.video.VideoRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations/videos")
@RequiredArgsConstructor
public class VideoRecommendationController {

    private final VideoRecommendationService videoRecommendationService;

    @GetMapping("/personalized/{studentId}")
    public ResponseEntity<List<VideoRecommendationDTO>> getPersonalizedRecommendations(
            @PathVariable Long studentId) {
        return ResponseEntity.ok(videoRecommendationService.recommendVideos(studentId));
    }

    @GetMapping("/replay/{studentId}")
    public ResponseEntity<List<VideoRecommendationDTO>> getReplayRecommendations(
            @PathVariable Long studentId) {
        return ResponseEntity.ok(videoRecommendationService.recommendReplayVideos(studentId));
    }
}