package com.example.smartlearn.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "video_events")
public class VideoEvent {

    // 定义视频事件的类型
    public enum EventType {
        PLAY,
        PAUSE,
        SEEK, // 拖动进度条
        ENDED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 事件由哪个学生触发
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id",referencedColumnName = "student_id", nullable = false)
    private Student student;

    // 事件发生在哪个教学资源上
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", referencedColumnName = "resource_id",nullable = false)
    private ClassResource resource;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    // 事件发生时，视频播放到了第几秒
    @Column(name = "video_timestamp")
    private Integer videoTimestamp;

    // 事件发生的真实时间
    @Column(name = "created_at", nullable = false,updatable = false)
    private LocalDateTime createdAt=LocalDateTime.now();

    //关联同一观看会话的事件
    @Column(name = "session_id",length = 64)
    private String sessionId;

    //视频总时长
    @Column(name = "duration")
    private Integer duration;



    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public ClassResource getResource() {
        return resource;
    }

    public void setResource(ClassResource resource) {
        this.resource = resource;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Integer getVideoTimestamp() {
        return videoTimestamp;
    }

    public void setVideoTimestamp(Integer videoTimestamp) {
        this.videoTimestamp = videoTimestamp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String sessionId) {}
    public Integer getDuration() {
        return duration;
    }
    public void setDuration(Integer duration) {}
}

