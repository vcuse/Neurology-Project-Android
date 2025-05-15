package com.example.neurology_project_android

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.usb.UsbDevice
import androidx.annotation.OptIn
import androidx.camera.core.imagecapture.CameraRequest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import org.webrtc.CameraVideoCapturer
import org.webrtc.CapturerObserver
import org.webrtc.EglBase
import org.webrtc.NV21Buffer
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoFrame
import org.webrtc.VideoProcessor
import org.webrtc.VideoSink
import org.webrtc.VideoSource
import java.nio.ByteBuffer

class VideoCameraSetup constructor(
    context: Context,
    localPeer: PeerConnection,
    factory: PeerConnectionFactory,
    rootEGL1: EglBase
){


    @SuppressLint("RestrictedApi")
    private lateinit var cameraRequest: CameraRequest
    private lateinit var videoProcessor: VideoProcessor
    private lateinit var videoSource: VideoSource
    private lateinit var capturerObserver: CapturerObserver
    private var rootEGL = rootEGL1

    private var localPeer = localPeer
    private var factory = factory

    private var cameraInitialized by mutableStateOf(false)

    init {
        prepareVideoDevices(context)


    }

    fun prepareVideoDevices(context: Context){

        val videoCapturerObserver = object: CapturerObserver {
            @OptIn(UnstableApi::class)
            override fun onCapturerStarted(p0: Boolean) {

                Log.e("Capturer Observer", "Capture Started")
            }

            override fun onCapturerStopped() {
                TODO("Not yet implemented")
            }

            override fun onFrameCaptured(p0: VideoFrame?) {


                //Log.e("Capturer Observer", "Frame Captured")
            }

        }


        val videoCapturer = object: VideoCapturer {
            @OptIn(UnstableApi::class)
            override fun initialize(
                p0: SurfaceTextureHelper?,
                p1: Context?,
                p2: CapturerObserver?
            ) {
                Log.e("Video Capturer", "Initialized")
            }

            @OptIn(UnstableApi::class)
            override fun startCapture(p0: Int, p1: Int, p2: Int) {
                Log.e("VIDEO CAPTURER" , "Start Capture")

            }

            override fun stopCapture() {
                TODO("Not yet implemented")
            }

            override fun changeCaptureFormat(
                p0: Int,
                p1: Int,
                p2: Int
            ) {
                TODO("Not yet implemented")
            }

            override fun dispose() {
                TODO("Not yet implemented")
            }

            override fun isScreencast(): Boolean {
                TODO("Not yet implemented")
            }

        }

        videoProcessor = object: VideoProcessor {
            override fun onCapturerStarted(p0: Boolean) {
                TODO("Not yet implemented")
            }

            override fun onCapturerStopped() {
                TODO("Not yet implemented")
            }

            override fun onFrameCaptured(p0: VideoFrame?) {
                TODO("Not yet implemented")
            }

            override fun setSink(p0: VideoSink?) {
                TODO("Not yet implemented")
            }

        }

        var timeStampNS: Long
        var n21Buffer: NV21Buffer
        var videoFrame: VideoFrame



        sendVideoCapturer(videoCapturer, context, videoCapturerObserver, localPeer, factory )
    }


    fun sendVideoCapturer(capturer: VideoCapturer, context: Context, capturerObserver: CapturerObserver, localPeer: PeerConnection, factory: PeerConnectionFactory) {

        //localPeer = factory.createPeerConnection(config, peerConnObserver)!!


        val videoSource2 = factory.createVideoSource(true)
        val surfaceTexture = SurfaceTextureHelper.create("CaptureThread", rootEGL.eglBaseContext)
        //capturerObserver.onCapturerStarted(true)
        //capturer.initialize(surfaceTexture,context , capturerObserver)


        //videoSource2.setVideoProcessor(videoProcessor)
        //videoProcessor.onCapturerStarted(true)
        //capturer.startCapture(1080, 720, 30)

        var videoTrack = factory.createVideoTrack("0001", videoSource2)
        //videoSource = videoSource2

        //localPeer.addTrack(videoTrack, listOf("track01"))



        //camera.initialize(surfaceTexture,context , capturerObserver)

    }
}