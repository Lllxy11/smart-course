package com.example.smartlearn.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 测验任务响应DTO
 */
@Data
public class QuizTaskResponse {
    
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
     * 截止时间
     */
    private LocalDateTime dueDate;
    
    /**
     * 状态（NOT_STARTED/COMPLETED）
     */
    private String status;
    
    /**
     * 题目总数
     */
    private Integer totalQuestions;
    
    /**
     * 总分
     */
    private Integer totalScore;
    
    /**
     * 提交记录ID（如果已完成）
     */
    private Long submissionId;
    
    /**
     * 提交时间（如果已完成）
     */
    private LocalDateTime submittedAt;
    
    /**
     * 得分（如果已完成）
     */
    private BigDecimal grade;
} 