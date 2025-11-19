package com.example.smartlearn.repository;

import com.example.smartlearn.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    boolean existsByTaskIdAndStudentStudentId(Long taskId, Long studentId);
    List<Submission> findByStudentStudentIdAndTaskId(Long studentId, Long taskId);

    /**
     * 根据任务ID查找所有提交记录
     */
    List<Submission> findByTaskId(Long taskId);

    /**
     * 根据学生ID查找所有提交记录
     */
    List<Submission> findByStudentStudentId(Long studentId);

    /**
     * 根据学生ID和课程ID查找提交记录
     */
    List<Submission> findByStudentStudentIdAndTaskCourseCourseId(Long studentId, Long courseId);

    /**
     * 根据学生ID和课程ID列表查找提交记录
     */
    List<Submission> findByStudentStudentIdAndTaskCourseCourseIdIn(Long studentId, List<Long> courseIds);
}
