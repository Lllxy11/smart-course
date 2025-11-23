package com.example.smartlearn.dto.studentgraph;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResourceDTO {
    private Long id;
    private String name;
    private String type;
}

