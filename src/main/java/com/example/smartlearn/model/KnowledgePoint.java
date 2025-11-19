package com.example.smartlearn.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Entity
@Table(name = "knowledge_points")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgePoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "position_x")
    private Double positionX;

    @Column(name = "position_y")
    private Double positionY;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "created_at")
    private Date createdAt;

    @OneToMany(mappedBy = "knowledgePoint", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // 添加这个注解
    private List<KnowledgePointResource> resources = new ArrayList<>();
    // 一个父知识点可以有多个子知识点
//    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
//    private List<KnowledgePoint> children;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
//
//    public KnowledgePoint getParent() {
//        return parent;
//    }
//
//    public void setParent(KnowledgePoint parent) {
//        this.parent = parent;
//    }
//
//    public List<KnowledgePoint> getChildren() {
//        return children;
//    }
//
//    public void setChildren(List<KnowledgePoint> children) {
//        this.children = children;
//    }
}