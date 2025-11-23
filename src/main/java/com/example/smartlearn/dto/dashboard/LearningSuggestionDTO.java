package com.example.smartlearn.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningSuggestionDTO {
    private String id;
    private String knowledgePoint;
    private String suggestion;
    private String priority; // high/medium/low
    private List<ResourceSuggestionDTO> relatedResources;
}

