package com.example.smartlearn.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskTypeStatDTO {
    private String type;
    private int total;
    private int completed;
    private int pending;
    private int overdue;
}

