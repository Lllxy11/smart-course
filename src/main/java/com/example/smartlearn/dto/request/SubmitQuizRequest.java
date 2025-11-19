package com.example.smartlearn.dto.request;

import lombok.Data;

import java.util.List;

/**
 * 提交试卷答案的请求DTO
 * 用于学生一次性提交所有答案
 */
@Data
public class SubmitQuizRequest {
    
    /**
     * 试卷ID
     */
    private Long quizId;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 任务ID（重要：关联到具体的考试任务）
     */
    private Long taskId;
    
    /**
     * 答案列表
     */
    private List<QuestionAnswer> answers;
    
    /**
     * 题目答案内部类
     */
    @Data
    public static class QuestionAnswer {
        
        /**
         * 题目ID
         */
        private Long questionId;
        
        /**
         * 答案内容
         * 单选题：选项字母（如"A"、"B"）
         * 多选题：选项字母数组（如["A","C"]）
         * 填空题：填空内容
         * 简答题：答案文本
         */
        private String answerContent;
        
        /**
         * 答题时间（可选，用于分析答题时长）
         */
        private Long answerTime; // 毫秒
    }
} 