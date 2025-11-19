package com.example.smartlearn.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 测验列表响应DTO
 */
@Data
public class QuizTaskListResponse {
    
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
     * 测验任务列表
     */
    private List<QuizTaskResponse> quizTasks;
} 