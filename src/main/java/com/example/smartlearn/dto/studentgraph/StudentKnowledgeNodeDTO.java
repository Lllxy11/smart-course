package com.example.smartlearn.dto.studentgraph;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentKnowledgeNodeDTO {
    private String id;
    private String title;
    private String description;
    private String type;
    private Long courseId;
    private List<Long> courseIds;
    private Double positionX;
    private Double positionY;
    private String learningStatus;
    private List<StudentResourceDTO> resources;
}

