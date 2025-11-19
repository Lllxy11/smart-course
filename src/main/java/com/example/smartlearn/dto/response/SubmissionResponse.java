package com.example.smartlearn.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 提交试卷答案的响应DTO
 * 返回提交结果和评分信息
 */
@Data
public class SubmissionResponse {
    
    /**
     * 提交记录ID
     */
    private Long submissionId;
    
    /**
     * 试卷ID
     */
    private Long quizId;
    
    /**
     * 试卷标题
     */
    private String quizTitle;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 学生姓名
     */
    private String studentName;
    
    /**
     * 提交时间
     */
    private LocalDateTime submittedAt;
    
    /**
     * 总分
     */
    private BigDecimal totalScore;
    
    /**
     * 满分
     */
    private BigDecimal maxScore;
    
    /**
     * 提交状态
     */
    private String status; // "submitted", "graded", "grading"
    
    /**
     * 教师评语
     */
    private String feedback;
    
    /**
     * 题目结果列表
     */
    private List<QuestionResult> results;
    
    /**
     * 题目结果内部类
     */
    @Data
    public static class QuestionResult {
        
        /**
         * 题目ID
         */
        private Long questionId;
        
        /**
         * 题目类型
         */
        private String questionType;
        
        /**
         * 学生答案
         */
        private String studentAnswer;
        
        /**
         * 正确答案（仅客观题显示）
         */
        private String correctAnswer;
        
        /**
         * 是否答对（仅客观题）
         */
        private Boolean isCorrect;
        
        /**
         * 本题得分
         */
        private BigDecimal score;
        
        /**
         * 本题满分
         */
        private BigDecimal maxScore;
        
        /**
         * 教师评语（主观题）
         */
        private String feedback;
    }
} 