package com.example.smartlearn.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CorrectDetailResponse extends CorrectResponse {
    private String content;
    private String filePath;
    private String description; // 新增
    private Long taskId;
    private List<AnswerDetail> answers; // 新增：答题详情列表

    public CorrectDetailResponse(Long submissionId, Long studentId, String studentName, LocalDateTime submittedAt, BigDecimal grade, String feedback, String content, String filePath, String description) {
        super(submissionId, studentId, studentName, submittedAt, grade, feedback);
        this.content = content;
        this.filePath = filePath;
        this.description = description;

    }

    // 新增 getter/setter
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<AnswerDetail> getAnswers() { return answers; }
    public void setAnswers(List<AnswerDetail> answers) { this.answers = answers; }

}