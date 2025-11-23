package com.example.smartlearn.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningProgressDTO {
    private int totalCourses;
    private int enrolledCourses;
    private int completedCourses;
    private int inProgressCourses;
    @Builder.Default
    private List<CourseProgressItemDTO> courseProgress = new ArrayList<>();
}

