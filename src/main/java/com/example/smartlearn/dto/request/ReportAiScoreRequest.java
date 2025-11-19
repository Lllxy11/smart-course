package com.example.smartlearn.dto.request;

import java.util.Map;

public class ReportAiScoreRequest {
    private Long taskId;
    private Map<String, Object> criteria; // 一套标准和权重

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Map<String, Object> getCriteria() { return criteria; }
    public void setCriteria(Map<String, Object> criteria) { this.criteria = criteria; }
}