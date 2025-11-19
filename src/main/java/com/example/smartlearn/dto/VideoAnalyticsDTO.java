package com.example.smartlearn.dto;

import com.example.smartlearn.model.VideoResourceAnalytics;

import java.util.List;

public class VideoAnalyticsDTO {
    private Integer totalViewers;
    private Double averageWatchTime;
    private Double completionRate;
    private List<VideoResourceAnalytics.HotSpot> hotSpots;
    private Integer studentWatchCount;
    private Integer studentLastWatchTime;
    private Double studentProgressRate;

    public void setAverageWatchTime(Double averageWatchTime) {
        this.averageWatchTime = averageWatchTime;
    }
    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }
    public void setHotSpots(List<VideoResourceAnalytics.HotSpot> hotSpots) {
        this.hotSpots = hotSpots;
    }
    public void setStudentWatchCount(Integer studentWatchCount) {
        this.studentWatchCount = studentWatchCount;
    }
    public void setStudentLastWatchTime(Integer studentLastWatchTime) {
        this.studentLastWatchTime = studentLastWatchTime;
    }
    public void setStudentProgressRate(Double studentProgressRate) {
        this.studentProgressRate = studentProgressRate;
    }
    public void setTotalViewers(Integer totalViewers) {
        this.totalViewers = totalViewers;
    }

    public List<VideoResourceAnalytics.HotSpot> getHotSpots() {
        return hotSpots;
    }

    public Double getAverageWatchTime() {
        return averageWatchTime;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public Double getStudentProgressRate() {

        return studentProgressRate;
    }
    public Integer getTotalViewers() {
        return totalViewers;
    }
    public Integer getStudentWatchCount() {
        return studentWatchCount;
    }

    public Integer getStudentLastWatchTime() {
            return studentLastWatchTime;
    }
}
