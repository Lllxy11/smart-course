package com.example.smartlearn.dto;

import lombok.Getter;
import lombok.Setter;

@Getter

@Setter
public class VideoEventDto {
    private Long studentId;
    private Long resourceId;
    private String eventType;
    private Integer videoTimestamp;
    private String sessionId;
    private Integer duration;
}
