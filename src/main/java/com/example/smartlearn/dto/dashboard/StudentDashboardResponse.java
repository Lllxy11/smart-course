package com.example.smartlearn.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDashboardResponse {
    private LearningProgressDTO learningProgress;
    private List<GradeTrendItemDTO> gradeTrend;
    private TaskCompletionDTO taskCompletion;
    private List<CourseStatDTO> courseStats;
}

