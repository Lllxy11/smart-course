package com.example.smartlearn.dto;


public class GraphNode {
    private String id;
    private String name;
    private String description;
    private String type; // "knowledge", "resource", "course"
    private Long courseId;

    public GraphNode() {}

    public GraphNode(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public GraphNode(String id, String name, String description, String type, Long courseId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.courseId = courseId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
}