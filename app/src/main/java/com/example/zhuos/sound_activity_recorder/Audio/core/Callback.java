package com.example.zhuos.sound_activity_recorder.Audio.core;

public interface Callback {
    void onBufferAvailable(byte[] buffer);
}