package com.example.smartlearn.dto.request;

import lombok.Data;

/**
 * 测验列表请求DTO
 */
@Data
public class QuizTaskListRequest {
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 课程ID（可选，用于筛选）
     */
    private Long courseId;
    
    /**
     * 状态筛选（可选，ALL/NOT_STARTED/COMPLETED）
     */
    private String status = "ALL";
    
    /**
     * 页码（可选，默认0）
     */
    private Integer page = 0;
    
    /**
     * 每页大小（可选，默认10）
     */
    private Integer size = 10;
} 