package com.example.smartlearn.dto;

import lombok.Data;

@Data
public class KnowledgePointDTO {
    private Long id;
    private String name;
    private String description;
    private Long courseId;
    private Long parentId;
    private Double positionX;
    private Double positionY;
}