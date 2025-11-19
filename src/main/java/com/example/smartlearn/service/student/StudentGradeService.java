package com.example.smartlearn.service.student;

import com.example.smartlearn.dto.response.GradeAnalysisResponse;
import com.example.smartlearn.service.teacher.GradeAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 学生端成绩分析服务
 * 只暴露学生个人成绩分析相关方法
 */
@Service
public class StudentGradeService {

    @Autowired
    private GradeAnalysisService gradeAnalysisService;

    /**
     * 获取学生个人成绩分析（只允许查自己的成绩）
     */
    public GradeAnalysisResponse getPersonalGradeAnalysis(Long studentId, Long courseId) {
        System.out.println("[StudentGradeService] 查询成绩: studentId=" + studentId + ", courseId=" + courseId);
        try {
            GradeAnalysisResponse result = gradeAnalysisService.getStudentGradeAnalysis(studentId, courseId);
            System.out.println("[StudentGradeService] 查询成功: " + result);
            return result;
        } catch (Exception e) {
            System.err.println("[StudentGradeService] 查询异常: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
