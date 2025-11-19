package com.example.smartlearn.dto.response;

import java.time.LocalDateTime;

public class TaskResponse {

    private Long id;
    private Long courseId;
    private String courseName;
    private String title;
    private String description;
    private String type;
    private Long quizId;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    // 在 TaskResponse 中添加
    private QuizDetailResponse quizDetail; // 当任务类型为QUIZ时，包含试卷详细信息

    // 新增字段
    private boolean expired;
    private boolean submitted;

    // getter/setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
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

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public QuizDetailResponse getQuizDetail() {
        return quizDetail;
    }

    public void setQuizDetail(QuizDetailResponse quizDetail) {
        this.quizDetail = quizDetail;
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
}
