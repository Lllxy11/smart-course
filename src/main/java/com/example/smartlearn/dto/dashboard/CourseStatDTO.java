package com.example.smartlearn.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseStatDTO {
    private long courseId;
    private String courseName;
    private double averageScore;
    private int totalTasks;
    private int completedTasks;
    private int rank;
    private int totalStudents;
}

