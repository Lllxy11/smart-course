package com.example.smartlearn.dto.response;

import com.example.smartlearn.model.User;
import lombok.Data;

@Data
public class LoginResponse {
    private Long userId;
    private String account;
    private String role;
    private Long teacherId;
    private Long studentId;
    private String token;


    public LoginResponse(User user) {
        this.userId = user.getUserId();
        this.account = user.getAccount();
        this.role = user.getRole();

    }

    public LoginResponse(User user, Long teacherId, Long studentId) {
        this.userId = user.getUserId();
        this.account = user.getAccount();
        this.role = user.getRole();
        this.teacherId = teacherId;
        this.studentId = studentId;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}