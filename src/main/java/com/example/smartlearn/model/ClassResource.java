package com.example.smartlearn.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "class_resource")
public class ClassResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resource_id")
    private Long resourceId;
    @Column(name = "duration")
    private Integer duration;  // 视频时长（秒），仅对视频资源有效
    @Column(name = "course_id")
    private Integer courseId;

    @Column(name = "task_id")
    private Integer taskId;

    @Column(name = "name", length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", columnDefinition = "ENUM('ppt','pdf','video','doc')")
    private ResourceType type;

    @Column(name = "url", length = 255)
    private String url;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public void setId(long l) {
        this.resourceId = l;
    }

    // 资源类型枚举
    public enum ResourceType {
        ppt, pdf, video, doc
    }

    public String getFileExtension() {
        switch (type) {
            case ppt: return ".pptx";
            case pdf: return ".pdf";
            case video: return ".mp4";
            case doc: return ".docx";
            default: return "";
        }
    }
    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<KnowledgePointResource> knowledgePoints = new ArrayList<>();

}
