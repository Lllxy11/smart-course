package com.example.smartlearn.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentCourseStatusDTO {
    private Long courseId;
    private String code;
    private String courseName;
    private boolean selected;
}
