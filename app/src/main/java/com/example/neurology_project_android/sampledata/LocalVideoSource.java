package com.example.neurology_project_android.sampledata;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;

import androidx.annotation.Nullable;

import com.example.neurology_project_android.GlassesVideoBridge;

import org.webrtc.Camera2Capturer;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.Logging;
import org.webrtc.MediaStreamTrack;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

public class LocalVideoSource extends LocalSource {

    private static final String TAG = "LocalVideoSource";
    private final Context appContext;

    private final VideoSource source;

    public VideoTrack track;

    public LocalVideoSource(
            Context context,
            EglBase rootEglBase,
            VideoSource videoSource,
            VideoTrack videoTrack
    ) throws CameraAccessException {

        appContext = context;
        source = videoSource;
        track = videoTrack;

        // Instead of starting Camera2, connect the
        // WebRTC VideoSource to the Ray-Ban frame bridge.
        GlassesVideoBridge.INSTANCE.attach(source);
    }

    @Override
    public MediaStreamTrack getTrack() {
        return track;
    }

    @Override
    public String getKind() {
        return "video";
    }



    private @Nullable VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Logging.d(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {

            Logging.d(TAG, "Creating camera capturer.");
            VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

            if (videoCapturer != null) {
                return videoCapturer;
            }

        }

        // Front facing camera not found, try something else
        Logging.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }
}