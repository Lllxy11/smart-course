package com.example.smartlearn.dto.response;

import com.example.smartlearn.model.Question;
import lombok.Data;

/**
 * 该DTO类用于题库管理的题目信息响应。
 * 包含题目的完整信息，用于前端展示和操作。
 */
@Data
public class QuestionResponse {
    
    /**
     * 题目ID
     * 该字段用于题库管理的题目唯一标识
     */
    private Long id;
    
    /**
     * 课程ID
     * 该字段用于题库管理的题目所属课程标识
     */
    private Long courseId;
    
    /**
     * 课程名称
     * 该字段用于题库管理的课程信息展示
     */
    private String courseName;
    
    /**
     * 知识点ID（可选）
     * 该字段用于题库管理的题目所属知识点标识
     */
    private Long knowledgePointId;
    
    /**
     * 知识点名称（可选）
     * 该字段用于题库管理的知识点信息展示
     */
    private String knowledgePointName;
    
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
    
    /**
     * 构造函数
     * 该构造函数用于题库管理的从Question实体转换为响应对象
     */
    public QuestionResponse(Question question) {
        this.id = question.getId();
        this.courseId = question.getCourse().getCourseId();
        this.courseName = question.getCourse().getName();
        
        if (question.getKnowledgePoint() != null) {
            this.knowledgePointId = question.getKnowledgePoint().getId();
            this.knowledgePointName = question.getKnowledgePoint().getName();
        }
        
        this.type = question.getType().name();
        this.body = question.getBody();
        this.difficulty = question.getDifficulty();
    }
} 