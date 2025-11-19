package com.example.smartlearn.service.student;

import com.example.smartlearn.model.ClassResource;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class WeightedResource {
    private ClassResource resource;
    private double weight;
    private List<Long> relatedKnowledgePointIds;
}
