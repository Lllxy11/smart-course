package com.example.smartlearn.dto.request;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class SubmissionRequest {
    private Long taskId;
    private Long studentId;
    private String content; // 用于HOMEWORK类型
    private List<MultipartFile> files; // 用于REPORT类型多文件

    // Getters and Setters
    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<MultipartFile> getFiles() {
        return files;
    }
    public void setFiles(List<MultipartFile> files) {
        this.files = files;
    }
}
