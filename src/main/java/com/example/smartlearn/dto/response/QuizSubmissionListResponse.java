package com.example.smartlearn.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 提交历史列表响应DTO
 */
@Data
public class QuizSubmissionListResponse {
    
    /**
     * 总记录数
     */
    private long total;
    
    /**
     * 总页数
     */
    private int totalPages;
    
    /**
     * 当前页码
     */
    private int currentPage;
    
    /**
     * 提交记录列表
     */
    private List<QuizSubmissionResponse> submissions;
} 