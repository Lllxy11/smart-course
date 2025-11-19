package com.example.smartlearn.repository;

import com.example.smartlearn.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByCourseCourseId(Long courseId);
    List<Task> findByCourseCourseIdIn(List<Long> courseIds);
    List<Task> findByType(Task.TaskType type);

    /**
     * 根据课程ID列表和任务类型查找任务列表
     */
    List<Task> findByCourseCourseIdInAndType(List<Long> courseIds, Task.TaskType type);

    /**
     * 根据课程ID和任务类型查找任务列表
     */
    List<Task> findByCourseCourseIdAndType(Long courseId, Task.TaskType type);
}
