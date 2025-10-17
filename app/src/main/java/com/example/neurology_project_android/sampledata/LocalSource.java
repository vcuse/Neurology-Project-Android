package com.example.neurology_project_android.sampledata; // <-- Must be your package!


import org.webrtc.MediaStreamTrack;
// FIX 1: Make the class public so your RoomClient can inherit/reference it.
public abstract class LocalSource {

    // FIX 2 (Crucial): The abstract methods must be public
    // if you want external classes to rely on them.
    public abstract MediaStreamTrack getTrack();
    public abstract String getKind();


    // ... other abstract methods ...
}