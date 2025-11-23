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
public class AILearningRecommendationDTO {
    private List<LearningSuggestionDTO> suggestions;
    private String generatedAt;
    private String analysisSummary;
}

