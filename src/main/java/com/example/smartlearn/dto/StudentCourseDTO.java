package com.example.smartlearn.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentCourseDTO {
    private Long courseId;
    private String  code;
    private String courseName;

    public StudentCourseDTO(Long courseId,String code, String courseName) {
        this.courseId = courseId;
        this.code = code;
        this.courseName = courseName;
    }

    public String getCode() {
        return code;
    }
    public void setCode(String code) {}

    public String getCourseName() {
        return courseName;
    }
    public void setCourseName(String courseName) {

    }
}
