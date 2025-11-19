package com.example.smartlearn.dto.response;

import java.util.ArrayList;
import java.util.List;

public class KnowldegePointGet {
    private List<String> concepts;
    private int doc_count;
    private List<String> hierarchy;
    private List<String> sources;
    public KnowldegePointGet() {}
    public KnowldegePointGet(List<String> concepts) {
        this.concepts = concepts;
        this.doc_count = concepts.size();
        this.hierarchy = new ArrayList<>();
        this.sources = new ArrayList<>();
    }

    public List<String> getHierarchy() {
        return hierarchy;
    }
    public void setHierarchy(List<String> hierarchy) {
        this.hierarchy = hierarchy;
    }
    public List<String> getSources() {
        return sources;
    }
    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public int getDoc_count() {
        return doc_count;
    }
    public void setDoc_count(int doc_count) {
        this.doc_count = doc_count;
    }

    public List<String> getConcepts() {
        return concepts;
    }
    public void setConcepts(List<String> concepts) {
        this.concepts = concepts;
    }
}
