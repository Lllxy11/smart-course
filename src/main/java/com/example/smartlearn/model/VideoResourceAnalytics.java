package com.example.smartlearn.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import java.util.Collections;
import java.util.List;
import java.time.LocalDateTime;
import java.util.List;
@Slf4j
@Entity
@Data
@Table(name = "video_resource_analytics")
public class VideoResourceAnalytics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    private ClassResource resource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "analysis_time", nullable = false, updatable = false)
    private LocalDateTime analysisTime = LocalDateTime.now();

    // 基础指标
    @Column(name = "total_views")
    private Integer totalViews;
    @Column(name = "average_watch_time")
    private Double averageWatchTime;
    @Column(name = "completion_rate")
    private Double completionRate;

    // 学生特定指标
    @Column(name = "student_watch_count")
    private Integer studentWatchCount;
    @Column(name = "student_last_watch_time")
    private Integer studentLastWatchTime;
    @Column(name = "student_progress_rate")
    private Double studentProgressRate;

    // 热点数据
    @Column(name = "hot_spots", columnDefinition = "JSON")
    private String hotSpotsJson;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HotSpot {
        private Integer startTime;
        private Integer endTime;
        private Integer replayCount;
    }

    @Transient
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public List<HotSpot> getHotSpots() {
        try {

            return objectMapper.readValue(hotSpotsJson,
                    new TypeReference<List<HotSpot>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse hot spots", e);
            return Collections.emptyList();
        }
    }

    @Transient
    public void setHotSpots(List<HotSpot> hotSpots) {
        try {
            this.hotSpotsJson = objectMapper.writeValueAsString(hotSpots);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize hot spots", e);
            this.hotSpotsJson = "[]";
        }
    }
    // 修正VideoResourceAnalytics的构造函数

    @PrePersist
    public void prePersist() {
        this.analysisTime = LocalDateTime.now();
    }
}

