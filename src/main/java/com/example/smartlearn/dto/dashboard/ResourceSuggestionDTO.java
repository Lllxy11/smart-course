package com.example.smartlearn.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceSuggestionDTO {
    private String type;
    private String name;
    private String url;
    private Long resourceId;
}

