package com.example.smartlearn.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 课程成绩报表响应DTO
 * 基于现有表结构，无需创建新表
 */
@Data
public class CourseGradeReportResponse {
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 课程名称
     */
    private String courseName;
    
    /**
     * 课程代码
     */
    private String courseCode;
    
    /**
     * 教师姓名
     */
    private String teacherName;
    
    /**
     * 生成时间
     */
    private LocalDateTime generatedAt;
    
    /**
     * 学生成绩列表
     */
    private List<StudentGradeInfo> studentGrades;
    
    /**
     * 课程统计信息
     */
    private CourseStatistics courseStatistics;
    
    /**
     * 任务统计信息
     */
    private List<TaskStatistics> taskStatistics;
    
    /**
     * 学生成绩信息内部类
     */
    @Data
    public static class StudentGradeInfo {
        private Long studentId;
        private String studentName;
        private String studentNumber;
        private BigDecimal totalGrade;
        private BigDecimal maxGrade;
        private Double gradePercentage;
        private String gradeLevel;
        private Integer rank;
        private Integer completedTasks;
        private Integer totalTasks;
        private Double completionRate;
        private List<TaskGradeDetail> taskGrades;
    }
    
    /**
     * 任务成绩详情内部类
     */
    @Data
    public static class TaskGradeDetail {
        private Long taskId;
        private String taskTitle;
        private String taskType;
        private LocalDateTime dueDate;
        private LocalDateTime submittedAt;
        private BigDecimal score;
        private BigDecimal maxScore;
        private Double percentage;
        private String status; // completed, late, not_submitted
    }
    
    /**
     * 课程统计信息内部类
     */
    @Data
    public static class CourseStatistics {
        private Integer totalStudents;
        private Integer enrolledStudents;
        private BigDecimal classAverage;
        private BigDecimal highestGrade;
        private BigDecimal lowestGrade;
        private BigDecimal standardDeviation;
        private Map<String, Integer> gradeDistribution;
        private Map<String, Integer> completionRateDistribution;
    }
    
    /**
     * 任务统计信息内部类
     */
    @Data
    public static class TaskStatistics {
        private Long taskId;
        private String taskTitle;
        private String taskType;
        private LocalDateTime dueDate;
        private Integer totalStudents;
        private Integer submittedStudents;
        private Integer completedStudents;
        private BigDecimal averageScore;
        private BigDecimal maxScore;
        private Double completionRate;
        private Double averagePercentage;
        private Map<String, Integer> scoreDistribution;
    }
} 