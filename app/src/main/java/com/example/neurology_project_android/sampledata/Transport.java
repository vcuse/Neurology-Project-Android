package com.example.neurology_project_android.sampledata;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import one.dugon.mediasoup_android_sdk.sdp.RemoteSdp;
import one.dugon.mediasoup_android_sdk.sdp.Utils;


public class Transport implements PeerConnection.Observer{
    private static final String TAG = "Transport";

    public String id ;
    public RemoteSdp remoteSdp;

    public Consumer<JSONObject> onConnect;
    public Consumer<MediaStreamTrack> onTrack = null;

    @Nullable
    public PeerConnection pc;

    public boolean ready = false;
    public ExecutorService executor = Executors.newSingleThreadExecutor();


    public Transport(String id,
                     JsonObject iceParameters,
                     JsonArray iceCandidates,
                     JsonObject dtlsParameters){
        this.id = id;
        this.remoteSdp = new RemoteSdp(iceParameters, iceCandidates, dtlsParameters, null);
    }

    public void start(PeerConnection peerConnection) {
        pc = peerConnection;
    }

    public void SetupTransport(String localDtlsRole, JsonObject localSdpObject) throws JSONException {


        // Get our local DTLS parameters.
        Gson gson = new Gson();


        // 2. Serialize the Gson JsonObject into a raw JSON string
        JsonObject dtlsParameters = Utils.extractDtlsParameters(localSdpObject);


        dtlsParameters.addProperty("role","client");
        String toConvert = gson.toJson(dtlsParameters);
        Log.e("TRANSPORT:", "TO CONVERT STRING " + toConvert);
        onConnect.accept(new JSONObject(toConvert));
        // Set our DTLS role.
//        dtlsParameters["role"] = localDtlsRole;

        // Update the remote DTLS role in the SDP.
//        var remoteDtlsRole = localDtlsRole.equals("client") ? "server" : "client";
//        this->remoteSdp->UpdateDtlsRole(remoteDtlsRole);
        remoteSdp.updateDtlsRole("server");

        // May throw.
//        this->privateListener->OnConnect(dtlsParameters);
        ready = true;
    }


    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {

    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        Log.d(TAG,"iceState:"+iceConnectionState.name());
    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {

    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {

    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

    }

    @Override
    public void onAddStream(MediaStream mediaStream) {

    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {

    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {

    }

    @Override
    public void onRenegotiationNeeded() {

    }

    @Override
    public void onAddTrack(RtpReceiver receiver, MediaStream[] mediaStreams) {
//        if(onTrack != null){
//            Log.d(TAG,"onAddTrack " + receiver.track().kind());
//
//            if(Objects.equals(receiver.track().kind(), "video")) {
//                onTrack.accept(receiver.track().id());
//                tracks.put(receiver.track().id(),receiver.track());
//            }
//        }
        if(onTrack != null){
            onTrack.accept(receiver.track());
        }
    }

    @Override
    public void onRemoveTrack(RtpReceiver receiver) {
        Log.d(TAG,"onRemoveTrack");
    }
}