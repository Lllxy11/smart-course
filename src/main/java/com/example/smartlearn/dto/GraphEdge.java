package com.example.smartlearn.dto;

public class GraphEdge {
    private String source;
    private String target;
    private String relationship; // "contains", "requires", "references"

    public GraphEdge() {}

    public GraphEdge(String source, String target, String relationship) {
        this.source = source;
        this.target = target;
        this.relationship = relationship;
    }

    // Getters and Setters
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
}