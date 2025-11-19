package com.example.smartlearn.repository;

import com.example.smartlearn.model.KnowledgePoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 该Repository接口用于知识点数据访问功能。
 * 提供知识点的基本CRUD操作。
 */
@Repository
public interface KnowledgePointRepository extends JpaRepository<KnowledgePoint, Long> {
    List<KnowledgePoint> findByCourseCourseId(Long courseId);
    List<KnowledgePoint> findByParentId(Long parentId);


    // 按知识点ID和资源ID删除关联
    @Modifying
    @Query("DELETE FROM KnowledgePoint k WHERE k.course.courseId = :courseId")
    void deleteAllByCourseId(@Param("courseId") Long courseId);
    List<KnowledgePoint> findByCourse_CourseId(Long courseId);
    List<KnowledgePoint> findByCourse_CourseIdAndNameContaining(Long courseId, String name);


    boolean existsByNameAndCourse_CourseId(String name, Long courseId);
    KnowledgePoint findByNameAndCourse_CourseId(String name, Long courseId);
} 