package com.example.smartlearn.service.teacher;

import com.example.smartlearn.dto.response.KnowldegePointGet;
import com.example.smartlearn.dto.response.KnowledgePointResponse;
import com.example.smartlearn.model.Course;
import com.example.smartlearn.model.KnowledgePoint;
import com.example.smartlearn.repository.ClassResourceRepository;
import com.example.smartlearn.repository.CourseRepository;
import com.example.smartlearn.repository.KnowledgePointRepository;
import com.example.smartlearn.repository.KnowledgePointResourceRepository;
import com.example.smartlearn.util.KnowledgePointExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class KnowledgePointService {

    private final KnowledgePointRepository knowledgePointRepository;
    private final Class_ResourceService class_ResourceService;
    private final ClassResourceRepository classResourceRepository;
    private final CourseRepository courseRepository;
    private final KnowledgePointResourceRepository knowledgePointResourceRepository;

    @Autowired
    public KnowledgePointService(KnowledgePointRepository knowledgePointRepository, Class_ResourceService class_ResourceService,
                                 CourseRepository courseRepository, ClassResourceRepository classResourceRepository,
                                 KnowledgePointResourceRepository knowledgePointResourceRepository) {
        this.knowledgePointRepository = knowledgePointRepository;
        this.class_ResourceService = class_ResourceService;
        this.classResourceRepository = classResourceRepository;
        this.knowledgePointResourceRepository = knowledgePointResourceRepository;
        this.courseRepository = courseRepository;

    }

    public List<KnowledgePointResponse> getByCourseId(Long courseId) {
        List<KnowledgePoint> list = knowledgePointRepository.findByCourse_CourseId(courseId);
        return list.stream()
                .map(kp -> new KnowledgePointResponse(kp.getId(), kp.getName(), kp.getDescription()))
                .collect(Collectors.toList());
    }

    public List<KnowledgePointResponse> searchByCourseIdAndName(Long courseId, String name) {
        List<KnowledgePoint> list = knowledgePointRepository.findByCourse_CourseIdAndNameContaining(courseId, name);
        return list.stream()
                .map(kp -> new KnowledgePointResponse(kp.getId(), kp.getName(), kp.getDescription()))
                .collect(Collectors.toList());
    }
    @Transactional
    public KnowldegePointGet saveKnowledgePointaAI(Long courseId,List<Long> resourceIds) {
        List<String> filePaths = class_ResourceService.getCourseResourcePathByIds((List<Long>) resourceIds);
//        List<String> filePaths = class_ResourceService.getCourseResourceLocalPath(courseId);
        System.out.println("<UNK文件路径>" + filePaths);
        if (filePaths == null || filePaths.size() == 0) {
            throw new IllegalArgumentException("该课程没有可以处理的资源");
        }
        KnowldegePointGet knowldegePointGet = KnowledgePointExtractor.extractFromFiles(filePaths);
        List<String> concepts = knowldegePointGet.getConcepts();
        List<String> parents = knowldegePointGet.getHierarchy();
        List<String> urls = knowldegePointGet.getSources();
        System.out.println(urls);


        for (String concept : concepts) {
            System.out.println(concept);
            System.out.println(courseId);
            if (!knowledgePointRepository.existsByNameAndCourse_CourseId(concept, courseId)) {
                Course course = courseRepository.findById(courseId).get();
                KnowledgePoint knowledgePoint = new KnowledgePoint();
                knowledgePoint.setName(concept);
                knowledgePoint.setCourse(course);
                System.out.println("AAAAAAAAAAAAAAAAAAAA");
                knowledgePointRepository.save(knowledgePoint);
                System.out.println("生成ID：" + knowledgePoint.getId());
                System.out.println("BBBBBBBBBBBB");
            }
        }

        for (int i = 0; i < concepts.size(); i++) {
            KnowledgePoint knowledgePoint = knowledgePointRepository.findByNameAndCourse_CourseId(concepts.get(i), courseId);//找到要添加东西的一列
            String parent = parents.get(i);
            Long parentId = 0L;
            List<KnowledgePoint> knowledgePoints = knowledgePointRepository.findByCourse_CourseId(courseId);
            for (KnowledgePoint knowledgePoint1 : knowledgePoints) {
                if (knowledgePoint1.getName().equals(parent)) {
                    parentId = knowledgePoint1.getId();
                    break;
                }
            }

            knowledgePoint.setParentId(parentId);
            knowledgePointRepository.save(knowledgePoint);

        }

        return knowldegePointGet;






    }

}