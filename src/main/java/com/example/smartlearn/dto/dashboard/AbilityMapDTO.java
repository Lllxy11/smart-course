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
public class AbilityMapDTO {
    private List<AbilityPointDTO> abilities;
    private double overallScore;
    private String updatedAt;
}

