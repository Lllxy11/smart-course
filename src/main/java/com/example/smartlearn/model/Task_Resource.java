package com.example.smartlearn.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "task_resources")
public class Task_Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_task_resource_course_id"))
    private Course course;

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;

    // 上传者（教师）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", foreignKey = @ForeignKey(name = "fk_resources_uploader_id"))
    private Teacher uploader;

    // 所属任务（核心关联）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_task_resources_task_id"))
    private Task task;
}
