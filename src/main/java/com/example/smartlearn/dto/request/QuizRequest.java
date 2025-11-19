package com.example.smartlearn.dto.request;

import lombok.Data;

/**
 * 该DTO类用于组卷管理的试卷创建和更新请求。
 * 包含试卷的基本信息，如标题、创建者ID、总分等。
 */
@Data
public class QuizRequest {
    
    /**
     * 试卷标题
     * 该字段用于组卷管理的试卷标题标识
     */
    private String title;
    
    /**
     * 创建者ID
     * 该字段用于组卷管理的创建者标识
     */
    private Long creatorId;
    /**
     * 课程ID
     * 该字段用于组卷管理的课程关联
     */
    private Long courseId;

    /**
     * 总分
     * 该字段用于组卷管理的试卷总分设置
     */
    private Integer totalPoints;
} 