package com.example.smartlearn.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 成绩分析响应DTO
 * 基于现有表结构，无需创建新表
 */
@Data
public class GradeAnalysisResponse {
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 学生姓名
     */
    private String studentName;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 课程名称
     */
    private String courseName;
    
    /**
     * 当前总成绩
     */
    private BigDecimal currentGrade;
    
    /**
     * 满分
     */
    private BigDecimal maxGrade;
    
    /**
     * 成绩百分比
     */
    private Double gradePercentage;
    
    /**
     * 成绩等级
     */
    private String gradeLevel;
    
    /**
     * 班级排名
     */
    private Integer classRank;
    
    /**
     * 班级总人数
     */
    private Integer totalStudents;
    
    /**
     * 成绩趋势数据
     */
    private List<GradeTrendData> gradeTrend;
    
    /**
     * 各任务类型成绩分布
     */
    private Map<String, TaskTypeGrade> taskTypeGrades;
    
    /**
     * 成绩统计信息
     */
    private GradeStatistics statistics;
    
    /**
     * 学习建议
     */
    private List<String> learningSuggestions;
    
    /**
     * 完成度（已完成任务数/任务总数，百分比）
     */
    private Double completionRate;
    
    /**
     * 成绩趋势数据内部类
     */
    @Data
    public static class GradeTrendData {
        private LocalDateTime date;
        private String taskTitle;
        private BigDecimal score;
        private BigDecimal maxScore;
        private String taskType;
        private Double percentage;
    }
    
    /**
     * 任务类型成绩内部类
     */
    @Data
    public static class TaskTypeGrade {
        private String taskType;
        private BigDecimal averageScore;
        private BigDecimal maxScore;
        private Integer taskCount;
        private Double completionRate;
    }
    
    /**
     * 成绩统计信息内部类
     */
    @Data
    public static class GradeStatistics {
        private BigDecimal averageGrade;
        private BigDecimal highestGrade;
        private BigDecimal lowestGrade;
        private Integer completedTasks;
        private Integer totalTasks;
        private Double completionRate;
        private BigDecimal standardDeviation;
        private Map<String, Integer> gradeDistribution;
    }
} 