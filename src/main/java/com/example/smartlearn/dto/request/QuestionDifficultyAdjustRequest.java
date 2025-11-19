package com.example.smartlearn.dto.request;

import lombok.Data;

import java.util.List;

/**
 * 题目难度动态调整-曹雨荷部分
 * 请求体DTO：用于题目难度动态调整的查询和确认
 */
public class QuestionDifficultyAdjustRequest {
    /**
     * 查询题目难度调整建议时用
     */
    @Data
    public static class Query {
        private List<Long> courseIds; // 课程ID列表，空表示全部
        private Boolean includeAllCourses; // 是否包含全部课程
    }

    /**
     * 教师确认调整时用
     */
    @Data
    public static class Confirm {
        private List<Adjustment> adjustments;
        @Data
        public static class Adjustment {
            private Long questionId;
            private Integer newDifficulty; // 新难度星级
            private Boolean shouldAdjust;  // 是否调整
        }
    }
} 