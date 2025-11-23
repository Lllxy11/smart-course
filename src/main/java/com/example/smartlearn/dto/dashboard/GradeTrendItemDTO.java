package com.example.smartlearn.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeTrendItemDTO {
    private String date;
    private String courseName;
    private String taskName;
    private double score;
    private double maxScore;
    private double percentage;
}

