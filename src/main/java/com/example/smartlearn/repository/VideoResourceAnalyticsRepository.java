package com.example.smartlearn.repository;

import com.example.smartlearn.model.ClassResource;
import com.example.smartlearn.model.VideoResourceAnalytics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoResourceAnalyticsRepository extends JpaRepository<com.example.smartlearn.model.VideoResourceAnalytics, Long> {
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
            "FROM VideoResourceAnalytics a " +
            "WHERE a.resource.resourceId = :resourceId " +
            "AND (:studentId IS NULL AND a.student IS NULL OR a.student.studentId= :studentId) " +
            "AND DATE(a.analysisTime) = CURRENT_DATE")
    boolean existsTodayAnalysis(@Param("resourceId") Long resourceId, @Param("studentId") Long studentId);

    @Query("SELECT a FROM VideoResourceAnalytics a " +
            "WHERE a.resource.resourceId = :resourceId " +
            "AND a.student IS NULL " +
            "ORDER BY a.analysisTime DESC")
    Page<VideoResourceAnalytics> findByResourceIdAndStudentIsNullOrderByAnalysisTimeDesc(
            @Param("resourceId") Long resourceId, Pageable pageable);

    @Query("SELECT a FROM VideoResourceAnalytics a " +
            "WHERE a.resource.resourceId= :resourceId " +
            "AND a.student.studentId = :studentId " +
            "ORDER BY a.analysisTime DESC")
    Page<VideoResourceAnalytics> findByResourceIdAndStudentIdOrderByAnalysisTimeDesc(
            @Param("resourceId") Long resourceId, @Param("studentId") Long studentId, Pageable pageable);
    @Query("SELECT va FROM VideoResourceAnalytics va " +
            "WHERE va.completionRate >= 0.7 AND va.resource.courseId = :courseId " +
            "ORDER BY va.completionRate DESC")
    List<VideoResourceAnalytics> findHighQualityVideos(
            @Param("courseId") Long courseId,
            Pageable pageable);

    // 获取热门视频（基于总观看次数和热点回放）
    @Query(value = """
    SELECT r.* FROM class_resource r
    JOIN video_resource_analytics va ON va.resource_id = r.resource_id
    WHERE r.type = 'video'
    GROUP BY r.resource_id
    ORDER BY 
        SUM(va.total_views) DESC,
        (
            SELECT COUNT(*) 
            FROM JSON_TABLE(
                va.hot_spots,
                '$[*]' COLUMNS(
                    replay_count INT PATH '$.replayCount'
                )
            ) AS hotspots 
            WHERE replay_count > 3
        ) DESC
    """, nativeQuery = true)
    List<ClassResource> findPopularVideos(Pageable pageable);
}
