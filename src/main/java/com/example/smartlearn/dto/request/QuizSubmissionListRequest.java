package com.example.smartlearn.dto.request;

import lombok.Data;

/**
 * 提交历史列表请求DTO
 */
@Data
public class QuizSubmissionListRequest {
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 课程ID（可选，用于筛选）
     */
    private Long courseId;
    
    /**
     * 页码（可选，默认0）
     */
    private Integer page = 0;
    
    /**
     * 每页大小（可选，默认10）
     */
    private Integer size = 10;
} 