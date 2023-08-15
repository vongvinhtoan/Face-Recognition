package com.example.facenet;

public class ConfidenceItem {
    private String class_name;
    private float dist;

    public ConfidenceItem(String class_name, float dist) {
        this.class_name = class_name;
        this.dist = dist;
    }

    public String getClass_name() {
        return class_name;
    }

    public void setClass_name(String class_name) {
        this.class_name = class_name;
    }

    public float getDist() {
        return dist;
    }

    public float getCos() {
        return (float) Math.acos(1 - dist/2);
    }

    public void setDist(float dist) {
        this.dist = dist;
    }
}
