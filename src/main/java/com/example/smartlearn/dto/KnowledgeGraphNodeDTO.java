package com.example.smartlearn.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeGraphNodeDTO {
    private String id;
    private String title;
    private String description;
    private String type;
    private Long courseId;
    private Double positionX;
    private Double positionY;
    private List<ResourceDTO> resources;
}



