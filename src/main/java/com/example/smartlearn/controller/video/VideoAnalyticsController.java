package com.example.smartlearn.controller.video;

import com.example.smartlearn.dto.VideoAnalyticsDTO;
import com.example.smartlearn.dto.VideoResourceAnalyticsDTO;
import com.example.smartlearn.model.VideoResourceAnalytics;
import com.example.smartlearn.repository.ClassResourceRepository;
import com.example.smartlearn.repository.StudentRepository;
import com.example.smartlearn.repository.VideoEventRepository;
import com.example.smartlearn.repository.VideoResourceAnalyticsRepository;
import com.example.smartlearn.service.video.VideoAnalyticsServide;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/video")
public class VideoAnalyticsController {
    private final VideoAnalyticsServide videoAnalyticsServide;
//    private final VideoResourceAnalytics videoResourceAnalytics;
    private final VideoEventRepository videoEventRepository;
    private final VideoResourceAnalyticsRepository videoResourceAnalyticsRepository;
    private final ClassResourceRepository classResourceRepository;
    private final StudentRepository studentRepository;

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<String > generateVideoAnalytics(){
        try {
            System.out.println("手触发视频分析数据生成");
            videoAnalyticsServide.generateDeatilAnalytics();
            return ResponseEntity.ok("视频数据分析任务启动");

        }catch (Exception e){
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    //获取某个视频资源的所有数据
    @GetMapping("/resources/{resourceId}")
    public ResponseEntity<VideoAnalyticsDTO> getResourceAnalytics(@PathVariable Long resourceId){

            System.out.println("获取数据资源："+resourceId);
            return ResponseEntity.ok(videoAnalyticsServide.getCombinedAnalyticsData(resourceId,null));

    }
    @GetMapping("/resources/{resourceId}/students/{studentId}")
    public ResponseEntity<VideoAnalyticsDTO> getStudentResourceAnalytics(@PathVariable Long resourceId,
                                                                         @PathVariable Long studentId){
        System.out.println("获取某学生资源数据"+resourceId+studentId);
        return ResponseEntity.ok(videoAnalyticsServide.getCombinedAnalyticsData(resourceId,studentId));
    }
    @GetMapping("/resources/{resourceId}/hotspots")
    public ResponseEntity<List<VideoResourceAnalytics.HotSpot>> getHotSpot(
            @PathVariable Long resourceId
    ){
        System.out.println("获取资源 的热点区域"+resourceId);
        VideoAnalyticsDTO analyticsDTO = videoAnalyticsServide.getCombinedAnalyticsData(resourceId,null);
        return ResponseEntity.ok(analyticsDTO.getHotSpots());
    }
    @GetMapping(value = "/resources/{resourceId}/teacher",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTeacherResourceAnalytics(
            @PathVariable Long resourceId) {
        try {
            VideoAnalyticsDTO fullData = videoAnalyticsServide.getCombinedAnalyticsData(resourceId, null);

            if (fullData == null) {
                return ResponseEntity.notFound().build();
            }

            VideoResourceAnalyticsDTO teacherData = new VideoResourceAnalyticsDTO();
            teacherData.setTotalViewers(fullData.getTotalViewers());
            teacherData.setAverageWatchTime(fullData.getAverageWatchTime());
            teacherData.setCompletionRate(fullData.getCompletionRate());
            teacherData.setHotSpots(fullData.getHotSpots());

            // 测试序列化
            try {
                new ObjectMapper().writeValueAsString(teacherData);
                return ResponseEntity.ok(teacherData);
            } catch (JsonProcessingException e) {
                System.out.println("JSON序列化失败"+ e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "数据序列化失败"));
            }

        } catch (Exception e) {
            System.out.println("处理请求时出错"+ e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "处理请求时出错"));
        }
    }

}
