package com.example.facenet;

import kotlin.random.Random;
import kotlin.random.URandomKt;

public class ConfidenceItem {
    private String class_name;
    private float dist;

    static float PI = (float) Math.acos(-1);

    public ConfidenceItem(String class_name, float dist) {
        dist += 1;
        dist /= 2;
        this.class_name = class_name;
        this.dist = dist;
    }

    public float getCos() {
        return 1 - (float) ((float) Math.acos(1 - dist / 2) / Math.acos(-1));
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

    public void setDist(float dist) {
        this.dist = dist;
    }
}
