package com.example.smartlearn.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务智能批改结果展示响应DTO
 */
@Data
public class TaskAiScoreResultResponse {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 任务标题
     */
    private String taskTitle;

    /**
     * 任务描述
     */
    private String taskDescription;

    /**
     * 学生智能批改结果列表
     */
    private List<StudentAiScoreResult> studentResults;

    /**
     * 学生智能批改结果内部类
     */
    @Data
    public static class StudentAiScoreResult {
        /**
         * 学生ID
         */
        private Long studentId;

        /**
         * 学生姓名
         */
        private String studentName;

        /**
         * 提交记录ID
         */
        private Long submissionId;

        /**
         * 提交时间
         */
        private LocalDateTime submittedAt;

        /**
         * 总分（如果有计算的话）
         */
        private BigDecimal totalGrade;

        /**
         * 教师评语
         */
        private String feedback;

        /**
         * 是否有智能批改结果
         */
        private Boolean hasAiScore;

        /**
         * 文件评分详情列表
         */
        private List<FileScoreInfo> fileScores;
    }

    /**
     * 文件评分信息内部类
     */
    @Data
    public static class FileScoreInfo {
        /**
         * 文件索引
         */
        private Integer index;

        /**
         * 原始文件名（包含后缀）
         */
        private String fileName;

        /**
         * 文件后缀
         */
        private String fileExtension;

        /**
         * 智能得分
         */
        private Integer paperGrade;

        /**
         * 完整性评分 (c1)
         */
        private Double completeness;

        /**
         * 创新性评分 (c2)
         */
        private Double innovation;

        /**
         * 文件路径
         */
        private String filePath;
    }
}
