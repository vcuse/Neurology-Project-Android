package com.example.neurology_project_android.sampledata;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStreamTrack;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpTransceiver;
import org.webrtc.SessionDescription;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import one.dugon.mediasoup_android_sdk.sdp.Parser;

public class RecvTransport{
    private static final String TAG = "RecvTransport";

    public Consumer<JSONObject> onConnect;
    public Consumer<MediaStreamTrack> onTrack = null;

    Transport transport;
    public RecvTransport(String id, JsonObject iceParameters, JsonArray iceCandidates, JsonObject dtlsParameters) {
        transport = new Transport(id, iceParameters, iceCandidates, dtlsParameters);
        transport.onConnect = (JSONObject dtls)->{
            Log.d(TAG, "onConnect:");
            onConnect.accept(dtls);
        };

//        transport.onTrack = (String trackId)->{
//            onTrack.accept(trackId);
//        };
        transport.onTrack = (MediaStreamTrack track)->{
            onTrack.accept(track);
        };

    }

    public com.example.neurology_project_android.sampledata.Consumer receive(String id, String kind, JsonObject rtpParameters){

        Callable<com.example.neurology_project_android.sampledata.Consumer> task = () -> receiveInternal(id, kind, rtpParameters);

        Future<com.example.neurology_project_android.sampledata.Consumer> future = transport.executor.submit(task);

        try {
            return future.get();
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return null;
    }


    private com.example.neurology_project_android.sampledata.Consumer receiveInternal(String id, String kind, JsonObject rtpParameters){
        // TODO: 2025/3/2 maybe get mid from mapMidTransceiver
        // https://github.com/versatica/libmediasoupclient/blob/v3/src/Handler.cpp#L652C35-L652C52
        String localId = rtpParameters.get("mid").getAsString();
        String cname = rtpParameters.getAsJsonObject("rtcp").get("cname").getAsString();

        transport.remoteSdp.receive(localId, kind, rtpParameters,cname,id);
//
        String offer = transport.remoteSdp.getSdp();

        Log.i(TAG, offer);
//
        CompletableFuture<Void> futureSetRemote = new CompletableFuture<>();
//
        transport.pc.setRemoteDescription(new Device.SDPObserverForRtpCaps(){
            @Override
            public void onSetSuccess() {
                futureSetRemote.complete(null);
            }

            @Override
            public void onSetFailure(String error) {
                futureSetRemote.completeExceptionally(new Exception(error));
            }
        }, new SessionDescription(SessionDescription.Type.OFFER,offer));

        try {
            futureSetRemote.get();
            Log.i(TAG,"setRemoteDescription ok");

        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        MediaConstraints sdpMediaConstraints = new MediaConstraints();

        CompletableFuture<SessionDescription> futureDesc = new CompletableFuture<>();

        transport.pc.createAnswer(new Device.SDPObserverForRtpCaps() {
            @Override
            public void onCreateSuccess(SessionDescription desc) {
                Log.i(TAG,"createAnswer ok");

                futureDesc.complete(desc);
            }

            @Override
            public void onCreateFailure(String error) {
                futureDesc.completeExceptionally(new Exception(error));
            }
        }, sdpMediaConstraints);

        try {
            SessionDescription answer = futureDesc.get();
            Log.d(TAG, answer.description);
            Log.d(TAG, localId);


            JsonObject localSdpObj = Parser.parse(answer.description);

            // TODO: 2025/3/3
            // May need to modify codec parameters in the answer based on codec
            // parameters in the offer.

            //
            //            var media = localSdpObj.getAsJsonArray("media");
            //            JsonObject m_select;
            //            for (JsonElement m : media) {
            //                JsonObject m_n = m.getAsJsonObject();
            //                if (Objects.equals(m_n.get("mid").getAsString(), localId)){
            //                    m_select = m_n;
            //                    Log.i(TAG, "selected:"+localId);
            //                }
            //            }
            // https://github.com/versatica/libmediasoupclient/blob/v3/src/Handler.cpp#L679
            //Sdp::Utils::applyCodecParameters(*rtpParameters, answerMediaObject);

            if(!transport.ready){
                transport.SetupTransport("", localSdpObj);
            }

            Log.d(TAG, "ready!!");

            CompletableFuture<Void> futureDesc2 = new CompletableFuture<>();

            transport.pc.setLocalDescription(new Device.SDPObserverForRtpCaps() {
                @Override
                public void onSetSuccess() {
                    Log.i(TAG,"setLocalDescription ok");
                    futureDesc2.complete(null);
                }

                @Override
                public void onSetFailure(String error) {
                    Log.i(TAG,"setLocalDescription "+error);
                    futureDesc2.completeExceptionally(new Exception(error));
                }
            }, answer);

            futureDesc2.get();

            //https://bugs.chromium.org/p/webrtc/issues/detail?id=10788&q=getTransceivers()&colspec=ID%20Pri%20Stars%20M%20Component%20Status%20Owner%20Summary%20Modified
//            var transceivers = pc.getTransceivers();
//            RtpTransceiver rtpTransceiver = null;
//            for (var t : transceivers){
//                if(localId.equals(t.getMid())){
//                    rtpTransceiver = t;
//                }
//            }
//
//            return rtpTransceiver;
            com.example.neurology_project_android.sampledata.Consumer consumer = new com.example.neurology_project_android.sampledata.Consumer(id,localId);
            return consumer;
        } catch (ExecutionException | JSONException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}