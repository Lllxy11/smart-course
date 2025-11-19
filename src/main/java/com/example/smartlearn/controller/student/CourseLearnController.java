package com.example.smartlearn.controller.student;

import com.example.smartlearn.dto.StudentCourseDTO;
import com.example.smartlearn.model.ClassResource;
import com.example.smartlearn.repository.ClassResourceRepository;
import com.example.smartlearn.repository.CourseRepository;
import com.example.smartlearn.repository.StudentCourseRepository;
import com.example.smartlearn.service.student.CourseLearnService;
import com.example.smartlearn.service.teacher.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/student/{studentId}/learn")
public class CourseLearnController {
    private final CourseLearnService courseLearnService;
    private final ClassResourceRepository classResourceRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final CourseRepository courseRepository;
    private final CourseService courseService;

    @Autowired
    public CourseLearnController(
            CourseLearnService courseLearnService,
            ClassResourceRepository classResourceRepository,
            StudentCourseRepository studentCourseRepository,
            CourseRepository courseRepository,
            CourseService courseService){
        this.courseLearnService = courseLearnService;
        this.classResourceRepository = classResourceRepository;
        this.studentCourseRepository = studentCourseRepository;
        this.courseRepository = courseRepository;
        this.courseService = courseService;

    }
    @GetMapping("/resources")
    public ResponseEntity<List<ClassResource>> getClassResource(@PathVariable Long studentId,
                                                                @RequestParam Long courseId,
                                                                @RequestParam ClassResource.ResourceType type) throws AccessDeniedException {
        System.out.println("接收到请求，"+type+courseId);
        try {
            List<ClassResource> result = courseLearnService.getResourcesByCourseAndType(studentId,courseId,type);
            System.out.println("查询完成，结果数量：" + result.size()); // 添加这行
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace(); // 打印完整异常堆栈
            throw e; // 重新抛出
        }
    }

    @GetMapping("/course")
    public ResponseEntity<List<StudentCourseDTO>> getEnrollCourse(@PathVariable Long studentId){
        List<StudentCourseDTO> studentCourseList = courseLearnService.getEnrolledCourse(studentId);
        System.out.println(studentCourseList.size());
        System.out.println(studentCourseList);
        System.out.println('a');

        return ResponseEntity.ok(
                courseLearnService.getEnrolledCourse(studentId)
        );

    }



}
