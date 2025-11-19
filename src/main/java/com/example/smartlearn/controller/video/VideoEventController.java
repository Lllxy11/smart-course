package com.example.smartlearn.controller.video;


import com.example.smartlearn.dto.VideoEventDto;
import com.example.smartlearn.model.ClassResource;
import com.example.smartlearn.model.Student;
import com.example.smartlearn.model.VideoEvent;
import com.example.smartlearn.repository.ClassResourceRepository;
import com.example.smartlearn.repository.StudentRepository;
import com.example.smartlearn.repository.VideoEventRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/video/events")
@RequiredArgsConstructor
public class VideoEventController {
    private final VideoEventRepository videoEventRepository;
    private final StudentRepository studentRepository;
    private final ClassResourceRepository classResourceRepository;

    @PostMapping
    public ResponseEntity<?> recordEvent(@RequestBody VideoEventDto eventDto) {
        try {

            // 获取当前学生(根据你的认证系统调整)
           Student student = studentRepository.findById(eventDto.getStudentId())
                    .orElseThrow(() -> new EntityNotFoundException("Student not found"));

            ClassResource resource = classResourceRepository.findById(eventDto.getResourceId().intValue())
                    .orElseThrow(() -> new EntityNotFoundException("Resource not found"));

            VideoEvent event = new VideoEvent();
            event.setStudent(student);
            event.setResource(resource);
            event.setEventType(VideoEvent.EventType.valueOf(eventDto.getEventType()));
            event.setVideoTimestamp(eventDto.getVideoTimestamp());
            event.setSessionId(eventDto.getSessionId());
            event.setDuration(eventDto.getDuration());

            videoEventRepository.save(event);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}