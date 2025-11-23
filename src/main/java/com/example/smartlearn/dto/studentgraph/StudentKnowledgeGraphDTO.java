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
public class StudentKnowledgeGraphDTO {
    private List<StudentKnowledgeNodeDTO> nodes;
    private List<StudentKnowledgeEdgeDTO> edges;
}

