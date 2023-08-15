package com.example.facenet;

import java.util.List;

public class ClassifiedItem {
    private String className;
    private List<ConfidenceItem> confidences;
    float[] feat;

    public ClassifiedItem(String className, List<ConfidenceItem> confidences, float[] feat) {
        this.className = className;
        this.confidences = confidences;
        this.feat = feat;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<ConfidenceItem> getConfidences() {
        return confidences;
    }

    public void setConfidences(List<ConfidenceItem> confidences) {
        this.confidences = confidences;
    }
}
