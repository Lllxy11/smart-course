package com.example.smartlearn.controller.teacher;

import com.example.smartlearn.dto.response.CourseGradeReportResponse;
import com.example.smartlearn.dto.response.GradeAnalysisResponse;
import com.example.smartlearn.repository.CourseRepository;
import com.example.smartlearn.service.teacher.GradeAnalysisService;
import com.example.smartlearn.service.teacher.GradeReportExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 成绩分析控制器
 * 基于现有表结构，无需创建新表
 */
@RestController
@RequestMapping("/api/grade-analysis")
@CrossOrigin(origins = "*")
public class GradeAnalysisController {

    @Autowired
    private GradeAnalysisService gradeAnalysisService;

    @Autowired
    private GradeReportExportService gradeReportExportService;

    @Autowired
    private CourseRepository courseRepository;

    /**
     * 获取学生成绩分析（教师端）
     */
    @GetMapping("/student/{studentId}/course/{courseId}")
    public ResponseEntity<GradeAnalysisResponse> getStudentGradeAnalysis(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        try {
            GradeAnalysisResponse analysis = gradeAnalysisService.getStudentGradeAnalysis(studentId, courseId);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取课程成绩报表
     */
    @GetMapping("/course/{courseId}/report")
    public ResponseEntity<CourseGradeReportResponse> getCourseGradeReport(
            @PathVariable Long courseId) {
        try {
            CourseGradeReportResponse report = gradeAnalysisService.getCourseGradeReport(courseId);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 导出课程成绩报表
     */
    @GetMapping("/course/{courseId}/export")
    public ResponseEntity<byte[]> exportCourseGradeReport(@PathVariable Long courseId) {
        try {
            System.out.println("开始导出课程 " + courseId + " 的成绩报表");
            byte[] excelData = gradeReportExportService.exportCourseGradeReport(courseId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment",
                    String.format("course_grade_report_%d.xlsx", courseId));

            System.out.println("导出成功，文件大小: " + excelData.length + " 字节");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (Exception e) {
            System.err.println("导出失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /*
    // 暂时注释掉，因为服务中还没有实现这些方法
    @PostMapping("/multi-course/analysis")
    public ResponseEntity<Map<String, Object>> getMultiCourseGradeAnalysis(@RequestBody List<Long> courseIds) {
        try {
            Map<String, Object> analysis = gradeAnalysisService.getMultiCourseGradeAnalysis(courseIds);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getGradeAnalysisOverview() {
        try {
            List<Long> allCourseIds = courseRepository.findAll().stream()
                    .map(Course::getCourseId)
                    .collect(Collectors.toList());
            
            Map<String, Object> overview = gradeAnalysisService.getMultiCourseGradeAnalysis(allCourseIds);
            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    */
} 