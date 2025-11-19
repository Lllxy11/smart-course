package com.example.smartlearn.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "knowledge_point_resources")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgePointResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "linked_at")
    private Date linkedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "knowledge_point_id", nullable = false)
    @JsonIgnore
    private KnowledgePoint knowledgePoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    @JsonIgnore
    private ClassResource resource;


//    public KnowledgePointResource(KnowledgePoint knowledgePoint, ClassResource classResource) {
//        this.knowledgePoint = knowledgePoint;
//    }
//    public KnowledgePointResource(ClassResource classResource) {
//        this.resource = classResource;
//    }
//    public KnowledgePointResource(KnowledgePoint knowledgePoint) {
//        this.knowledgePoint = knowledgePoint;
//    }
//    public KnowledgePointResource(ClassResource classResource, KnowledgePoint knowledgePoint) {
//        this.resource = classResource;
//    }

    public ClassResource getResource() {

        return resource;
    }
    public void setResource(ClassResource resource) {
        this.resource = resource;
    }

    public KnowledgePoint getKnowledgePoint() {
            return knowledgePoint;
    }

    public void setKnowledgePoint(KnowledgePoint knowledgePoint) {
            this.knowledgePoint = knowledgePoint;
    }

    public Date getLinkedAt() {
        return linkedAt;
    }
    public void setLinkedAt(Date linkedAt) {
        this.linkedAt = linkedAt;
    }

//    public Long getResourceId() {
//            return resourceId;
//    }
//    public void setResourceId(Long resourceId) {
//        this.resourceId = resourceId;
//    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
}