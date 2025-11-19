package com.example.smartlearn.dto.request;

import lombok.Data;

/**
 * 提交详情请求DTO
 */
@Data
public class QuizSubmissionDetailRequest {
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 提交记录ID
     */
    private Long submissionId;
} 