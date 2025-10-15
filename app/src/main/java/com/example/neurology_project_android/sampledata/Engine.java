package com.example.neurology_project_android.sampledata;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.MediaStreamTrack;
import org.webrtc.RtpReceiver;
import org.webrtc.VideoTrack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import one.dugon.mediasoup_android_sdk.protoo.ProtooEventListener;
import one.dugon.mediasoup_android_sdk.protoo.ProtooSocket;


public class Engine {

    public enum PeerState {
        Join,
        Leave,
    }

    public enum MediaKind {
        Audio,
        Video,
    }

    public interface Listener {
        void onPeer(String peerId, PeerState state);
        void onMedia(String peerId, String consumerId , MediaKind kind, boolean available);
    }

    private static final String TAG = "Engine";

    private Listener listener;

    private ProtooSocket protoo;
    private SendTransport sendTransport;
    private RecvTransport recvTransport;

    private LocalAudioSource localAudioSource;
    private LocalVideoSource localVideoSource;

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public Consumer<String> onTrack = null;

    List<Peer> peerList;

    HashMap<String, com.example.neurology_project_android.sampledata.Consumer> consumerHashMap;
    HashMap<String, MediaStreamTrack> tracks;


    public Engine(Context context){
        protoo = new ProtooSocket();
        consumerHashMap = new HashMap<>();
        tracks = new HashMap<>();
        Device.initialize(context);
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    public void connect(String signalServer, String roomId, String peerId){

        protoo.setEventListener(new ProtooEventListener() {
            @Override
            public void onConnect() {
                Log.d(TAG, "onConnect");

                executor.execute(()->{
                    getRtpCaps();
                    createWebRTCTransport(false);
                    createWebRTCTransport(true);
                    join();
                });
            }

            @Override
            public void onDisconnect() {

            }

            @Override
            public void onRequest(JsonObject requestData) {
                Log.d(TAG,"onRequest:");
                String requestMethod = requestData.get("method").getAsString();
                if (Objects.equals(requestMethod, "newConsumer")) {

                    Log.d(TAG,"newConsumer");
                    int id = requestData.get("id").getAsInt();
                    JsonObject data = requestData.get("data").getAsJsonObject();
                    String peerId = data.get("peerId").getAsString();

                    String kind = data.get("kind").getAsString();
                    String consumerId = data.get("id").getAsString();
                    JsonObject rtpParameters= data.get("rtpParameters").getAsJsonObject();


                    Device.executor.execute(()->{
                        com.example.neurology_project_android.sampledata.Consumer consumer = recvTransport.receive(consumerId,kind,rtpParameters);
                        consumer.setPeerId(peerId);

                        MediaStreamTrack track = tracks.get(consumerId);

                        MediaKind k;
                        if (Objects.equals(track.kind(), "audio")){
                            k = Engine.MediaKind.Audio;
                        } else {
                            k = Engine.MediaKind.Video;
                        }

                        consumer.setTrack(track);
                        consumerHashMap.put(consumerId, consumer);

                        listener.onMedia(peerId, consumerId, k, true);
//                        if(kind.equals("video")){
//                            Log.d(TAG,"video !" + transceiver.getMid());
//                            var track = transceiver.getReceiver().track();
//                            var videotrack =  (VideoTrack)track;
//                            videotrack.setEnabled(true);
//
//                            myVideoTrack = videotrack;
//                            addRemoteVideoRenderer(videotrack);
//                        }

                        protoo.response(id);
                    });


                }
            }

            @Override
            public void onNotification(String method, JsonObject data) {
                if(Objects.equals(method, "newPeer")){
                    Gson gson = new Gson();
                    Peer peer = gson.fromJson(data, new TypeToken<Peer>(){}.getType());
                    peerList.add(peer);
                    listener.onPeer(peer.id, PeerState.Join);
                } else if(Objects.equals(method, "peerClosed")){
                    String peerId = data.get("peerId").getAsString();
                    peerList.removeIf(peer -> peer.id.equals(peerId));
                    listener.onPeer(peerId, PeerState.Leave);
                }
            }

            @Override
            public void onError() {

            }
        });

        protoo.connect(signalServer, Map.of("roomId", roomId, "peerId", peerId));
    }

    private void getRtpCaps(){
        JsonObject response = protoo.requestSync("getRouterRtpCapabilities");
        Device.load(response);
    }

    private void createWebRTCTransport(boolean isSender){
        JsonObject createData = new JsonObject();
        createData.addProperty("consuming", !isSender);
        createData.addProperty("forceTcp", false);
        createData.addProperty("producing", isSender);

        JsonObject response = protoo.requestSync("createWebRtcTransport", createData);

        String id = response.get("id").getAsString();
        JsonObject iceParameters = response.getAsJsonObject("iceParameters");
        JsonArray iceCandidates = response.getAsJsonArray("iceCandidates");
        JsonObject dtlsParameters = response.getAsJsonObject("dtlsParameters");

        if (isSender){
            sendTransport = Device.createSendTransport(id, iceParameters, iceCandidates, dtlsParameters);

//            sendTransport.onConnect = (JSONObject dtls)->{
//                Log.d(TAG,"dtls:"+dtls.toString());
//                JsonObject connectData = new JsonObject();
//                connectData.addProperty("transportId", id);
//                //connectData.add("dtlsParameters",dtls);
////                var r4 = socket.request("connectWebRtcTransport", connectData);
//                //JsonObject connectResponse = protoo.requestSync("connectWebRtcTransport", connectData);
//                Log.d(TAG,"dtls: ok");
//            };

//            sendTransport.onProduce = (JsonObject pData)->{
//                Log.d(TAG, "onProduce:");
//                pData.addProperty("transportId",id);
//                JsonObject produceResponse = protoo.requestSync("produce", pData);
//                return produceResponse.get("id").getAsString();
//            };

        }else{
            recvTransport = Device.createRecvTransport(id,iceParameters,iceCandidates,dtlsParameters);
            recvTransport.onTrack = (MediaStreamTrack track)->{
//                String kind = track.kind();
//                if(kind.equals("video")){
//                    var videotrack =  (VideoTrack)track;
//                    videotrack.setEnabled(true);
//
////                    myVideoTrack = videotrack;
//                    addRemoteVideoRenderer(videotrack);
//                }
                tracks.put(track.id(),track);
            };


            recvTransport.onConnect = (JSONObject dtls)->{
                Log.d(TAG,"recv dtls:"+dtls.toString());
                JsonObject connectData = new JsonObject();
                connectData.addProperty("transportId", id);
                //connectData.add("dtlsParameters",dtls);
//                var r4 = socket.request("connectWebRtcTransport", connectData);
                JsonObject connectResponse = protoo.requestSync("connectWebRtcTransport", connectData);
                Log.d(TAG,"dtls: ok");
            };

//            recvTransport.onTrack = (String trackId)-> {
//                if(onTrack != null){
//                    onTrack.accept(trackId);
//                }
//            };
//
//            recvTransport.onTrack = (RtpReceiver receiver) ->{
//                Log.d(TAG,"Fuck " + receiver.track().id());
//
//                receivers.put(receiver.track().id(), receiver);
//            };

        }

    }

    private void join(){
        JsonObject joinData = new JsonObject();
        JsonObject rtpCapabilitiesJson = Device.rtpCapabilities;
        JsonObject sctpCapabilitiesJson = Device.sctpCapabilities;

        JsonObject device = new JsonObject();
        device.addProperty("flag", "chrome");
        device.addProperty("name", "Chrome");
        device.addProperty("version", "129.0.0.0");

        joinData.add("device", device);
        joinData.add("rtpCapabilities", rtpCapabilitiesJson);
        joinData.add("sctpCapabilities", sctpCapabilitiesJson);
        joinData.addProperty("displayName", "gg");

        JsonObject response = protoo.requestSync("join", joinData);
        // TODO: 2025/3/16 handle response
        JsonArray peers =  response.get("peers").getAsJsonArray();
//        for (JsonElement p : peers) {
//            Log.d(TAG,"peer");
//
//        }
        // TODO: 2025/8/14 reuse gson
        Gson gson = new Gson();
        peerList = gson.fromJson(peers, new TypeToken<List<Peer>>(){}.getType());
        for(Peer p: peerList ){
            Log.d(TAG, "peer join " + p.id);
            listener.onPeer(p.id, PeerState.Join);
        }
    }

    public void enableCam() throws JSONException {
        localVideoSource = Device.createVideoSource();
//        sendTransport.abc();
        sendTransport.send(localVideoSource);
    }

    public void enableMic() throws JSONException {
        localAudioSource = Device.createAudioSource();
        sendTransport.send(localAudioSource);
    }

    public void previewCam(Player player){
        player.play(localVideoSource);
    }

    public void initView(Player player){
        Device.initView(player);
    }

    public void play(Player player, String consumerId){
        com.example.neurology_project_android.sampledata.Consumer consumer = consumerHashMap.get(consumerId);

        if(consumer.kind == MediaKind.Video){
            Log.d(TAG,"Play " + consumerId);

            VideoTrack videoTrack = (VideoTrack) consumer.track;
            player.play(videoTrack);
        }

    }
}