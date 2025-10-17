package com.example.neurology_project_android.sampledata;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.RtpParameters;
import org.webrtc.RtpTransceiver;
import org.webrtc.SessionDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;

import one.dugon.mediasoup_android_sdk.sdp.Parser;
import one.dugon.mediasoup_android_sdk.sdp.RemoteSdp;
import one.dugon.mediasoup_android_sdk.sdp.Utils;


public class SendTransport {

    private static final String TAG = "SendTransport";

    public Function<JSONObject,String> onProduce;

    public JsonObject sendingRtpParametersByKind;
    public JsonObject sendingRemoteRtpParametersByKind;

    public Consumer<JSONObject> onConnect;

    public Transport transport;
    public JsonObject produceData;

    public SendTransport(String id,
                         JsonObject iceParameters,
                         JsonArray iceCandidates,
                         JsonObject dtlsParameters,
                         JsonObject sendingRtpParametersByKind,
                         JsonObject sendingRemoteRtpParametersByKind) {
        transport = new Transport(id,iceParameters,iceCandidates,dtlsParameters);
        transport.onConnect = (JSONObject dtls)->{
            Log.d(TAG, "onConnect:");
            //Log.d(TAG, "dtls is" + dtls.toString());
            //onConnect.accept(dtls);
        };

        this.sendingRtpParametersByKind = sendingRtpParametersByKind.deepCopy();
        this.sendingRemoteRtpParametersByKind = sendingRemoteRtpParametersByKind.deepCopy();
    }

    public void start(PeerConnection peerConnection) {
        transport.pc = peerConnection;
    }



    //
    public void send(LocalSource source) throws JSONException {

        MediaStreamTrack track = source.getTrack();
        List<RtpParameters.Encoding> encodings = new ArrayList<>();

        JsonObject sendingRtpParameters = sendingRtpParametersByKind.getAsJsonObject(track.kind()).deepCopy();
        JsonObject sendingRemoteRtpParameters = sendingRemoteRtpParametersByKind.getAsJsonObject(track.kind()).deepCopy();
//        reduceCodecs
        RemoteSdp.MediaSectionIdx mediaSectionIdx = transport.remoteSdp.getNextMediaSectionIdx();
        RtpTransceiver transceiver = transport.pc.addTransceiver(track);

        MediaConstraints sdpMediaConstraints = new MediaConstraints();

        CompletableFuture<SessionDescription> futureDesc = new CompletableFuture<>();

        transport.pc.createOffer(new Device.SDPObserverForRtpCaps() {
            @Override
            public void onCreateSuccess(SessionDescription desc) {
                futureDesc.complete(desc);
            }

            @Override
            public void onCreateFailure(String error) {
                futureDesc.completeExceptionally(new Exception(error));
            }
        }, sdpMediaConstraints);

        try {
            SessionDescription sdp = futureDesc.get();
            Log.d(TAG, sdp.description);

            CompletableFuture<Void> futureDesc2 = new CompletableFuture<>();

            transport.pc.setLocalDescription(new Device.SDPObserverForRtpCaps() {
                @Override
                public void onSetSuccess() {
                    futureDesc2.complete(null);
                }

                @Override
                public void onSetFailure(String error) {
                    futureDesc2.completeExceptionally(new Exception(error));
                }
            }, sdp);

            futureDesc2.get();
            String localId = transceiver.getMid();
            Log.d(TAG, "localId:" + localId);

            sendingRtpParameters.addProperty("mid", localId);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        SessionDescription localSdp = transport.pc.getLocalDescription();
        JsonObject localSdpObj = Parser.parse(localSdp.description);

        if(!transport.ready){
            transport.SetupTransport("", localSdpObj);
        }

//        var localSdpStr = Writer.write(localSdpObj);
//        Log.d(TAG,localSdpStr);
//        Log.d(TAG,localSdpObj.get("media").getAsJsonArray().get(0).getAsJsonObject().get("ssrcs").toString());

        JsonObject offerMediaObject = localSdpObj.getAsJsonArray("media").get(mediaSectionIdx.idx).getAsJsonObject();

        sendingRtpParameters.getAsJsonObject("rtcp").addProperty("cname", Utils.getCname(offerMediaObject));

        sendingRtpParameters.add("encodings", Utils.getRtpEncodings(offerMediaObject));

        Log.d(TAG,"codec:"+sendingRemoteRtpParameters.getAsJsonArray("codecs").toString());
        // TODO: 2024/10/11 fix mid

        transport.remoteSdp.send(offerMediaObject, "", sendingRtpParameters, sendingRemoteRtpParameters, null);
        Log.d(TAG, sendingRemoteRtpParameters.toString());
        String remoteSdpStr = transport.remoteSdp.getSdp();


        CompletableFuture<Void> futureSetRemote = new CompletableFuture<>();

        transport.pc.setRemoteDescription(new Device.SDPObserverForRtpCaps(){
            @Override
            public void onSetSuccess() {
                futureSetRemote.complete(null);
            }

            @Override
            public void onSetFailure(String error) {
                futureSetRemote.completeExceptionally(new Exception(error));
            }
        },new SessionDescription(SessionDescription.Type.ANSWER,remoteSdpStr));

        try {
            futureSetRemote.get();

        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        this.produceData = new JsonObject();
        produceData.addProperty("kind",track.kind());
        produceData.add("rtpParameters",sendingRtpParameters);
        //String producerId = onProduce.apply(produceData);
        //Log.d(TAG,"pid:"+producerId);
    }


}