package com.example.zhuos.sound_activity_recorder;


import java.io.Serializable;

public class Gesture implements Serializable {

    private long startTime,currentTime;
    private float startX,startY,currentX,currentY;
    private GestureType type;


    public Gesture(long startTime, long currentTime, float startX, float startY, float currentX, float currentY, GestureType type) {
        this.startTime = startTime;
        this.currentTime = currentTime;
        this.startX = startX;
        this.startY = startY;
        this.currentX = currentX;
        this.currentY = currentY;
        this.type = type;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public float getStartX() {
        return startX;
    }

    public float getStartY() {
        return startY;
    }

    public float getCurrentX() {
        return currentX;
    }

    public float getCurrentY() {
        return currentY;
    }

    public GestureType getType() {
        return type;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public void setCurrentX(float currentX) {
        this.currentX = currentX;
    }

    public void setCurrentY(float currentY) {
        this.currentY = currentY;
    }

    public void setType(GestureType type) {
        this.type = type;
    }
}


