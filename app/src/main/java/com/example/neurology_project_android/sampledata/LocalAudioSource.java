package com.example.neurology_project_android.sampledata;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.MediaStreamTrack;

public class LocalAudioSource extends  LocalSource{
    private AudioTrack track;
    private AudioSource source;

    public LocalAudioSource(AudioSource audioSource, AudioTrack audioTrack) {
        source = audioSource;
        track = audioTrack;
    }

    @Override
    public MediaStreamTrack getTrack() {
        return track;
    }

    @Override
    public String getKind() {
        return "audio";
    }
}