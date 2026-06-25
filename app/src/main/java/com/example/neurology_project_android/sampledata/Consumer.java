package com.example.neurology_project_android.sampledata;

import org.webrtc.MediaStreamTrack;
import org.webrtc.RtpReceiver;

import java.util.Objects;

class Consumer {
    String id;
    String mid;
    String peerId;
    Engine.MediaKind kind;

    MediaStreamTrack track;
    Consumer(String id, String mid){
        id = id;
        mid = mid;
    }

    public void setTrack(MediaStreamTrack track) {
        this.track = track;

        if (Objects.equals(track.kind(), "audio")){
            kind = Engine.MediaKind.Audio;
        } else {
            kind = Engine.MediaKind.Video;
        }
    }



    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }
}