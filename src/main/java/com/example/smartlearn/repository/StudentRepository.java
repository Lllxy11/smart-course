package com.example.smartlearn.repository;

import com.example.smartlearn.model.Student;
import com.example.smartlearn.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    //导出选某一门课的所有学生
    @Query("SELECT sc.student from StudentCourse sc where sc.course.courseId=:courseId")
    Page<Student> findByCourse_Id(@Param("courseId") Long courseId, Pageable pageable);
    //批量保存学生
    <S extends Student> List<S> saveAll(Iterable<S> entities);
    @Query("SELECT s FROM Student s JOIN s.studentCourseRecords sc WHERE sc.course.courseId = :courseId")
    Page<Student> findByCourseId(@Param("courseId") Long courseId, Pageable pageable);
    Optional<Student> findByUser(User user);
}
