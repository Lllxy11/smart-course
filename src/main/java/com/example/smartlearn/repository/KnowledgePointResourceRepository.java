package com.example.smartlearn.repository;

import com.example.smartlearn.model.KnowledgePointResource;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgePointResourceRepository extends JpaRepository<KnowledgePointResource, Long> {
    // 按知识点ID查找关联资源
    //List<KnowledgePointResource> findByKnowledgePointId(Long pointId);

    // 添加方法：按知识点ID和资源ID检查是否存在关联
    boolean existsByKnowledgePointIdAndResourceResourceId(Long knowledgePointId, Long resourceId);

    //使用明确的JOIN FETCH查询
    @Query("SELECT a FROM KnowledgePointResource a " +
                  "JOIN FETCH a.resource r " +
                  "LEFT JOIN FETCH r.knowledgePoints " + // 打破循环关联
                  "WHERE a.knowledgePoint.id = :pointId")
    List<KnowledgePointResource> findAssociationsWithResourceByPointId(@Param("pointId") Long pointId);
    @Modifying
    @Query("DELETE FROM KnowledgePointResource k WHERE k.knowledgePoint.course.courseId = :courseId")
    void deleteAllByCourseId(@Param("courseId") Long courseId);
//    @Query("SELECT " +
//            "r.resourceId as resourceId, " +
//            "r.name as name, " +
//            "r.type as type, " +
//            "r.url as url, " +
//            "a.linkedAt as linkedAt " +
//            "FROM KnowledgePointResource a " +
//            "JOIN a.resource r " +
//            "WHERE a.knowledgePointId = :pointId")
//    List<ResourceAssociationView> findAssociationsWithResourceByPointId(@Param("pointId") Long pointId);

    // 添加方法：按知识点ID删除所有关联资源
    @Transactional
    @Modifying
    @Query("DELETE FROM KnowledgePointResource r WHERE r.knowledgePoint.id= :knowledgePointId")
    void deleteByKnowledgePointId(@Param("knowledgePointId") Long knowledgePointId);

    // 按知识点ID和资源ID删除关联
    @Transactional
    @Modifying
    void deleteByKnowledgePointIdAndResourceResourceId(Long pointId, Long resourceId);

    // 添加方法：按资源ID删除关联（可选，按需添加）
    @Transactional
    @Modifying
    void deleteByResourceResourceId(Long resourceId);

    // 添加方法：计算某知识点的资源数量
    @Query("SELECT COUNT(r) FROM KnowledgePointResource r WHERE r.knowledgePoint.id = :pointId")
    int countByKnowledgePointId(@Param("pointId") Long pointId);
    // 查询指定课程的资源数量（使用DISTINCT确保去重）
    @Query("SELECT COUNT(DISTINCT kpr.resource.resourceId) " +
            "FROM KnowledgePointResource kpr " +
            "WHERE kpr.knowledgePoint.id IN (SELECT kp.id FROM KnowledgePoint kp WHERE kp.course.courseId = :courseId)")
    int countDistinctResourcesByCourseId(@Param("courseId") Long courseId);
    @Query("SELECT kpr FROM KnowledgePointResource kpr " +
            "JOIN FETCH kpr.resource " +
            "WHERE kpr.knowledgePoint.id IN :knowledgePointIds")
    List<KnowledgePointResource> findWithResourcesByKnowledgePointIdIn(
            @Param("knowledgePointIds") List<Long> knowledgePointIds);

}