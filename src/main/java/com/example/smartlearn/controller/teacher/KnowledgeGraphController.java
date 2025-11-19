// KnowledgeGraphController.java
package com.example.smartlearn.controller.teacher;

import com.example.smartlearn.dto.KnowledgeGraphDTO;
import com.example.smartlearn.dto.KnowledgePointDTO;
import com.example.smartlearn.dto.PositionUpdateDTO;
import com.example.smartlearn.dto.ResourceDTO;
import com.example.smartlearn.model.ClassResource;
import com.example.smartlearn.service.teacher.KnowledgeGraphService;
import com.example.smartlearn.service.teacher.Class_ResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers/{teacherId}/knowledge-graph")
public class KnowledgeGraphController {

    private final KnowledgeGraphService knowledgeGraphService;
    private final Class_ResourceService classResourceService;

    public KnowledgeGraphController(KnowledgeGraphService knowledgeGraphService,
                                    Class_ResourceService classResourceService) {
        this.knowledgeGraphService = knowledgeGraphService;
        this.classResourceService = classResourceService;
    }
    @PostMapping("/courses/{courseId}/generate-by-ai-resources")
    public ResponseEntity<KnowledgeGraphDTO> generateByAiResources(
            @PathVariable("teacherId") Long teacherId,
            @PathVariable("courseId") Long courseId,
            @RequestBody List<Long> resources) {

        System.out.println("========================================");
        System.out.println("✅ 收到请求！");
        System.out.println("teacherId: " + teacherId);
        System.out.println("courseId: " + courseId);
        System.out.println("资源列表: " + resources);
        System.out.println("资源数量: " + resources.size());
        System.out.println("========================================");

        KnowledgeGraphDTO knowledgeGraphDTO = knowledgeGraphService.generateByAI(courseId, resources);
        return ResponseEntity.ok(knowledgeGraphDTO);
    }
    //获取某课程现有所有知识点，作为知识图谱初始化依据
    @GetMapping("/points/allresource/{courseId}")
    public ResponseEntity<List> pointsAllResource(
            @PathVariable("courseId") Integer courseId){
        System.out.println("测试");
        List<ClassResource> original_resource = classResourceService.getAllResources(courseId);
        return ResponseEntity.ok(original_resource);
    }

    // 创建知识点
    @PostMapping("/points")
    public ResponseEntity<Long> createKnowledgePoint(
            @PathVariable Long teacherId,
            @RequestBody KnowledgePointDTO dto) {
        Long pointId = knowledgeGraphService.createKnowledgePoint(dto);
        return ResponseEntity.ok(pointId);
    }

    // 获取课程知识图谱
    @GetMapping("/course/{courseId}")
    public ResponseEntity<KnowledgeGraphDTO> getCourseKnowledgeGraph(
            @PathVariable Long teacherId,
            @PathVariable Long courseId) {

        KnowledgeGraphDTO graph = knowledgeGraphService.getCourseKnowledgeGraph(courseId);
        return ResponseEntity.ok(graph);
    }


    // 更新知识点位置
    @PutMapping("/points/{pointId}/position")
    public ResponseEntity<Void> updatePosition(
            @PathVariable Long teacherId,
            @PathVariable Long pointId,
            @RequestBody PositionUpdateDTO position) {
        knowledgeGraphService.updatePosition(pointId, position);
        return ResponseEntity.ok().build();
    }

    // 删除知识点
    @DeleteMapping("/points/{pointId}")
    public ResponseEntity<Void> deleteKnowledgePoint(
            @PathVariable Long teacherId,
            @PathVariable Long pointId) {
        knowledgeGraphService.deleteKnowledgePoint(pointId);
        return ResponseEntity.ok().build();
    }

    // AI生成知识图谱
//    @PostMapping("/{courseId}/generate-by-ai")
//    public ResponseEntity<KnowledgeGraphDTO> generateByAI(
//            @PathVariable Long teacherId,
//            @PathVariable Long courseId) {
//        KnowledgeGraphDTO graph = knowledgeGraphService.generateByAI(courseId);
//        System.out.println(graph);
//        return ResponseEntity.ok(graph);
//    }

}