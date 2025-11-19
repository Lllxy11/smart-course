package com.example.smartlearn.dto.response;

import com.example.smartlearn.model.Quiz;
import lombok.Data;

/**
 * 该DTO类用于组卷管理的试卷信息响应。
 * 包含试卷的基本信息，用于前端展示和操作。
 */
@Data
public class QuizResponse {

    /**
     * 试卷ID
     * 该字段用于组卷管理的试卷唯一标识
     */
    private Long id;

    /**
     * 试卷标题
     * 该字段用于组卷管理的试卷标题展示
     */
    private String title;

    /**
     * 创建者ID
     * 该字段用于组卷管理的创建者标识
     */
    private Long creatorId;

    /**
     * 创建者姓名
     * 该字段用于组卷管理的创建者信息展示
     */
    private String creatorName;

    /**
     * 课程ID
     * 该字段用于组卷管理的课程标识
     */
    private Long courseId;

    /**
     * 课程名称
     * 该字段用于组卷管理的课程信息展示
     */
    private String courseName;

    /**
     * 总分
     * 该字段用于组卷管理的试卷总分展示
     */
    private Integer totalPoints;

    /**
     * 题目数量
     * 该字段用于组卷管理的题目统计展示
     */
    private Integer questionCount;

    /**
     * 构造函数
     * 该构造函数用于组卷管理的从Quiz实体转换为响应对象
     */
    public QuizResponse(Quiz quiz) {
        this.id = quiz.getId();
        this.title = quiz.getTitle();
        this.creatorId = quiz.getCreator().getTeacherId();
        this.creatorName = quiz.getCreator().getName();
        this.courseId = quiz.getCourse() != null ? quiz.getCourse().getCourseId() : null;
        this.courseName = quiz.getCourse() != null ? quiz.getCourse().getName() : null;
        this.totalPoints = quiz.getTotalPoints();
        this.questionCount = quiz.getQuestions() != null ? quiz.getQuestions().size() : 0;
    }
} 