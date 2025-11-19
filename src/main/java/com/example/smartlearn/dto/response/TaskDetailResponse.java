package com.example.smartlearn.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class TaskDetailResponse {
    private Long id;
    private String title;
    private String description;
    private String type;
    private LocalDateTime dueDate;
    private boolean expired;
    private boolean submitted;
    private List<Task_ResourceResponse> resources;

    // 新增：仅homework和report类型任务展示
    private java.math.BigDecimal grade;
    private String feedback;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    public List<Task_ResourceResponse> getResources() {
        return resources;
    }

    public void setResources(List<Task_ResourceResponse> resources) {
        this.resources = resources;
    }

    // 新增 getter/setter
    public java.math.BigDecimal getGrade() {
        return grade;
    }

    public void setGrade(java.math.BigDecimal grade) {
        this.grade = grade;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
