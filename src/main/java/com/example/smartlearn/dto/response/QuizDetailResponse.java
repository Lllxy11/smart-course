package com.example.smartlearn.dto.response;

import com.example.smartlearn.model.Quiz;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 该DTO类用于组卷管理的试卷详情响应。
 * 包含试卷的完整信息，包括题目列表和详细信息。
 */
@Data
public class QuizDetailResponse {
    
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
     * 题目列表
     * 该字段用于组卷管理的题目详细信息展示
     */
    private List<QuizQuestionDetail> questions;
    
    /**
     * 无参构造函数
     */
    public QuizDetailResponse() {
    }
    
    /**
     * 构造函数
     * 该构造函数用于组卷管理的从Quiz实体转换为详情响应对象
     */
    public QuizDetailResponse(Quiz quiz) {
        this.id = quiz.getId();
        this.title = quiz.getTitle();
        
        if (quiz.getCreator() != null) {
            this.creatorId = quiz.getCreator().getTeacherId();
            this.creatorName = quiz.getCreator().getName();
        }
        
        if (quiz.getCourse() != null) {
            this.courseId = quiz.getCourse().getCourseId();
            this.courseName = quiz.getCourse().getName();
        }
        
        this.totalPoints = quiz.getTotalPoints();
        
        if (quiz.getQuestions() != null) {
            this.questions = quiz.getQuestions().stream()
                    .map(question -> new QuizQuestionDetail(question, quiz))
                    .collect(Collectors.toList());
        }
    }
    
    /**
     * 试卷题目详情内部类
     * 该内部类用于组卷管理的题目详细信息展示
     */
    @Data
    public static class QuizQuestionDetail {
        
        /**
         * 题目ID
         * 该字段用于组卷管理的题目标识
         */
        private Long questionId;
        
        /**
         * 题目类型
         * 该字段用于组卷管理的题目类型展示
         */
        private String type;
        
        /**
         * 题目内容
         * 该字段用于组卷管理的题目内容展示
         */
        private String body;
        
        /**
         * 题目难度
         * 该字段用于组卷管理的题目难度展示
         */
        private Integer difficulty;
        
        /**
         * 题目分数
         * 该字段用于组卷管理的题目分值展示
         */
        private Integer score;
        
        /**
         * 题目顺序
         * 该字段用于组卷管理的题目排序展示
         */
        private Integer orderIndex;
        
        /**
         * 构造函数
         * 该构造函数用于组卷管理的从Question实体转换为题目详情对象
         */
        public QuizQuestionDetail(com.example.smartlearn.model.Question question) {
            this.questionId = question.getId();
            this.type = question.getType().name();
            this.body = question.getBody();
            this.difficulty = question.getDifficulty();
            // TODO: 从quiz_questions关联表中获取score和orderIndex
            // 这里需要根据实际的关联表结构来实现
        }
        
        /**
         * 构造函数（带Quiz参数）
         * 该构造函数用于组卷管理的从Question实体和Quiz实体转换为题目详情对象
         */
        public QuizQuestionDetail(com.example.smartlearn.model.Question question, com.example.smartlearn.model.Quiz quiz) {
            this.questionId = question.getId();
            this.type = question.getType().name();
            this.body = question.getBody();
            this.difficulty = question.getDifficulty();
            // 从Quiz的Map中获取分数和顺序信息
            this.score = quiz.getQuestionScore(question);
            this.orderIndex = quiz.getQuestionOrder(question);
        }
    }
} 