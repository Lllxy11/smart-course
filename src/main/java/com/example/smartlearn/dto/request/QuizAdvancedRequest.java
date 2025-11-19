package com.example.smartlearn.dto.request;

import java.util.List;

/**
 * 该类用于组卷管理的高级功能请求体
 */
public class QuizAdvancedRequest {
    /**
     * 替换题目请求体
     */
    public static class ReplaceQuestionRequest {
        public Long quizId;
        public Long oldQuestionId;
        public Long newQuestionId;
        public Integer score;
        public Integer orderIndex;
    }

    /**
     * 批量排序请求体
     */
    public static class ReorderQuestionsRequest {
        public Long quizId;
        public List<QuestionOrder> questionOrderList;
        public static class QuestionOrder {
            public Long questionId;
            public Integer orderIndex;
        }
    }

    /**
     * 智能组卷请求体
     */
    public static class AutoGenerateQuizRequest {
        public Long courseId;
        public List<Long> knowledgePointIds;
        public List<String> typeList;
        public Integer minDifficulty;
        public Integer maxDifficulty;
        public Integer totalQuestions;
        public Integer totalPoints;
        public String quizTitle;
    }
} 