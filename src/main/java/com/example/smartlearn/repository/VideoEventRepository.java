package com.example.smartlearn.repository;

import com.example.smartlearn.model.VideoEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VideoEventRepository extends JpaRepository<VideoEvent, Long> {
    List<VideoEvent> findByStudentStudentId(Long studentId);
    List<VideoEvent> findBySessionId(String sessionId);
    @Query("SELECT MAX (e.duration) FROM VideoEvent e WHERE e.resource.resourceId=:resourceId")
    Long findDurationByResourceId(@Param("resourceId") Long resourceId);

    @Query("SELECT DISTINCT e.student.studentId, e.resource.resourceId FROM VideoEvent e")
    List<Object[]> findDistinctStudentResourcePairs();
    //统计特定资源的唯一观看学生数
    @Query("SELECT COUNT (DISTINCT e.student.studentId)FROM VideoEvent e WHERE e.resource.resourceId=:resourceId")
    Long countUniqueViewersByResource(Long resourceId);
    //计算某学生观看某个资源的次数
    @Query("SELECT COUNT (e) FROM VideoEvent e WHERE e.resource.resourceId=:resourceId AND e.student.studentId=:studentId")
    Long countByStudentIdAndResourceId(@Param("studentId") Long studentId,
                                       @Param("resourceId") Long resourceId);
    //查找学生的最大观看进度
    @Query("SELECT MAX (e.videoTimestamp) FROM VideoEvent e WHERE e.student.studentId=:studentId AND e.resource.resourceId=:resourceId")
    Integer findMaxProgressByStudentAndResource(Long studentId,Long resourceId);
    @Query("SELECT AVG (e.videoTimestamp) FROM VideoEvent e WHERE e.resource.resourceId=:resourceId AND e.eventType='PAUSE'")
    Double calculateAverageWatchTime(Long resourceId);
    /**
     * 查找特定时间范围内的视频事件
     */
    @Query("SELECT e FROM VideoEvent e " +
            "WHERE e.resource.resourceId = :resourceId " +
            "AND e.createdAt >= :start AND e.createdAt <= :end " +
            "ORDER BY e.createdAt")
    List<VideoEvent> findByResourceAndPeriod(
            @Param("resourceId") Long resourceId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
    /**
     * 查找学生最近观看的视频事件
     */
    @Query("SELECT e FROM VideoEvent e " +
            "WHERE e.student.studentId = :studentId " +
            "ORDER BY e.createdAt DESC")
    List<VideoEvent> findRecentByStudent(
            @Param("studentId") Long studentId,
            Pageable pageable);
    /**
     * 查找资源的热点区域（观看次数最多的片段） 这玩意能实现/
     */
    @Query(value = "SELECT FLOOR(e.video_timestamp/5)*5 AS start_time, " +
            "COUNT(*) AS replay_count " +
            "FROM video_events e " +
            "WHERE e.resource_id = ?1 " +
            "GROUP BY FLOOR(e.video_timestamp/5)*5 " +  // 确保与SELECT中的表达式一致
            "ORDER BY replay_count DESC " +
            "LIMIT 5", nativeQuery = true)
    List<Object[]> findHotSpotsByResource(Long resourceId);
    @Query("SELECT COUNT(v) FROM VideoEvent v WHERE v.resource.resourceId = :resourceId")
    long countByResourceId(@Param("resourceId") Long resourceId);
    @Query("SELECT e FROM VideoEvent e WHERE e.eventType=:eventType AND e.resource.resourceId=:resourceId")
    List<VideoEvent> findByResourceIdAndEventType(@Param("resourceId") Long resourceId,
                                                  @Param("eventType") VideoEvent.EventType eventType);




}
