package com.example.smartlearn.controller.student;

import com.example.smartlearn.dto.response.GradeAnalysisResponse;
import com.example.smartlearn.service.student.StudentGradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 学生成绩控制器
 * 基于现有表结构，无需创建新表
 */
@RestController
@RequestMapping("/api/student/grade")
public class StudentGradeController {

    @Autowired
    private StudentGradeService studentGradeService;

    /**
     * 获取学生个人成绩分析
     */
    @GetMapping("/analysis/{courseId}")
    public ResponseEntity<GradeAnalysisResponse> getPersonalGradeAnalysis(
            @PathVariable Long courseId,
            @RequestParam Long studentId) {
        try {
            GradeAnalysisResponse analysis = studentGradeService.getPersonalGradeAnalysis(studentId, courseId);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 