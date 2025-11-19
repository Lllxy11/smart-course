package com.example.smartlearn.repository;

import com.example.smartlearn.model.Task_Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface Task_ResourceRepository extends JpaRepository<Task_Resource, Long> {

    List<Task_Resource> findByTaskId(Long taskId);

    List<Task_Resource> findByCourseCourseId(Long courseId);
}
