package com.example.smartlearn.service.student;

import com.example.smartlearn.dto.studentgraph.StudentKnowledgeEdgeDTO;
import com.example.smartlearn.dto.studentgraph.StudentKnowledgeGraphDTO;
import com.example.smartlearn.dto.studentgraph.StudentKnowledgeNodeDTO;
import com.example.smartlearn.dto.studentgraph.StudentResourceDTO;
import com.example.smartlearn.model.ClassResource;
import com.example.smartlearn.model.KnowledgePoint;
import com.example.smartlearn.model.KnowledgePointResource;
import com.example.smartlearn.model.Submission;
import com.example.smartlearn.repository.KnowledgePointRepository;
import com.example.smartlearn.repository.StudentCourseRepository;
import com.example.smartlearn.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentKnowledgeGraphFacade {

    private final KnowledgePointRepository knowledgePointRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final SubmissionRepository submissionRepository;

    public StudentKnowledgeGraphDTO getCourseGraph(Long studentId, Long courseId) {
        List<KnowledgePoint> points = knowledgePointRepository.findByCourseCourseId(courseId);
        return buildGraph(points, studentId, List.of(courseId));
    }

    public StudentKnowledgeGraphDTO getGlobalGraph(Long studentId) {
        List<Long> courseIds = studentCourseRepository.findByStudentStudentId(studentId).stream()
                .map(sc -> sc.getCourse().getCourseId())
                .toList();
        List<KnowledgePoint> points = courseIds.isEmpty()
                ? Collections.emptyList()
                : courseIds.stream()
                    .map(knowledgePointRepository::findByCourseCourseId)
                    .flatMap(Collection::stream)
                    .toList();
        return buildGraph(points, studentId, courseIds);
    }

    private StudentKnowledgeGraphDTO buildGraph(List<KnowledgePoint> points, Long studentId, List<Long> courseIds) {
        Map<Long, String> learningStatusMap = buildLearningStatus(points, studentId);
        List<StudentKnowledgeNodeDTO> nodes = new ArrayList<>();
        List<StudentKnowledgeEdgeDTO> edges = new ArrayList<>();

        int index = 0;
        for (KnowledgePoint kp : points) {
            String nodeId = "kp_" + kp.getId();
            nodes.add(StudentKnowledgeNodeDTO.builder()
                    .id(nodeId)
                    .title(kp.getName())
                    .description(kp.getDescription())
                    .type("knowledge")
                    .courseId(kp.getCourse() != null ? kp.getCourse().getCourseId() : null)
                    .courseIds(courseIds)
                    .positionX(defaultPosition(kp.getPositionX(), index, true))
                    .positionY(defaultPosition(kp.getPositionY(), index, false))
                    .learningStatus(learningStatusMap.getOrDefault(kp.getId(), "learning"))
                    .resources(mapResources(kp.getResources()))
                    .build());
            if (kp.getParentId() != null) {
                edges.add(StudentKnowledgeEdgeDTO.builder()
                        .source("kp_" + kp.getParentId())
                        .target(nodeId)
                        .relationship("prerequisite")
                        .build());
            }
            index++;
        }

        return StudentKnowledgeGraphDTO.builder()
                .nodes(nodes)
                .edges(edges)
                .build();
    }

    private Map<Long, String> buildLearningStatus(List<KnowledgePoint> points, Long studentId) {
        Map<Long, String> status = new HashMap<>();
        if (points.isEmpty()) {
            return status;
        }
        Set<Long> courseIds = points.stream()
                .map(kp -> kp.getCourse().getCourseId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<Submission> submissions = submissionRepository.findByStudentStudentIdAndTaskCourseCourseIdIn(studentId, new ArrayList<>(courseIds));
        double avgGrade = submissions.stream()
                .map(Submission::getGrade)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(75.0);
        for (KnowledgePoint kp : points) {
            // 根据平均成绩和知识点序号简单推断学习状态，后续可替换为真实逻辑
            double adjust = (kp.getId() % 15) - 7;
            double score = Math.max(0, Math.min(100, avgGrade + adjust));
            if (score >= 85) {
                status.put(kp.getId(), "mastered");
            } else if (score >= 60) {
                status.put(kp.getId(), "learning");
            } else {
                status.put(kp.getId(), "weak");
            }
        }
        return status;
    }

    private double defaultPosition(Double pos, int index, boolean isX) {
        if (pos != null) {
            return pos;
        }
        int spacing = 220;
        return (index % 5) * spacing + (isX ? 200 : 180) + (index / 5) * 60;
    }

    private List<StudentResourceDTO> mapResources(List<KnowledgePointResource> kpResources) {
        if (kpResources == null || kpResources.isEmpty()) {
            return Collections.emptyList();
        }
        return kpResources.stream()
                .map(KnowledgePointResource::getResource)
                .filter(Objects::nonNull)
                .map(this::toResource)
                .toList();
    }

    private StudentResourceDTO toResource(ClassResource res) {
        return StudentResourceDTO.builder()
                .id(res.getResourceId() != null ? res.getResourceId().longValue() : null)
                .name(res.getName())
                .type(res.getType() != null ? res.getType().name() : "resource")
                .build();
    }
}
