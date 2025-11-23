package com.example.smartlearn.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseProgressItemDTO {
    private long courseId;
    private String courseName;
    private int progress;
    private int totalResources;
    private int completedResources;
    private int totalTasks;
    private int completedTasks;
}

