package com.example.smartlearn.model;
/*
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploapackage com.example.smartlearn.model;\n" +
            "\n" +
            "import jakarta.persistence.*;\n" +
            "import lombok.Data;\n" +
            "import java.time.LocalDateTime;\n" +
            "\n" +
            "@Entity\n" +
            "@Data\n" +
            "@Table(name = \"resources\")\n" +
            "public class Resource {\n" +
            "\n" +
            "    @Id\n" +
            "    @GeneratedValue(strategy = GenerationType.IDENTITY)\n" +
            "    private Long id;\n" +
            "\n" +
            "    @ManyToOne(fetch = FetchType.LAZY)\n" +
            "    @JoinColumn(name = \"course_id\", nullable = false)\n" +
            "    private Course course;\n" +
            "\n" +
            "    @ManyToOne(fetch = FetchType.LAZY)\n" +
            "    @JoinColumn(name = \"uploader_id\", nullable = false)\n" +
            "    private Teacher uploader; // 上传者为教师\n" +
            "\n" +
            "    @Column(nullable = false)\n" +
            "    private String name; // 资源显示名称\n" +
            "\n" +
            "    @Column(name = \"file_type\")\n" +
            "    private String fileType; // 文件类型，如 'pdf', 'pptx', 'mp4'\n" +
            "\n" +
            "    @Column(name = \"file_path\", nullable = false)\n" +
            "    private String filePath; // 文件在服务器或云存储上的路径\n" +
            "\n" +
            "    @Column(name = \"upload_date\", nullable = false)\n" +
            "    private LocalDateTime uploadDate;\n" +
            "\n" +
            "    @PrePersist\n" +
            "    protected void onCreate() {\n" +
            "        uploadDate = LocalDateTime.now();\n" +
            "    }\n" +
            "}\nder_id", nullable = false)
    private Teacher uploader; // 上传者为教师

    @Column(nullable = false)
    private String name; // 资源显示名称

    @Column(name = "file_type")
    private String fileType; // 文件类型，如 'pdf', 'pptx', 'mp4'

    @Column(name = "file_path", nullable = false)
    private String filePath; // 文件在服务器或云存储上的路径

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;

    @PrePersist
    protected void onCreate() {
        uploadDate = LocalDateTime.now();
    }

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

    public Teacher getUploader() {
        return uploader;
    }

    public void setUploader(Teacher uploader) {
        this.uploader = uploader;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }
}
*/
