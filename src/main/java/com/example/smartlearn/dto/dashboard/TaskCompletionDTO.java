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
public class TaskCompletionDTO {
    private int totalTasks;
    private int completedTasks;
    private int pendingTasks;
    private int overdueTasks;
    private int completionRate;
    @Builder.Default
    private List<TaskTypeStatDTO> tasksByType = new ArrayList<>();
    @Builder.Default
    private List<TaskStatusStatDTO> tasksByStatus = new ArrayList<>();
}

