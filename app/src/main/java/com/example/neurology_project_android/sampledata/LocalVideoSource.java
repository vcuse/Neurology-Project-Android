package com.example.neurology_project_android.sampledata;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;

import androidx.annotation.Nullable;

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
    public VideoTrack track;
    private final VideoCapturer capturer;
    private final VideoSource source;
    private final SurfaceTextureHelper surfaceTextureHelper;

    public LocalVideoSource(Context context, EglBase rootEglBase, VideoSource videoSource, VideoTrack videoTrack) throws CameraAccessException {
        appContext = context;
        source = videoSource;
        track = videoTrack;
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        //String lastCameraId = cameraManager.getCameraIdList()[cameraManager.getCameraIdList().length-1];
        String lastCameraId = cameraManager.getCameraIdList()[cameraManager.getCameraIdList().length - 1];
        Camera2Capturer cameraCapturer = new Camera2Capturer(context, lastCameraId, null);

        CameraEnumerator enumerator = new Camera2Enumerator(appContext);
        capturer = createCameraCapturer(enumerator);
//        capturer = createCameraCapturer(new Camera2Enumerator(appContext));
        surfaceTextureHelper =
                SurfaceTextureHelper.create("CaptureThread", rootEglBase.getEglBaseContext());
//        capturer.initialize(surfaceTextureHelper, appContext, source.getCapturerObserver());
//        capturer.startCapture(1080, 1920, 30);
        cameraCapturer.initialize(surfaceTextureHelper, appContext, source.getCapturerObserver());
        cameraCapturer.startCapture(1920, 1080, 30);
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
//            if (enumerator.isFrontFacing(deviceName)) {
            Logging.d(TAG, "Creating camera capturer.");
            VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

            if (videoCapturer != null) {
                return videoCapturer;
            }
//            }
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