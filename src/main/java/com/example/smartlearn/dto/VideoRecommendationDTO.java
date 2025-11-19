package com.example.smartlearn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoRecommendationDTO {
    private Long resourceId;
    private String title;
    private String url;
    private Integer durationSeconds;
    private Double completionRate;
    private String recommendationReason;


    public String getFormattedDuration() {
        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public String getName() {
        return  title;
    }
}