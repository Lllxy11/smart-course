package com.example.smartlearn.repository;

import com.example.smartlearn.model.ClassResource;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClassResourceRepository extends JpaRepository<ClassResource, Integer> {
    Optional<ClassResource> findByResourceId(@Param("id") Long id);
    List<ClassResource> findByCourseId(Integer courseId);
    List<ClassResource> findByTaskId(Integer taskId);

    List<ClassResource> findByType(ClassResource.ResourceType type);
    List<ClassResource> findByNameContaining(String keyword);
    @Query("SELECT r FROM ClassResource r WHERE r.name = :name AND r.courseId = :courseId")
    Optional<ClassResource> findByCourseAndName(@Param("courseId") Integer courseId,
                                           @Param("name") String name);
    @Query("SELECT r FROM ClassResource r WHERE r.resourceId IN :resourceIds")
    List<ClassResource> findByResourceIds(@Param("resourceIds") List<Long> resourceIds);

    @Query("SELECT r FROM ClassResource r WHERE r.courseId = :courseId AND r.type = :type")

    List<ClassResource> findByCourseIdAndType(@Param("courseId") Integer courseId,
                                         @Param("type") ClassResource.ResourceType type);

    @Query("SELECT r FROM ClassResource r WHERE r.courseId = :courseId AND r.type = :type AND r.name LIKE %:keyword%")
    List<ClassResource> findByCourseIdAndTypeAndNameContaining(@Param("courseId") Integer courseId,
                                                          @Param("type") ClassResource.ResourceType type,
                                                          @Param("keyword") String keyword);

    @Query("SELECT r FROM ClassResource r WHERE r.type = :type AND r.courseId = :courseId")
    List<ClassResource> findByTypeAndCourseId(@Param("type") ClassResource.ResourceType type,
                                         @Param("courseId") Integer courseId);


    @Query("SELECT r FROM ClassResource r WHERE " +
            "(:courseIds IS NULL OR r.courseId IN :courseIds) AND " +
            "(:type IS NULL OR r.type = :type) AND " +
            "(:keyword IS NULL OR r.name LIKE %:keyword%)")
    List<ClassResource> findByCourseIdsAndTypeAndKeyword(
            @Param("courseIds") List<Integer> courseIds,
            @Param("type") ClassResource.ResourceType type,
            @Param("keyword") String keyword
    );
    Page<ClassResource> findByCourseIdIn(List<Long> courseIds, Pageable pageable);




    @Query("SELECT r.courseId FROM ClassResource r WHERE r.resourceId = :resourceId")
    Integer findCourseIdByResourceId(@Param("resourceId") Integer resourceId);

    // 添加新方法：仅更新URL
    @Modifying
    @Transactional
    @Query("UPDATE ClassResource r SET r.url = :url WHERE r.resourceId = :resourceId")
    void updateUrl(@Param("resourceId") Integer resourceId, @Param("url") String url);


    @Lock(LockModeType.PESSIMISTIC_WRITE) // 添加悲观锁
    @Query("SELECT r FROM ClassResource r WHERE r.resourceId = :resourceId")
    Optional<ClassResource> findByIdForUpdate(@Param("resourceId") Integer resourceId);

    @Query("SELECT r.courseId FROM ClassResource r WHERE r.resourceId = :resourceId")
    Integer getCourseIdByResourceId(@Param("resourceId") Integer resourceId); // 简化方法名

    @Query("SELECT r FROM ClassResource r WHERE r.resourceId = :resourceId")
    Optional<ClassResource> findById(@Param("resourceId") Integer resourceId);

    // ResourceRepository.java
    @Modifying
    @Transactional
    @Query("UPDATE ClassResource r SET r.url = :url WHERE r.resourceId = :resourceId")
    void updateResourceUrl(@Param("resourceId") Integer resourceId, @Param("url") String url);

    Optional<ClassResource> findByUrl(String url);



}
