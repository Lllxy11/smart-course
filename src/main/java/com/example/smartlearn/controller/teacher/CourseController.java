package com.example.smartlearn.controller.teacher;


import com.example.smartlearn.model.Course;
import com.example.smartlearn.repository.CourseRepository;
import com.example.smartlearn.service.teacher.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    private final CourseService courseService;
    private final CourseRepository courseRepository;

    @Autowired
    public CourseController(CourseService courseService, CourseRepository courseRepository) {
        this.courseService = courseService;
        this.courseRepository = courseRepository;
    }

    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        return ResponseEntity.ok(courseService.createCourse(course));
    }

    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }
//@GetMapping("/{teacherId}")
//public ResponseEntity<List<Course>> getByTeacherCourse(@PathVariable Long teacherId) {
//        return ResponseEntity.ok(courseService.getCoursesByTeacher(teacherId));
//}
    @GetMapping("/{courseId}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long courseId) {
        return courseService.getCourseById(courseId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<Course>> getCoursesByTeacher(@PathVariable Long teacherId) {
        return ResponseEntity.ok(courseService.getCoursesByTeacher(teacherId));
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<Course> updateCourse(
            @PathVariable Long courseId,
            @RequestBody Course updatedCourse) {
        return ResponseEntity.ok(courseService.updateCourse(courseId, updatedCourse));
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.noContent().build();
    }

    // 在CourseController.java中添加

    @GetMapping("/teacher/{teacherId}/paged")
    public ResponseEntity<Page<Course>> getTeacherCoursesPaged(
            @PathVariable Long teacherId,
            @RequestParam(defaultValue = "0") String pageStr,
            @RequestParam(defaultValue = "10") String sizeStr) {

        // 处理无效参数
        int page;
        try {
            page = Math.max(0, Integer.parseInt(pageStr));
        } catch (NumberFormatException e) {
            page = 0;
        }

        int size;
        try {
            size = Math.min(100, Math.max(1, Integer.parseInt(sizeStr)));
        } catch (NumberFormatException e) {
            size = 10;
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Course> courses = courseRepository.findByTeacherId(teacherId, pageable);

        // 如果查询结果为空，返回更多信息
        if (courses.isEmpty()) {
            Map<String, Object> body = new HashMap<>();
            body.put("message", "未找到课程数据");
            body.put("teacherId", teacherId);
            body.put("page", page);
            body.put("size", size);
            body.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Page.empty(pageable)); // 返回空页对象
        }

        return ResponseEntity.ok(courses);
    }

    /**
     * 搜索教师课程（带分页）
     */
    @GetMapping("/teacher/{teacherId}/search")
    public ResponseEntity<Page<Course>> searchTeacherCourses(
            @PathVariable Long teacherId,
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageable = PageRequest.of(page, size);

        // 自定义查询方法
        Page<Course> coursePage = courseRepository.findByTeacherIdAndKeyword(
                teacherId,
                query.toLowerCase(),
                pageable
        );

        return ResponseEntity.ok(coursePage);
    }
}