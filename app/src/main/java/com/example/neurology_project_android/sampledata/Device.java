package com.example.neurology_project_android.sampledata;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.CandidatePairChangeEvent;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.IceCandidateErrorEvent;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SoftwareVideoDecoderFactory;
import org.webrtc.SoftwareVideoEncoderFactory;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.audio.AudioDeviceModule;
import org.webrtc.audio.JavaAudioDeviceModule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import one.dugon.mediasoup_android_sdk.sdp.Parser;
import one.dugon.mediasoup_android_sdk.sdp.Utils;


public class Device {

    private static final String TAG = "Dugon";

    public static final String AUDIO_TRACK_ID = "ARDAMSa0";
    public static final String VIDEO_TRACK_ID = "ARDAMSv0";

    public static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static Context appContext;

    private static EglBase rootEglBase;

    private static PeerConnectionFactory factory;

    public static void initialize(Context context) {
        appContext = context;

        rootEglBase = EglBase.create();

        // TODO: 2024/9/30 log level
        executor.execute(() -> {

//            Log.d(TAG, "Initialize WebRTC. Field trials: " + fieldTrials);
            PeerConnectionFactory.initialize(
                    PeerConnectionFactory.InitializationOptions.builder(appContext)
//                            .setFieldTrials(fieldTrials)
                            .setEnableInternalTracer(true)
                            .createInitializationOptions());


            // PeerConnectionFactory
            final AudioDeviceModule adm = createJavaAudioDevice();

            // Create peer connection factory.
            PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();

//        final boolean enableH264HighProfile =
//                VIDEO_CODEC_H264_HIGH.equals(peerConnectionParameters.videoCodec);
            final VideoEncoderFactory encoderFactory;
            final VideoDecoderFactory decoderFactory;


//        encoderFactory = new DefaultVideoEncoderFactory(
//                rootEglBase.getEglBaseContext(), true /* enableIntelVp8Encoder */, enableH264HighProfile);
//        decoderFactory = new DefaultVideoDecoderFactory(rootEglBase.getEglBaseContext());

            encoderFactory = new SoftwareVideoEncoderFactory();
            decoderFactory = new SoftwareVideoDecoderFactory();

            // Disable encryption for loopback calls.
//        if (peerConnectionParameters.loopback) {
//            options.disableEncryption = true;
//        }

            factory = PeerConnectionFactory.builder()
                    .setOptions(options)
                    .setAudioDeviceModule(adm)
                    .setVideoEncoderFactory(encoderFactory)
                    .setVideoDecoderFactory(decoderFactory)
                    .createPeerConnectionFactory();
            Log.d(TAG, "Peer connection factory created.");
            adm.release();
        });
    }

    public static JsonObject rtpCapabilities;

    public static JsonObject sctpCapabilities;

    public static JsonObject extendedRtpCapabilities;

    public static JsonObject getRtpCapabilities() {
        CompletableFuture<SessionDescription> futureDesc = new CompletableFuture<>();

        Callable<Integer> task = () -> {
            Log.d(TAG, "getRtpCapabilities");

            List<PeerConnection.IceServer> iceServers = new ArrayList<>();

            PeerConnection.RTCConfiguration rtcConfig =
                    new PeerConnection.RTCConfiguration(iceServers);
            // TCP candidates are only useful when connecting to a server that supports
            // ICE-TCP.
            rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
            rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
            rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
            rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
            // Use ECDSA encryption.
            rtcConfig.keyType = PeerConnection.KeyType.ECDSA;
            rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;

            assert factory != null;
            PCObserverForRtpCaps pcObserver = new PCObserverForRtpCaps();
            PeerConnection peerConnection = factory.createPeerConnection(rtcConfig, pcObserver);

            MediaConstraints sdpMediaConstraints = new MediaConstraints();

            sdpMediaConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
            sdpMediaConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));

            SDPObserverForRtpCaps sdpObserver = new SDPObserverForRtpCaps() {
                @Override
                public void onCreateSuccess(SessionDescription desc) {
//                super.onCreateSuccess(desc);
                    Log.d("w", "onCreateSuccess");
                    futureDesc.complete(desc);

                }

                @Override
                public void onCreateFailure(String error) {
//                super.onCreateFailure(error);
                    futureDesc.completeExceptionally(new Exception(error));

                }

            };
            assert peerConnection != null;
            peerConnection.createOffer(sdpObserver, sdpMediaConstraints);

