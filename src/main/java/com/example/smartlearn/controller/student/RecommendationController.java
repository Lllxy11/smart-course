package com.example.smartlearn.controller.student;

import com.example.smartlearn.service.student.RecommendationService;
import com.example.smartlearn.service.student.WeightedResource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {
    private final RecommendationService recommendationService;

    /**
     * 获取学生的错题推荐资源
     * @param studentId 学生ID
     * @param limit 返回结果数量限制 (可选)
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<WeightedResource>> getRecommendations(
            @PathVariable Long studentId,
            @RequestParam(required = false) Integer limit) {

        List<WeightedResource> recommendations = recommendationService
                .getWeightedRecommendations(studentId);

        if (limit != null && limit > 0) {
            recommendations = recommendations.stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(recommendations);

    }
    @GetMapping("/student/{studentId}/course/{courseId}")
    public ResponseEntity<List<WeightedResource>> getCourseRecommendations(
            @PathVariable Long studentId,
            @PathVariable Long courseId,
            @RequestParam(required = false) Double minWeight) {

        List<WeightedResource> recommendations = recommendationService
                .getWeightedRecommendations(studentId, courseId);

        if (minWeight != null && minWeight > 0) {
            recommendations = recommendations.stream()
                    .filter(r -> r.getWeight() >= minWeight)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(recommendations);
    }
    @GetMapping("/student/{studentId}/top")
    public ResponseEntity<List<WeightedResource>> getTopRecommendations(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "5") int topN) {

        List<WeightedResource> recommendations = recommendationService
                .getWeightedRecommendations(studentId);

        return ResponseEntity.ok(
                recommendations.stream()
                        .limit(Math.max(1, topN))
                        .collect(Collectors.toList())
        );
    }

}
