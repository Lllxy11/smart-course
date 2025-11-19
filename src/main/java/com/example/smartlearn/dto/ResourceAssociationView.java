package com.example.smartlearn.dto;

import java.util.Date;

public interface ResourceAssociationView {
    Long getResourceId();
    String getName();
    String getType();
    String getUrl();
    Date getLinkedAt();
}