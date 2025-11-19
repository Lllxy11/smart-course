package com.example.smartlearn.controller.teacher;

import com.example.smartlearn.dto.ResourceDTO;
import com.example.smartlearn.service.teacher.KnowledgeGraphService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge-points")
public class KnowledgePointResourceController {

    private final KnowledgeGraphService knowledgeGraphService;
    public KnowledgePointResourceController(KnowledgeGraphService knowledgeGraphService) {
        this.knowledgeGraphService = knowledgeGraphService;
    }


    // 获取知识点资源
    @GetMapping("/{pointId}/resources")
    public ResponseEntity<List<ResourceDTO>> getPointResources(
            @PathVariable Long pointId) {

        // 调用服务层获取资源列表
        List<ResourceDTO> resources = knowledgeGraphService.getPointResources(pointId);
        return ResponseEntity.ok(resources);
    }

    // 添加资源关联
    @PostMapping("/{pointId}/resources/{resourceId}")
    public ResponseEntity<Void> addResourceAssociation(
            @PathVariable Long pointId,
            @PathVariable Long resourceId,
            @RequestBody Map<String, String> requestBody) { // 添加请求体参数

        String linkedAt = requestBody.get("linkedAt");
        knowledgeGraphService.addResourceAssociation(pointId, resourceId, linkedAt);
        return ResponseEntity.ok().build();
    }

    // 移除资源关联
    @DeleteMapping("/{pointId}/resources/{resourceId}")
    public ResponseEntity<Void> removeResourceAssociation(
            @PathVariable Long pointId,
            @PathVariable Long resourceId) {
        knowledgeGraphService.removeResourceAssociation(pointId, resourceId);
        return ResponseEntity.ok().build();
    }
}