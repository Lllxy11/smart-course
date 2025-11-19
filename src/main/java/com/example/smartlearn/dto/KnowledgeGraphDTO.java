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
public class KnowledgeGraphDTO {
    private List<KnowledgeGraphNodeDTO> nodes;
    private List<KnowledgeGraphEdgeDTO> edges;
}