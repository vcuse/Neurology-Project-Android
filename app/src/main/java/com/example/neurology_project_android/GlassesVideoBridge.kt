package com.example.neurology_project_android


import org.webrtc.VideoFrame
import org.webrtc.VideoSource
import org.webrtc.CapturerObserver
import java.util.concurrent.Executors

object GlassesVideoBridge {

    // Keep all capturer callbacks on one thread.
    private val executor =
        Executors.newSingleThreadExecutor()

    // Accessed only by the executor thread.
    private var observer: CapturerObserver? = null

    fun attach(videoSource: VideoSource) {
        executor.execute {
            observer?.onCapturerStopped()

            observer = videoSource.capturerObserver

            observer?.onCapturerStarted(true)
        }
    }

    fun submitFrame(frame: VideoFrame) {
        // Keep the frame alive until the executor handles it.
        frame.retain()

        executor.execute {
            try {
                observer?.onFrameCaptured(frame)
            } finally {
                frame.release()
            }
        }
    }

    fun detach() {
        executor.execute {
            observer?.onCapturerStopped()
            observer = null
        }
    }
}