package com.example.smartlearn.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 题目难度动态调整-曹雨荷部分
 * 响应体DTO：用于题目难度动态调整的查询和确认结果
 */
public class QuestionDifficultyAdjustResponse {
    @Data
    public static class QueryResult {
        private List<QuestionStat> questions;
        private Statistics statistics;
        @Data
        public static class QuestionStat {
            private Long id;
            private String body;
            private Integer currentDifficulty;
            private Integer suggestedDifficulty;
            private Double correctRate;
            private Integer totalAttempts;
            private Integer wrongAttempts;
            private Map<String, Integer> optionStats;
            private String courseName;
            private String knowledgePointName;
            private String changeReason;
        }
        @Data
        public static class Statistics {
            private Integer totalQuestions;
            private Integer needAdjustment;
            private Double averageCorrectRate;
        }
    }

    @Data
    public static class ConfirmResult {
        private Integer successCount;
        private Integer failedCount;
        private List<Detail> details;
        @Data
        public static class Detail {
            private Long questionId;
            private String status; // success/failed/skipped
            private Integer oldDifficulty;
            private Integer newDifficulty;
        }
    }
} 