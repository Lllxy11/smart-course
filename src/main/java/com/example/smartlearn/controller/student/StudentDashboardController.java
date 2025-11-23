package com.example.smartlearn.controller.student;

import com.example.smartlearn.dto.dashboard.*;
import com.example.smartlearn.service.student.StudentDashboardFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/{studentId}/dashboard")
@RequiredArgsConstructor
public class StudentDashboardController {

    private final StudentDashboardFacade dashboardFacade;

    @GetMapping
    public ResponseEntity<StudentDashboardResponse> getDashboard(@PathVariable Long studentId) {
        return ResponseEntity.ok(dashboardFacade.getDashboard(studentId));
    }

    @GetMapping("/learning-progress")
    public ResponseEntity<LearningProgressDTO> getLearningProgress(@PathVariable Long studentId) {
        return ResponseEntity.ok(dashboardFacade.getLearningProgress(studentId));
    }

    @GetMapping("/grade-trend")
    public ResponseEntity<?> getGradeTrend(@PathVariable Long studentId,
                                           @RequestParam(value = "days", required = false) Integer days) {
        // days 参数目前未使用，保留以兼容前端
        return ResponseEntity.ok(dashboardFacade.getGradeTrend(studentId));
    }

    @GetMapping("/task-completion")
    public ResponseEntity<TaskCompletionDTO> getTaskCompletion(@PathVariable Long studentId) {
        return ResponseEntity.ok(dashboardFacade.getTaskCompletion(studentId));
    }

    @GetMapping("/ai-recommendations")
    public ResponseEntity<AILearningRecommendationDTO> getAiRecommendations(@PathVariable Long studentId) {
        return ResponseEntity.ok(dashboardFacade.getAiRecommendations(studentId));
    }

    @GetMapping("/ability-map")
    public ResponseEntity<AbilityMapDTO> getAbilityMap(@PathVariable Long studentId,
                                                       @RequestParam(value = "courseId", required = false) Long courseId) {
        return ResponseEntity.ok(dashboardFacade.getAbilityMap(studentId, courseId));
    }
}

