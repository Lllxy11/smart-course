package com.example.smartlearn.service.video;

import com.example.smartlearn.dto.VideoAnalyticsDTO;
import com.example.smartlearn.model.ClassResource;
import com.example.smartlearn.model.Student;
import com.example.smartlearn.model.VideoEvent;
import com.example.smartlearn.model.VideoResourceAnalytics;
import com.example.smartlearn.repository.ClassResourceRepository;
import com.example.smartlearn.repository.StudentRepository;
import com.example.smartlearn.repository.VideoEventRepository;
import com.example.smartlearn.repository.VideoResourceAnalyticsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class VideoAnalyticsServide {
    private final VideoEventRepository videoEventRepository;
    private final VideoResourceAnalyticsRepository videoResourceAnalyticsRepository;
    private final ObjectMapper objectMapper;
    private final ClassResourceRepository classResourceRepository;
    private final StudentRepository studentRepository;


    @Autowired
    public VideoAnalyticsServide(VideoEventRepository videoEventRepository
            ,VideoResourceAnalyticsRepository videoResourceAnalyticsRepository
    ,ObjectMapper objectMapper,ClassResourceRepository classResourceRepository,
                                 StudentRepository studentRepository) {
        this.videoEventRepository = videoEventRepository;
        this.videoResourceAnalyticsRepository = videoResourceAnalyticsRepository;
        this.objectMapper = objectMapper;
        this.classResourceRepository = classResourceRepository;
        this.studentRepository = studentRepository;
    }
    @Transactional
    //@Scheduled(cron = "")
    public void generateDeatilAnalytics(){
        System.out.println("开始生成每日的视频分析数据");
        generateResourceAnalytics(null);
        List<Object[]> studentResources=videoEventRepository.findDistinctStudentResourcePairs();
        studentResources.forEach(
                pair->{
                    Long studentId=(Long) pair[0];
                    Long resourceId=(Long) pair[1];
                    generateStudentResourceAnalytics(studentId,resourceId);
                }
        );
        System.out.println("每日的视频分析数据生成完成");

    }
    private void generateResourceAnalytics(Long resourceId){
        List<ClassResource> resources=new ArrayList<>();
        if(resourceId==null){
            resources=classResourceRepository.findAll();
        }else {
            resources= Collections.singletonList(classResourceRepository.findById(resourceId.intValue()).orElse(null));

        }

        resources.forEach(r->{
            if(r==null){
                return;
            }
//            if(videoResourceAnalyticsRepository.existsTodayAnalysis(r.getResourceId().longValue(),null)){
//                return;
//            }

            VideoResourceAnalytics videoResourceAnalytics=new VideoResourceAnalytics();
            videoResourceAnalytics.setResource(r);
            //调用视频数据计算的方法
            calculateResourceMetrics(r.getResourceId(),videoResourceAnalytics);
            System.out.println('C');

            videoResourceAnalyticsRepository.save(videoResourceAnalytics);
        });


    }

    private void generateStudentResourceAnalytics(Long studentId,Long resourceId){
        if(videoResourceAnalyticsRepository.existsTodayAnalysis(studentId,resourceId)){
            return;//今天的数据已经收集过了 不用再收集了
        }
        VideoResourceAnalytics videoResourceAnalytics = new VideoResourceAnalytics();
        ClassResource classResource = classResourceRepository.findById(resourceId.intValue()).orElse(null);

        videoResourceAnalytics.setResource(classResource);
        Student student = studentRepository.findById(studentId).orElse(null);
        videoResourceAnalytics.setStudent(student);

        calculateStudentMetrics(studentId,resourceId,videoResourceAnalytics);
        videoResourceAnalyticsRepository.save(videoResourceAnalytics);

    }
    //这是计算某个视频资源的计算方法

    private void calculateResourceMetrics(Long resourceId,VideoResourceAnalytics videoResourceAnalytics){
        //视频总观看人数，经过去重的那种
        //计算某视频平均观看时长
        Long totalViewers=videoEventRepository.countUniqueViewersByResource(resourceId);
        Double avgWatchTime=videoEventRepository.calculateAverageWatchTime(resourceId);
        videoResourceAnalytics.setTotalViews(totalViewers.intValue());
        videoResourceAnalytics.setAverageWatchTime(avgWatchTime);


        List<VideoEvent> completionEvents=videoEventRepository.findByResourceIdAndEventType(
                resourceId,VideoEvent.EventType.ENDED
        );
        //计算某资源完播率
        double completionRate=totalViewers == 0?0:(double)completionEvents.size()/totalViewers;
        videoResourceAnalytics.setCompletionRate(completionRate);

        //计算热点区域
        List<Object[]> hopSpotsData=videoEventRepository.findHotSpotsByResource(resourceId);
        System.out.println('a');
        List<VideoResourceAnalytics.HotSpot> hotSpots=convertHopSpots(hopSpotsData);
        System.out.println("b");
        videoResourceAnalytics.setHotSpots(hotSpots);
        // 在calculateResourceMetrics方法中添加检查
        Long eventCount = videoEventRepository.countByResourceId(resourceId);
        System.out.println("该视频总事件记录数: " + eventCount);

        if(eventCount == 0) {
            System.out.println("警告: 没有找到该视频的任何观看事件");
        }


    }
    //热点区域转换，过滤掉没有开始时间或者是重复次数的返回数据
    //结束时间是开始时间+10，相当于默认每个热点片段持续是10秒
    private List<VideoResourceAnalytics.HotSpot> convertHopSpots(List<Object[]> hotSpotsData) {
        return hotSpotsData.stream()
                .filter(Objects::nonNull)
                .filter(row -> row.length == 2)
                .map(row -> {
                    try {
                        // 修改为安全的类型转换
                        Long startTimeLong = (Long) row[0];
                        Long replayCountLong = (Long) row[1];

                        int startTime = startTimeLong != null ? startTimeLong.intValue() : 0;
                        int replayCount = replayCountLong != null ? replayCountLong.intValue() : 0;

                        System.out.println("转换成功 - startTime: " + startTime + ", replayCount: " + replayCount);
                        return new VideoResourceAnalytics.HotSpot(startTime, startTime + 10, replayCount);
                    } catch (Exception e) {
                        System.out.println("转换出错: " + e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    //这是学生观看某个视频资源的数据获取逻辑
    private void calculateStudentMetrics(Long studentId,Long resourceId,VideoResourceAnalytics videoResourceAnalytics){
        //计算学生观看这个视频的次数
        Long totalViewCount=videoEventRepository.countByStudentIdAndResourceId(studentId,resourceId);
        videoResourceAnalytics.setStudentWatchCount(totalViewCount.intValue());
        //学生的最大观看进度
        videoResourceAnalytics.setStudentLastWatchTime(videoEventRepository.findMaxProgressByStudentAndResource(studentId,resourceId));
        //计算学生观看比率，就是最大观看进度/视频长度
        Long videoDuration=videoEventRepository.findDurationByResourceId(resourceId);
        Double videoProgressRate=videoDuration==null || videoDuration==0?0:(double)videoDuration/videoDuration;
        videoResourceAnalytics.setStudentProgressRate(videoProgressRate);

    }
    //数据综合，归档
    private void mapToDTP(VideoResourceAnalytics videoResourceAnalytics,VideoAnalyticsDTO dto){
        dto.setAverageWatchTime(videoResourceAnalytics.getAverageWatchTime());
        dto.setCompletionRate(videoResourceAnalytics.getCompletionRate());
        dto.setHotSpots(videoResourceAnalytics.getHotSpots());
        dto.setTotalViewers(videoResourceAnalytics.getTotalViews());

    }
    public VideoAnalyticsDTO getCombinedAnalyticsData(Long resourceId,Long studentId){
        System.out.println('B');
        generateResourceAnalytics(resourceId);
        VideoAnalyticsDTO videoAnalyticsDTO=new VideoAnalyticsDTO();

        videoResourceAnalyticsRepository.findByResourceIdAndStudentIsNullOrderByAnalysisTimeDesc(
                resourceId, PageRequest.of(0, 1)
        ).stream().findFirst().ifPresent(vra->mapToDTP(vra,videoAnalyticsDTO));
        System.out.println('A');

        if(studentId!=null){
            videoResourceAnalyticsRepository.findByResourceIdAndStudentIdOrderByAnalysisTimeDesc(
                    resourceId,studentId,PageRequest.of(0, 1)
            ).stream().findFirst().ifPresent(vra->{
                videoAnalyticsDTO.setStudentProgressRate(vra.getStudentProgressRate());
                videoAnalyticsDTO.setAverageWatchTime(vra.getAverageWatchTime());
                videoAnalyticsDTO.setCompletionRate(vra.getCompletionRate());
            });

        }
        System.out.println(videoAnalyticsDTO);
        return videoAnalyticsDTO;

    }


}
