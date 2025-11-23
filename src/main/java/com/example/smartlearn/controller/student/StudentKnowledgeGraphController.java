package com.example.smartlearn.controller.student;

import com.example.smartlearn.dto.studentgraph.StudentKnowledgeGraphDTO;
import com.example.smartlearn.service.student.StudentKnowledgeGraphFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/{studentId}")
@RequiredArgsConstructor
public class StudentKnowledgeGraphController {

    private final StudentKnowledgeGraphFacade knowledgeGraphFacade;

    @GetMapping("/knowledge-graph")
    public ResponseEntity<StudentKnowledgeGraphDTO> getGlobalGraph(@PathVariable Long studentId) {
        return ResponseEntity.ok(knowledgeGraphFacade.getGlobalGraph(studentId));
    }

    @GetMapping("/course/{courseId}/knowledge-graph")
    public ResponseEntity<StudentKnowledgeGraphDTO> getCourseGraph(@PathVariable Long studentId,
                                                                   @PathVariable Long courseId) {
        return ResponseEntity.ok(knowledgeGraphFacade.getCourseGraph(studentId, courseId));
    }
}

