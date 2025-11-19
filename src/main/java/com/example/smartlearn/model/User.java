package com.example.smartlearn.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId; // 修改为驼峰命名

    @Column(unique = true, nullable = false)
    private String account;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash; // 修改为驼峰命名

    @Column(name = "role", nullable = false)
    private String role; // 修改为字符串类型

    public enum Role {
        teacher, student // 保持小写与数据库一致
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public User() {}

    public User(String account, String passwordHash, String role) {
        this.account = account;
        this.passwordHash = passwordHash;
        this.role = role;
    }
}