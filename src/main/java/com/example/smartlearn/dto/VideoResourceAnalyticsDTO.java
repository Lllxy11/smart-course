package com.example.smartlearn.dto;

import com.example.smartlearn.model.VideoResourceAnalytics;

import java.util.List;

public class VideoResourceAnalyticsDTO {
    private Integer totalViewers;
    private Double averageWatchTime;
    private Double completionRate;
    private List<VideoResourceAnalytics.HotSpot> hotSpots;

    public VideoResourceAnalyticsDTO() {}

    // 必须添加getter方法
    public Integer getTotalViewers() {
        return totalViewers;
    }

    public Double getAverageWatchTime() {
        return averageWatchTime;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public List<VideoResourceAnalytics.HotSpot> getHotSpots() {
        return hotSpots;
    }

    // 保留原有的setter方法
    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }

    public void setTotalViewers(Integer totalViewers) {
        this.totalViewers = totalViewers;
    }

    public void setAverageWatchTime(Double averageWatchTime) {
        this.averageWatchTime = averageWatchTime;
    }

    public void setHotSpots(List<VideoResourceAnalytics.HotSpot> hotSpots) {
        this.hotSpots = hotSpots;
    }
}
