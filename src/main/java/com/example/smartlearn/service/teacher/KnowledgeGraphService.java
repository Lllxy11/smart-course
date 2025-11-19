// KnowledgeGraphService.java
package com.example.smartlearn.service.teacher;

import com.example.smartlearn.dto.*;
import com.example.smartlearn.dto.response.KnowldegePointGet;
import com.example.smartlearn.model.*;
import com.example.smartlearn.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class KnowledgeGraphService {
    private final KnowledgePointRepository pointRepository;
    private final KnowledgePointResourceRepository resourceRepo;
    private final ClassResourceRepository classResourceRepo;
    private final KnowledgePointService knowledgePointService;
    private final CourseRepository courseRepository;
    private final Class_ResourceService class_ResourceService;
    private final KnowledgePointResourceRepository knowledgePointResourceRepository;
    private final KnowledgePointRepository knowledgePointRepository;
    private final ClassResourceRepository classResourceRepository;
    private final QuestionRepository questionRepository;
    // 创建知识点classResourceRepo
    public Long createKnowledgePoint(KnowledgePointDTO dto) {
        Course course=new Course();
        course=courseRepository.findById(dto.getCourseId()).get();
        KnowledgePoint point = new KnowledgePoint();
        point.setName(dto.getName());
        point.setDescription(dto.getDescription());
        point.setCourse(course);
        point.setPositionX(dto.getPositionX());
        point.setPositionY(dto.getPositionY());

        if (dto.getParentId() != null) {
            point.setParentId(dto.getParentId());
        }

        point = pointRepository.save(point);
        return point.getId();
    }







    // 获取知识图谱
    @Transactional
    public KnowledgeGraphDTO getCourseKnowledgeGraph(Long courseId) {


        List<KnowledgePoint> points = pointRepository.findByCourseCourseId(courseId);
        List<KnowledgeGraphNodeDTO> nodes = points.stream()
                .map(this::convertToNodeDTO)
                .collect(Collectors.toList());

        List<KnowledgeGraphEdgeDTO> edges = points.stream()
                .filter(point -> point.getParentId() != null)
                .map(point -> new KnowledgeGraphEdgeDTO(
                        "knowledge_" + point.getParentId(),
                        "knowledge_" + point.getId(),
                        "subtopic"
                ))
                .collect(Collectors.toList());

        return new KnowledgeGraphDTO(nodes, edges);



    }

    // 知识点转节点DTO
    public KnowledgeGraphNodeDTO convertToNodeDTO(KnowledgePoint point) {
        KnowledgeGraphNodeDTO node = new KnowledgeGraphNodeDTO();
        node.setId("knowledge_" + point.getId());
        node.setTitle(point.getName());
        node.setDescription(point.getDescription());
        node.setCourseId(point.getCourse().getCourseId());
        node.setPositionX(point.getPositionX());
        node.setPositionY(point.getPositionY());
        node.setResources(getPointResources(point.getId()));
        return node;
    }

    // 获取知识点资源
    public List<ResourceDTO> getPointResources(Long pointId) {

        List<KnowledgePointResource> associations = resourceRepo.findAssociationsWithResourceByPointId(pointId);


        return associations.stream()
                .map(assoc -> {
                    ResourceDTO dto = new ResourceDTO();
                    dto.setId(assoc.getResource().getResourceId());
                    dto.setName(assoc.getResource().getName());
                    dto.setType(assoc.getResource().getType().name());
                    dto.setUrl(assoc.getResource().getUrl());
                    dto.setLinkedAt(assoc.getLinkedAt().toString());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 更新位置
    public void updatePosition(Long pointId, PositionUpdateDTO position) {
        pointRepository.findById(pointId).ifPresent(point -> {
            point.setPositionX(position.getPositionX());
            point.setPositionY(position.getPositionY());
            pointRepository.save(point);
        });
    }

    // 删除知识点
    public void deleteKnowledgePoint(Long pointId) {

        // 递归删除子节点
        List<KnowledgePoint> knowledgePoints=pointRepository.findByParentId(pointId);
        for (KnowledgePoint knowledgePoint : knowledgePoints) {
            knowledgePoint.setParentId(null);
            knowledgePointRepository.save(knowledgePoint);
            System.out.println('B');
        }
        System.out.println('A');
        // 删除关联资源
        resourceRepo.deleteByKnowledgePointId(pointId);

        // 删除节点本身
        pointRepository.deleteById(pointId);
    }
    private void deleteKnowledgePointAll(Long courseId) {
        List<Question> questions=questionRepository.findByCourseCourseId(courseId);
        for (Question question : questions) {
            question.setKnowledgePoint(null);
            questionRepository.save(question);
        }
        resourceRepo.deleteAllByCourseId(courseId);
        pointRepository.deleteAllByCourseId(courseId);
    }

    // AI生成知识图谱
    public KnowledgeGraphDTO generateByAI(Long courseId,List<Long> resourceIds) {
        deleteKnowledgePointAll(courseId);
        pointRepository.deleteAllByCourseId(courseId);
        System.out.println("获取知识图谱：");
        KnowldegePointGet knowledgePointGet = knowledgePointService.saveKnowledgePointaAI(courseId,resourceIds);
//        KnowldegePointGet knowldegePointGet=knowledgePointService.saveKnowledgePointaAI(courseId);

        List<String> urls = knowledgePointGet.getSources();
        List<String> concepts = knowledgePointGet.getConcepts();
        for (int i = 0; i < concepts.size(); i++) {
            try {
                // 1. 验证知识点存在
                KnowledgePoint knowledgePoint = knowledgePointRepository.findByNameAndCourse_CourseId(concepts.get(i), courseId);
                if (knowledgePoint == null) {
                    System.err.println("知识点不存在: " + concepts.get(i));
                    continue;
                }
                System.out.println("知识点ID: " + knowledgePoint.getId());

                // 2. 验证资源存在
                String url = class_ResourceService.convertLocalPathToUrl(urls.get(i));
                Optional<ClassResource> resourceOpt = classResourceRepository.findByUrl(url);
                if (!resourceOpt.isPresent()) {
                    System.err.println("资源不存在: " + url);
                    continue;
                }
                ClassResource classResource = resourceOpt.get();
                System.out.println("资源ID: " + classResource.getResourceId());

                // 3. 创建并保存关联
                KnowledgePointResource kpr = new KnowledgePointResource();
                kpr.setKnowledgePoint(knowledgePoint);
                kpr.setResource(classResource);
                kpr.setLinkedAt(new Date());
                kpr.setKnowledgePoint(knowledgePoint);
                kpr.setResource(classResource);

                // 4. 保存并立即刷新
                knowledgePointResourceRepository.save(kpr);



            } catch (Exception e) {
                System.err.println("保存过程中出错: ");
                e.printStackTrace();
            }
        }
        System.out.println("AAAAAAAAAAAAAAAAAAAA");

        List<KnowledgePoint> points = pointRepository.findByCourseCourseId(courseId);
        List<KnowledgeGraphNodeDTO> nodes = points.stream()
                .map(this::convertToNodeDTO)
                .collect(Collectors.toList());

        List<KnowledgeGraphEdgeDTO> edges = points.stream()
                .filter(point -> point.getParentId() != null)
                .map(point -> new KnowledgeGraphEdgeDTO(
                        "knowledge_" + point.getParentId(),
                        "knowledge_" + point.getId(),
                        "subtopic"
                ))
                .collect(Collectors.toList());

        return new KnowledgeGraphDTO(nodes, edges);
    }


    // 添加资源关联
    public void addResourceAssociation(Long pointId, Long resourceId, String linkedAt) {
        // 将字符串时间转换为Date类型
        Date linkedAtDate = Date.from(Instant.parse(linkedAt));

        // 创建关联
        KnowledgePointResource association = new KnowledgePointResource();
        KnowledgePoint knowledgePoint = pointRepository.findById(pointId).get();
        association.setKnowledgePoint(knowledgePoint);//改动的地方
        ClassResource classResource = classResourceRepository.findById(resourceId.intValue()).orElse(null);
        association.setResource(classResource);
        association.setLinkedAt(linkedAtDate);

        // 保存关联
        resourceRepo.save(association);
    }

    // 移除资源关联
    public void removeResourceAssociation(Long pointId, Long resourceId) {
        resourceRepo.deleteByKnowledgePointIdAndResourceResourceId(pointId, resourceId);
    }
}