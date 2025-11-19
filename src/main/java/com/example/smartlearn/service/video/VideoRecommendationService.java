package com.example.smartlearn.service.video;

import com.example.smartlearn.dto.VideoRecommendationDTO;
import com.example.smartlearn.model.ClassResource;
import com.example.smartlearn.model.StudentCourse;
import com.example.smartlearn.model.VideoEvent;
import com.example.smartlearn.model.VideoEvent.EventType;
import com.example.smartlearn.model.VideoResourceAnalytics;
import com.example.smartlearn.repository.ClassResourceRepository;
import com.example.smartlearn.repository.StudentCourseRepository;
import com.example.smartlearn.repository.VideoEventRepository;
import com.example.smartlearn.repository.VideoResourceAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class VideoRecommendationService {

    private final VideoResourceAnalyticsRepository videoRecommendationRepo;
    private final VideoEventRepository videoEventRepo;
    private final ClassResourceRepository classResourceRepo;
    private final StudentCourseRepository studentCourseRepo;

    // 个性化视频推荐
    public List<VideoRecommendationDTO> recommendVideos(Long studentId) {
        // 1. 获取学生所有课程
        List<StudentCourse> courses = studentCourseRepo.findByStudentStudentId(studentId);

        // 2. 提取课程ID列表
        List<Long> courseIds = courses.stream()
                .map(StudentCourse::getCourseId) // 假设StudentCourse有getCourseId方法
                .collect(Collectors.toList());

        if (courseIds.isEmpty()) {
            return Collections.emptyList(); // 如果没有课程，返回空列表
        }

        // 3. 查询每个课程的高质量视频
        List<VideoResourceAnalytics> highQualityVideos = new ArrayList<>();
        for (Long courseId : courseIds) {
            List<VideoResourceAnalytics> videos = videoRecommendationRepo
                    .findHighQualityVideos(courseId, PageRequest.of(0, 3));
            highQualityVideos.addAll(videos);
        }

// 4. 按完成率排序
        highQualityVideos.sort((v1, v2) -> Double.compare(v2.getCompletionRate(), v1.getCompletionRate()));

// 5. 去重
        Set<Long> seenResourceIds = new HashSet<>();
        List<VideoResourceAnalytics> distinctVideos = new ArrayList<>();

        for (VideoResourceAnalytics video : highQualityVideos) {
            Long resourceId = video.getResource().getResourceId();
            if (!seenResourceIds.contains(resourceId)) {
                seenResourceIds.add(resourceId);
                distinctVideos.add(video);
            }
        }

// 使用final临时变量
        final List<VideoResourceAnalytics> finalVideos = distinctVideos;

// 转换为DTO
        return finalVideos.stream()
                .map(va -> new VideoRecommendationDTO(
                        va.getResource().getResourceId(),
                        va.getResource().getName(),
                        va.getResource().getUrl(),
                        va.getResource().getDuration(),
                        va.getCompletionRate(),
                        finalVideos.contains(va) ? "根据您的学习效果推荐" : "热门推荐"
                ))
                .collect(Collectors.toList());
    }

    // 基于热点回放的推荐
    public List<VideoRecommendationDTO> recommendReplayVideos(Long studentId) {

        return videoEventRepo.findByStudentStudentId(studentId).stream()
                .filter(event -> event.getEventType() == EventType.SEEK)
                .collect(Collectors.groupingBy(VideoEvent::getResource))
                .entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .limit(3)
                .map(entry -> {
                    ClassResource resource = entry.getKey();
                    return new VideoRecommendationDTO(
                            resource.getResourceId(),
                            resource.getName(),
                            resource.getUrl(),
                            resource.getDuration(),
                            0.0,
                            "您曾多次回放此视频"
                    );
                })
                .collect(Collectors.toList());
    }
}