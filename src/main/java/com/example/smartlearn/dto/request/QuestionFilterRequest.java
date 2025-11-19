package com.example.smartlearn.dto.request;

import lombok.Data;

/**
 * 该DTO类用于题库管理的题目筛选请求。
 * 包含筛选条件，如课程ID、知识点ID、题目类型、难度、关键词等。
 */
@Data
public class QuestionFilterRequest {
    
    /**
     * 课程ID（可选）
     * 该字段用于题库管理的按课程筛选功能
     */
    private Long courseId;
    
    /**
     * 知识点ID（可选）
     * 该字段用于题库管理的按知识点筛选功能
     */
    private Long knowledgePointId;
    
    /**
     * 题目类型（可选）
     * 该字段用于题库管理的按题目类型筛选功能
     */
    private String type;
    
    /**
     * 题目难度（可选）
     * 该字段用于题库管理的按难度筛选功能
     */
    private Integer difficulty;
    
    /**
     * 关键词（可选）
     * 该字段用于题库管理的关键词搜索功能
     */
    private String keyword;
    
    /**
     * 页码
     * 该字段用于题库管理的分页功能
     */
    private Integer page = 0;
    
    /**
     * 每页大小
     * 该字段用于题库管理的分页功能
     */
    private Integer size = 10;
} 