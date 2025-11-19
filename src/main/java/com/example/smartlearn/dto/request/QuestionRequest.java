package com.example.smartlearn.dto.request;

import lombok.Data;

/**
 * 该DTO类用于题库管理的题目创建和更新请求。
 * 包含题目的基本信息，如课程ID、知识点ID、题目类型、题目内容、难度等。
 */
@Data
public class QuestionRequest {
    
    /**
     * 课程ID
     * 该字段用于题库管理的题目所属课程标识
     */
    private Long courseId;
    
    /**
     * 知识点ID（可选）
     * 该字段用于题库管理的题目所属知识点标识
     */
    private Long knowledgePointId;
    
    /**
     * 题目类型
     * 该字段用于题库管理的题目类型分类
     */
    private String type;
    
    /**
     * 题目内容
     * 该字段用于题库管理的题目具体内容
     */
    private String body;
    
    /**
     * 题目难度
     * 该字段用于题库管理的题目难度等级
     */
    private Integer difficulty;
} 