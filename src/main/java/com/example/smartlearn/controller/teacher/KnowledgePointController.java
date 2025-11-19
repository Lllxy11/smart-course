package com.example.smartlearn.controller.teacher;

import com.example.smartlearn.dto.response.KnowledgePointResponse;
import com.example.smartlearn.service.teacher.KnowledgePointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/knowledge-point")
@CrossOrigin(origins = "*")
public class KnowledgePointController {
    @Autowired
    private KnowledgePointService knowledgePointService;

    @PostMapping("/by-course")
    public ResponseEntity<List<KnowledgePointResponse>> getByCourse(@RequestBody ByCourseRequest request) {
        List<KnowledgePointResponse> list = knowledgePointService.getByCourseId(request.getCourseId());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/search-by-name")
    public ResponseEntity<List<KnowledgePointResponse>> searchByName(@RequestBody SearchByNameRequest request) {
        List<KnowledgePointResponse> list = knowledgePointService.searchByCourseIdAndName(request.getCourseId(), request.getName());
        return ResponseEntity.ok(list);
    }

    public static class ByCourseRequest {
        private Long courseId;
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
    }

    public static class SearchByNameRequest {
        private Long courseId;
        private String name;
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
} 