package com.example.smartlearn.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 该DTO类用于学生端试卷答案响应。
 * 包含试卷答案的完整信息。
 */
@Data
public class QuizAnswersResponse {
    
    /**
     * 试卷ID
     * 该字段用于学生端试卷答案的试卷标识
     */
    private Long quizId;
    
    /**
     * 试卷标题
     * 该字段用于学生端试卷答案的试卷标题展示
     */
    private String quizTitle;
    
    /**
     * 答案列表
     * 该字段用于学生端试卷答案的题目答案信息
     */
    private List<QuestionAnswer> answers;
    
    /**
     * 题目答案内部类
     * 该内部类用于学生端试卷答案的题目答案详细信息
     */
    @Data
    public static class QuestionAnswer {
        
        /**
         * 题目ID
         * 该字段用于学生端试卷答案的题目标识
         */
        private Long questionId;
        
        /**
         * 答案内容
         * 该字段用于学生端试卷答案的答案内容展示
         */
        private String answer;
        
        /**
         * 题目分数
         * 该字段用于学生端试卷答案的题目分值展示
         */
        private Integer score;
    }
} 