            return 1;
        };

        executor.submit(task);

        try {
            SessionDescription sdp = futureDesc.get();

            JsonObject sdpSession = Parser.parse(sdp.description);
//            var sdpStr = Writer.write(sdpSession);
            JsonObject rtpCapabilities = Utils.extractRtpCapabilities(sdpSession);
            Log.d("W", rtpCapabilities.toString());
//            Log.d("W",sdp.description);
            return rtpCapabilities;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static LocalVideoSource createVideoSource() {

        Callable<LocalVideoSource> task = () -> {
            boolean isScreencast = false;

            VideoSource videoSource = factory.createVideoSource(isScreencast);

            VideoTrack localVideoTrack = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
            localVideoTrack.setEnabled(true);

            return new LocalVideoSource(appContext, rootEglBase, videoSource, localVideoTrack);
        };

        Future<LocalVideoSource> future = executor.submit(task);

        try {
            return future.get();
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return null;
    }

    public  static LocalAudioSource createAudioSource() {
        Callable<LocalAudioSource> task = () -> {

            MediaConstraints audioConstraints = new MediaConstraints();;

            AudioSource audioSource = factory.createAudioSource(audioConstraints);
            AudioTrack localAudioTrack = factory.createAudioTrack(AUDIO_TRACK_ID, audioSource);

            localAudioTrack.setEnabled(true);

            return new LocalAudioSource(audioSource, localAudioTrack);
        };

        Future<LocalAudioSource> future = executor.submit(task);

        try {
            return future.get();
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return null;
    }

    static AudioDeviceModule createJavaAudioDevice() {
        // Enable/disable OpenSL ES playback.
//        if (!peerConnectionParameters.useOpenSLES) {
//            Log.w(TAG, "External OpenSLES ADM not implemented yet.");
//            // TODO(magjed): Add support for external OpenSLES ADM.
//        }

        // Set audio record error callbacks.
        JavaAudioDeviceModule.AudioRecordErrorCallback audioRecordErrorCallback = new JavaAudioDeviceModule.AudioRecordErrorCallback() {
            @Override
            public void onWebRtcAudioRecordInitError(String errorMessage) {
                Log.e(TAG, "onWebRtcAudioRecordInitError: " + errorMessage);
//                reportError(errorMessage);
            }

            @Override
            public void onWebRtcAudioRecordStartError(
                    JavaAudioDeviceModule.AudioRecordStartErrorCode errorCode, String errorMessage) {
                Log.e(TAG, "onWebRtcAudioRecordStartError: " + errorCode + ". " + errorMessage);
//                reportError(errorMessage);
            }

            @Override
            public void onWebRtcAudioRecordError(String errorMessage) {
                Log.e(TAG, "onWebRtcAudioRecordError: " + errorMessage);
//                reportError(errorMessage);
            }
        };

        JavaAudioDeviceModule.AudioTrackErrorCallback audioTrackErrorCallback = new JavaAudioDeviceModule.AudioTrackErrorCallback() {
            @Override
            public void onWebRtcAudioTrackInitError(String errorMessage) {
                Log.e(TAG, "onWebRtcAudioTrackInitError: " + errorMessage);
//                reportError(errorMessage);
            }

            @Override
            public void onWebRtcAudioTrackStartError(
                    JavaAudioDeviceModule.AudioTrackStartErrorCode errorCode, String errorMessage) {
                Log.e(TAG, "onWebRtcAudioTrackStartError: " + errorCode + ". " + errorMessage);
//                reportError(errorMessage);
            }

            @Override
            public void onWebRtcAudioTrackError(String errorMessage) {
                Log.e(TAG, "onWebRtcAudioTrackError: " + errorMessage);
//                reportError(errorMessage);
            }
        };

        // Set audio record state callbacks.
        JavaAudioDeviceModule.AudioRecordStateCallback audioRecordStateCallback = new JavaAudioDeviceModule.AudioRecordStateCallback() {
            @Override
            public void onWebRtcAudioRecordStart() {
                Log.i(TAG, "Audio recording starts");
            }

            @Override
            public void onWebRtcAudioRecordStop() {
                Log.i(TAG, "Audio recording stops");
            }
        };

        // Set audio track state callbacks.
        JavaAudioDeviceModule.AudioTrackStateCallback audioTrackStateCallback = new JavaAudioDeviceModule.AudioTrackStateCallback() {
            @Override
            public void onWebRtcAudioTrackStart() {
                Log.i(TAG, "Audio playout starts");
            }

            @Override
            public void onWebRtcAudioTrackStop() {
                Log.i(TAG, "Audio playout stops");
            }
        };

        return JavaAudioDeviceModule.builder(appContext)
//                .setSamplesReadyCallback(saveRecordedAudioToFile)
//                .setUseHardwareAcousticEchoCanceler(!peerConnectionParameters.disableBuiltInAEC)
//                .setUseHardwareNoiseSuppressor(!peerConnectionParameters.disableBuiltInNS)
                .setAudioRecordErrorCallback(audioRecordErrorCallback)
                .setAudioTrackErrorCallback(audioTrackErrorCallback)
                .setAudioRecordStateCallback(audioRecordStateCallback)
                .setAudioTrackStateCallback(audioTrackStateCallback)
                .createAudioDeviceModule();
    }

    public static void initView(Player player) {
        player.initEgl(rootEglBase.getEglBaseContext());
    }


    static class PCObserverForRtpCaps implements PeerConnection.Observer {
        @Override
        public void onIceCandidate(final IceCandidate candidate) {
        }

        @Override
        public void onIceCandidateError(final IceCandidateErrorEvent event) {
        }

        @Override
        public void onIceCandidatesRemoved(final IceCandidate[] candidates) {
        }

        @Override
        public void onSignalingChange(PeerConnection.SignalingState newState) {
        }

        @Override
        public void onIceConnectionChange(final PeerConnection.IceConnectionState newState) {
        }

        @Override
        public void onConnectionChange(final PeerConnection.PeerConnectionState newState) {
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState newState) {
        }

        @Override
        public void onIceConnectionReceivingChange(boolean receiving) {
        }

        @Override
        public void onSelectedCandidatePairChanged(CandidatePairChangeEvent event) {
        }

        @Override
        public void onAddStream(final MediaStream stream) {
        }

        @Override
        public void onRemoveStream(final MediaStream stream) {
        }

        @Override
        public void onDataChannel(final DataChannel dc) {
        }

        @Override
        public void onRenegotiationNeeded() {
        }

        @Override
        public void onAddTrack(final RtpReceiver receiver, final MediaStream[] mediaStreams) {
        }

        @Override
        public void onRemoveTrack(final RtpReceiver receiver) {
        }
    }

    static class SDPObserverForRtpCaps implements SdpObserver {
        @Override
        public void onCreateSuccess(final SessionDescription desc) {
        }

        @Override
        public void onCreateFailure(final String error) {
        }

        @Override
        public void onSetSuccess() {
        }

        @Override
        public void onSetFailure(final String error) {
        }
    }

    // for mediasoup
    public static void load(JsonObject routerRtpCapabilities) {
        Log.d(TAG, "getRtpCapabilities 1");

        JsonObject numStreams = new JsonObject();
        numStreams.addProperty("OS", "1024");
        numStreams.addProperty("MIS", "1024");

        sctpCapabilities = new JsonObject();
        sctpCapabilities.add("numStreams", numStreams);

        Log.d(TAG, "getRtpCapabilities 2");

        JsonObject local = getRtpCapabilities();
        Log.d(TAG, "getRtpCapabilities ok");
        extendedRtpCapabilities = Utils.getExtendedRtpCapabilities(local, routerRtpCapabilities);
        Log.d(TAG, extendedRtpCapabilities.toString());
        rtpCapabilities = Utils.getRecvRtpCapabilities(extendedRtpCapabilities);
    }

    public static SendTransport createSendTransport(
            String id,
            JsonObject iceParameters,
            JsonArray iceCandidates,
            JsonObject dtlsParameters
    ) {
        JsonObject audioSendingRtpParameters = Utils.getSendingRtpParameters("audio", extendedRtpCapabilities);
        JsonObject videoSendingRtpParameters = Utils.getSendingRtpParameters("video", extendedRtpCapabilities);
        JsonObject sendingRtpParametersByKind = new JsonObject();
        sendingRtpParametersByKind.add("audio", audioSendingRtpParameters);
        sendingRtpParametersByKind.add("video", videoSendingRtpParameters);


        JsonObject audioSendingRemoteRtpParameters = Utils.getSendingRemoteRtpParameters("audio", extendedRtpCapabilities);
        JsonObject videoSendingRemoteRtpParameters = Utils.getSendingRemoteRtpParameters("video", extendedRtpCapabilities);
        JsonObject sendingRemoteRtpParametersByKind = new JsonObject();
        sendingRemoteRtpParametersByKind.add("audio", audioSendingRemoteRtpParameters);
        sendingRemoteRtpParametersByKind.add("video", videoSendingRemoteRtpParameters);

        SendTransport t = new SendTransport(id, iceParameters, iceCandidates, dtlsParameters, sendingRtpParametersByKind, sendingRemoteRtpParametersByKind);
        //
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();

        PeerConnection.RTCConfiguration rtcConfig =
                new PeerConnection.RTCConfiguration(iceServers);

        PeerConnection peerConnection = factory.createPeerConnection(rtcConfig, t.transport);
        t.start(peerConnection);
        return t;
    }

    public static RecvTransport createRecvTransport(
            String id,
            JsonObject iceParameters,
            JsonArray iceCandidates,
            JsonObject dtlsParameters
    ) {
        RecvTransport t = new RecvTransport(id,iceParameters,iceCandidates,dtlsParameters);
        //
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();

        PeerConnection.RTCConfiguration rtcConfig =
                new PeerConnection.RTCConfiguration(iceServers);

        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;

        PeerConnection peerConnection = factory.createPeerConnection(rtcConfig, t.transport);
        t.transport.start(peerConnection);
        return t;
    }
}