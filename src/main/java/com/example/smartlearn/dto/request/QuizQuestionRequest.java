package com.example.smartlearn.dto.request;

import lombok.Data;

import java.util.List;

/**
 * 该DTO类用于组卷管理的试卷题目操作请求。
 * 包含题目ID、分数、顺序等操作信息。
 */
@Data
public class QuizQuestionRequest {
    
    /**
     * 题目ID
     * 该字段用于组卷管理的题目标识
     */
    private Long questionId;
    
    /**
     * 题目分数
     * 该字段用于组卷管理的题目分值设置
     */
    private Integer score;
    
    /**
     * 题目顺序
     * 该字段用于组卷管理的题目排序设置
     */
    private Integer orderIndex;
    
    /**
     * 批量操作时的题目ID列表
     * 该字段用于组卷管理的批量题目操作
     */
    private List<Long> questionIds;
    
    /**
     * 批量操作时的分数列表
     * 该字段用于组卷管理的批量分数设置
     */
    private List<Integer> scores;
    
    /**
     * 批量操作时的顺序列表
     * 该字段用于组卷管理的批量顺序设置
     */
    private List<Integer> orderIndexes;
} 