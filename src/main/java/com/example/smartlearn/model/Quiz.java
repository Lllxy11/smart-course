package com.example.smartlearn.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Entity
@Data
@Table(name = "quizzes")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private Teacher creator; // 创建者为教师

    @Column(name = "total_points")
    private Integer totalPoints;

    // 一张试卷包含多道题目 - 只保留基本的ManyToMany关系
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "quiz_questions",
            joinColumns = @JoinColumn(name = "quiz_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private List<Question> questions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    // 使用Transient字段暂时存储分数和顺序，后续通过Service层管理
    @Transient
    private Map<Question, Integer> questionScores = new HashMap<>();

    @Transient
    private Map<Question, Integer> questionOrders = new HashMap<>();

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

    public Teacher getCreator() {
        return creator;
    }

    public void setCreator(Teacher creator) {
        this.creator = creator;
    }

    public Integer getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    // 题目分数相关方法
    public Map<Question, Integer> getQuestionScores() {
        return questionScores;
    }

    public void setQuestionScores(Map<Question, Integer> questionScores) {
        this.questionScores = questionScores;
    }

    public Integer getQuestionScore(Question question) {
        return questionScores.get(question);
    }

    public void setQuestionScore(Question question, Integer score) {
        questionScores.put(question, score);
    }

    // 题目顺序相关方法
    public Map<Question, Integer> getQuestionOrders() {
        return questionOrders;
    }

    public void setQuestionOrders(Map<Question, Integer> questionOrders) {
        this.questionOrders = questionOrders;
    }

    public Integer getQuestionOrder(Question question) {
        return questionOrders.get(question);
    }

    public void setQuestionOrder(Question question, Integer order) {
        questionOrders.put(question, order);
    }

    // 便捷方法：添加题目并设置分数和顺序
    public void addQuestionWithInfo(Question question, Integer score, Integer order) {
        if (questions == null) {
            questions = new java.util.ArrayList<>();
        }
        questions.add(question);
        questionScores.put(question, score);
        questionOrders.put(question, order);
    }

    // 便捷方法：移除题目
    public void removeQuestion(Question question) {
        if (questions != null) {
            questions.remove(question);
        }
        questionScores.remove(question);
        questionOrders.remove(question);
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }
}
