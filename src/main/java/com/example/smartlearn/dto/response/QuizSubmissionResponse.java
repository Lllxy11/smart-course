package com.example.smartlearn.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 提交历史响应DTO
 */
@Data
public class QuizSubmissionResponse {
    
    /**
     * 提交记录ID
     */
    private Long submissionId;
    
    /**
     * 任务ID
     */
    private Long taskId;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 课程名称
     */
    private String courseName;
    
    /**
     * 测验标题
     */
    private String quizTitle;
    
    /**
     * 提交时间
     */
    private LocalDateTime submittedAt;
    
    /**
     * 得分
     */
    private BigDecimal grade;
    
    /**
     * 满分
     */
    private BigDecimal maxScore;
    
    /**
     * 教师评语
     */
    private String feedback;
    
    /**
     * 状态（submitted/graded）
     */
    private String status;
} 