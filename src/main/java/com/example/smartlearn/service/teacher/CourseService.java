package com.example.smartlearn.service.teacher;

import com.example.smartlearn.exception.ResourceNotFoundException;
import com.example.smartlearn.model.Course;
import com.example.smartlearn.repository.CourseRepository;
import com.example.smartlearn.repository.KnowledgePointRepository;
import com.example.smartlearn.repository.KnowledgePointResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    private final CourseRepository courseRepository;
    private final KnowledgePointResourceRepository resourceRepo;
    private final KnowledgePointRepository pointRepository;
    @Autowired
    public CourseService(CourseRepository courseRepository,
                         KnowledgePointRepository pointRepository,
                         KnowledgePointResourceRepository resourceRepo) {
        this.courseRepository = courseRepository;
        this.resourceRepo = resourceRepo;
        this.pointRepository = pointRepository;
    }

    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Optional<Course> getCourseById(Long courseId) {
        return courseRepository.findById(courseId);
    }


    @Transactional
    public Course updateCourse(Long courseId, Course updatedCourse) {
        return courseRepository.findById(courseId)
                .map(course -> {
                    // 保留原始课程编号，不更新它
                    // course.setCode(updatedCourse.getCode()); // 移除这一行

                    // 只更新这些字段
                    course.setName(updatedCourse.getName());
                    course.setDescription(updatedCourse.getDescription());
                    course.setCredit(updatedCourse.getCredit());
                    course.setTerm(updatedCourse.getTerm());

                    return courseRepository.save(course);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    }

    public void deleteCourse(Long courseId) {

        courseRepository.deleteById(courseId);
    }

    // 在CourseService.java中添加
    // 修改：添加 Pageable 参数
    public Page<Course> getCoursesByTeacherPaged(Long teacherId, Pageable pageable) {
        return courseRepository.findByTeacherId(teacherId, pageable);
    }

    // 修复搜索方法
// 添加安全的搜索方法
    public Page<Course> searchCourses(Long teacherId, String keyword, Pageable pageable) {
        // 验证并清理关键词
        if (keyword == null || keyword.trim().isEmpty()) {
            // 如果关键词无效，返回所有课程
            return getCoursesByTeacherPaged(teacherId, pageable);
        }

        // 清理关键词中的潜在问题字符
        String safeKeyword = keyword.trim()
                .replaceAll("[^\\w\\s\\p{L}]", "") // 移除非字母数字和空格
                .toLowerCase();

        if (safeKeyword.isEmpty()) {
            // 如果清理后关键词为空，返回所有课程
            return getCoursesByTeacherPaged(teacherId, pageable);
        }

        // 使用安全的关键词查询
        return courseRepository.findByTeacherIdAndKeyword(teacherId, safeKeyword, pageable);
    }

    public List<Course> getCoursesByTeacher(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId);
    }
}

