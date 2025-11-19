package com.example.smartlearn.repository;

import com.example.smartlearn.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // 非分页方法
    List<Course> findByTeacherId(Long teacherId);

    // 分页方法
    Page<Course> findByTeacherId(Long teacherId, Pageable pageable);

    // 搜索方法（分页）
    @Query("SELECT c FROM Course c WHERE " +
            "c.teacherId = :teacherId AND " +
            "(LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(c.term) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Course> findByTeacherIdAndKeyword(
            @Param("teacherId") Long teacherId,
            @Param("keyword") String keyword,
            Pageable pageable);
    @Query("SELECT c.courseId FROM Course c WHERE c.teacherId = :teacherId")
    List<Integer> findIdsByTeacherId(@Param("teacherId") Integer teacherId);

}