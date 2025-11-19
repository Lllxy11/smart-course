package com.example.smartlearn.repository;

import com.example.smartlearn.dto.StudentCourseDTO;
import com.example.smartlearn.model.StudentCourse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentCourseRepository extends JpaRepository<StudentCourse, Long> {
    //批量保存信息
    boolean existsByStudentStudentIdAndCourseCourseId(Long studentId, Long courseId);

    @Query("SELECT sc FROM StudentCourse sc WHERE sc.course.courseId=:courseId AND sc.student.studentId=:studentId")
    Optional<StudentCourse> findByCourseIdAndStudentId(
        @Param("courseId") Long courseId,
        @Param("studentId") Long studentId
    );

    Page<StudentCourse> findByCourseCourseId(Long courseId, Pageable pageable);
    @Modifying
    @Query("DELETE FROM StudentCourse sc WHERE sc.student.studentId = :studentId AND sc.course.courseId = :courseId")
    void deleteByStudentStudentIdAndCourseCourseId(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId
    );


    List<StudentCourse> findByStudentStudentId(Long studentId);
    @Query("SELECT NEW com.example.smartlearn.dto.StudentCourseDTO(" +
            " c.courseId, c.code, c.name) " +  // 注意括号和空格
            "FROM StudentCourse sc " +         // 修正表名
            "JOIN sc.course c " +
            "WHERE sc.student.studentId = :studentId")
    List<StudentCourseDTO> findCourseDTOsByStudentId(@Param("studentId") Long studentId);

}
