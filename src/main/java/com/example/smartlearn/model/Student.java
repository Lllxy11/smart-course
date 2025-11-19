package com.example.smartlearn.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "students")
public class Student {

    @Id
    @Column(name = "student_id")
    private Long studentId;

    // 与 User 实体建立一对一关联，将学生资料与登录账户绑定
    // user_id 列将是唯一的，确保一个登录账户只对应一个学生角色
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", unique = true)
    private User user;

    @Column(name="student_name", nullable = false)
    private String studentName;
    @Column(name = "class")
    private String className;
    // 一个学生可以有多条选课记录
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentCourse> studentCourseRecords;

    // 一个学生可以有多个任务提交物
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Submission> submissions;

    public void setName(String name) {
        this.studentName = name;
    }

    public String getName() {
        return studentName;
    }
    public void setClassName(String className) {
        this.className=className;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public List<StudentCourse> getStudentCourseRecords() {
        return studentCourseRecords;
    }

    public void setStudentCourseRecords(List<StudentCourse> studentCourseRecords) {
        this.studentCourseRecords = studentCourseRecords;
    }

    public List<Submission> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(List<Submission> submissions) {
        this.submissions = submissions;
    }
}
