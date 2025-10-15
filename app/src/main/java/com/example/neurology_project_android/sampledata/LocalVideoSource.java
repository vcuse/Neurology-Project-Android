package com.example.neurology_project_android.sampledata;

import android.content.Context;

import androidx.annotation.Nullable;

import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.Logging;
import org.webrtc.MediaStreamTrack;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

public class LocalVideoSource extends LocalSource{

    private static final String TAG = "LocalVideoSource";

    private VideoCapturer capturer;
    private VideoSource source;
    private final Context appContext;
    private SurfaceTextureHelper surfaceTextureHelper;

    public VideoTrack track;

    public LocalVideoSource(Context context, EglBase rootEglBase, VideoSource videoSource, VideoTrack videoTrack){
        appContext = context;
        source = videoSource;
        track = videoTrack;
        //
        capturer = createCameraCapturer(new Camera2Enumerator(appContext));
        surfaceTextureHelper =
                SurfaceTextureHelper.create("CaptureThread", rootEglBase.getEglBaseContext());
        capturer.initialize(surfaceTextureHelper, appContext, source.getCapturerObserver());
        capturer.startCapture(720, 1280, 30);
    }

    @Override
    public MediaStreamTrack getTrack() {
        return track;
    }

    @Override
    public String getKind() {
        return "video";
    }


    //    public void play(Player player){
//        track.addSink(player);
//    }

    private @Nullable VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Logging.d(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
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