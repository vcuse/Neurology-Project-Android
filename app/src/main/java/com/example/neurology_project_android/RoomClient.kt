package com.example.neurology_project_android

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.neurology_project_android.sampledata.Device
import com.example.neurology_project_android.sampledata.RecvTransport
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.socket.client.Ack
import io.socket.client.Socket

import org.json.JSONArray
import org.json.JSONObject
import org.webrtc.MediaStreamTrack
import java.util.function.Consumer
import java.util.function.Function

class RoomClient constructor(room_id: String, name: String, socket: Socket, context: Context) {
    private lateinit var sendTransportIdAudio: String
    private lateinit var device: Device
    private lateinit var producers: Any
    private lateinit var consumerTransportId: String
    private var localMedia = null;
    private var remoteMedia = null;
    private lateinit var consumerTransport: RecvTransport
    private lateinit var socket: Socket
    private lateinit var sendTransportId: String

    @OptIn(UnstableApi::class)
    fun requestProducer(producerTransportId: String, kind: String, rtpParams: String) {
        Log.d("REQUESTPRODUCER: ", "IN REQUEST PRODUCER")
        val jsonPayload = JSONObject().apply {
            put("producerTransportId", producerTransportId)
            put("kind", kind)
            put("rtpParameters", JSONObject(rtpParams))
        }

        socket.emit("produce", jsonPayload, Ack { args ->

            // This block runs when the server executes 'callback(roomList)'

            if (args.isEmpty() || args[0] == null) {
                Log.e("SIGNALING CLIENT", "no producer recv")
                return@Ack
            }

            // Assuming the room list is the first argument in the callback's arguments array
            val responseData = args[0]

            if (responseData is JSONObject) {


                Log.d("REQUEST PRODUCER", "SUCCESS! producer Recd $responseData")
                // 1. Initialize a mutable list to hold the extracted room IDs
//                val gson = Gson()
//                val jsonObject:JsonObject = gson.fromJson(responseData.toString(), JsonObject::class.java)
//
//
////
////                Device.load(responseData)
//
//                Device.load(jsonObject)


                // TODO: Update ViewModel/Activity state here

            } else {
                Log.e(
                    "SIGNALING CLIENT",
                    "Received unexpected response type: ${responseData.javaClass.name}"
                )
            }
        })
    }

    @OptIn(UnstableApi::class)
    private fun requestConnectTransport(
        t: JSONObject,
        producerId: String
    ) {
        val jsonPayload = JSONObject().apply {
            put("transport_id", producerId)
            put("dtlsParameters", t)
        }
        socket.emit("connectTransport", jsonPayload)
        Log.d("ROOMCLIENT", "EMITTED CONNECT TRANSPORT" + t.toString())


    }

    @OptIn(UnstableApi::class)
    fun createWebRTCTransport() {
        val jsonPayload = JSONObject().apply {
            put("forceTcp", "false")
            put("rtpCapabilities", Device.rtpCapabilities)
        }
        socket.emit("createWebRtcTransport", jsonPayload, Ack { args ->

            // This block runs when the server executes 'callback(roomList)'

            if (args.isEmpty() || args[0] == null) {
                Log.e("SIGNALING CLIENT", "no router rtpCaps")
                return@Ack
            }

            // Assuming the room list is the first argument in the callback's arguments array
            val responseData = args[0]

            if (responseData is JSONObject) {


                Log.d("SIGNALING CLIENT", "SUCCESS! transport created: $responseData")
                // 1. Initialize a mutable list to hold the extracted room IDs


                val gson = Gson()
                val iceParameters: JsonObject = gson.fromJson(
                    responseData.get("iceParameters").toString(),
                    JsonObject::class.java
                )
                val iceCandidates: JSONArray = responseData.getJSONArray("iceCandidates")
                val iceCandidatesJson: JsonArray =
                    gson.fromJson(iceCandidates.toString(), JsonArray::class.java)
                val dtlsParameters: JsonObject = gson.fromJson(
                    responseData.get("dtlsParameters").toString(),
                    JsonObject::class.java
                )
//
//                Device.load(responseData)
                val producerTransport = Device.createSendTransport(
                    responseData.get("id").toString(), iceParameters,
                    iceCandidatesJson, dtlsParameters
                )

                sendTransportId = responseData.get("id").toString()
                Log.d("ROOM CLIENT", "CREATED PRODUCER TRANSPORT: " + producerTransport.toString())

                producerTransport.onProduce = object : Function<JSONObject, String> {
                    override fun apply(pData: JSONObject): String {
                        Log.d("ROOM CLIENT", "IN ONPRODUCE")
                        // The 'apply' method is the Single Abstract Method (SAM) that must be implemented
                        Log.d("ROOM CLIENT", "IN PRODUCER TRANSPORT ONPRODUCE" + pData.toString())
                        var rtpParams = pData.get("rtpParameters").toString()

                        var kind = pData.get("kind").toString()

                        requestProducer(sendTransportId, kind, rtpParams)
                        return "hi"
                    }
                }

                producerTransport.transport.onConnect =
                    Consumer<JSONObject> { dtlsParameters: JSONObject ->

                        Log.d("ROOM CLIENT", "ON CONNECT" + dtlsParameters.toString())

                        requestConnectTransport(dtlsParameters, sendTransportId)


                    }


                var localVideoSource = Device.createVideoSource()
                producerTransport.send(localVideoSource)
                var producerId =
                    producerTransport.onProduce.apply(JSONObject(producerTransport.produceData.toString()))
                Log.d("producerIDasdfsdaf", producerId.toString())


                // TODO: Update ViewModel/Activity state here

            } else {
                Log.e(
                    "SIGNALING CLIENT",
                    "Received unexpected response type: ${responseData.javaClass.name}"
                )
            }
        })

        val jsonPayloadAudio = JSONObject().apply {
            put("forceTcp", "false")
            put("rtpCapabilities", Device.rtpCapabilities)
        }
        socket.emit("createWebRtcTransport", jsonPayloadAudio, Ack { args ->

            // This block runs when the server executes 'callback(roomList)'

            if (args.isEmpty() || args[0] == null) {
                Log.e("SIGNALING CLIENT", "no router rtpCaps")
                return@Ack
            }

            // Assuming the room list is the first argument in the callback's arguments array
            val responseData = args[0]

            if (responseData is JSONObject) {


                Log.d("SIGNALING CLIENT", "SUCCESS! transport created: $responseData")
                // 1. Initialize a mutable list to hold the extracted room IDs


                val gson = Gson()
                val iceParameters: JsonObject = gson.fromJson(
                    responseData.get("iceParameters").toString(),
                    JsonObject::class.java
                )
                val iceCandidates: JSONArray = responseData.getJSONArray("iceCandidates")
                val iceCandidatesJson: JsonArray =
                    gson.fromJson(iceCandidates.toString(), JsonArray::class.java)
                val dtlsParameters: JsonObject = gson.fromJson(
                    responseData.get("dtlsParameters").toString(),
                    JsonObject::class.java
                )
//
//                Device.load(responseData)
                val producerTransport = Device.createSendTransport(
                    responseData.get("id").toString(), iceParameters,
                    iceCandidatesJson, dtlsParameters
                )

                sendTransportIdAudio = responseData.get("id").toString()
                Log.d("ROOM CLIENT", "CREATED PRODUCER TRANSPORT: " + producerTransport.toString())

                producerTransport.onProduce = object : Function<JSONObject, String> {
                    override fun apply(pData: JSONObject): String {
                        Log.d("ROOM CLIENT", "IN ONPRODUCE")
                        // The 'apply' method is the Single Abstract Method (SAM) that must be implemented
                        Log.d("ROOM CLIENT", "IN PRODUCER TRANSPORT ONPRODUCE" + pData.toString())
                        var rtpParams = pData.get("rtpParameters").toString()

                        var kind = pData.get("kind").toString()

                        requestProducer(sendTransportIdAudio, kind, rtpParams)
                        return "hi"
                    }
                }

                producerTransport.transport.onConnect =
                    Consumer<JSONObject> { dtlsParameters: JSONObject ->

                        Log.d("ROOM CLIENT", "ON CONNECT" + dtlsParameters.toString())

                        requestConnectTransport(dtlsParameters, sendTransportIdAudio)


                    }


                var localAudioSource = Device.createAudioSource()
                producerTransport.send(localAudioSource)
                var producerId =
                    producerTransport.onProduce.apply(JSONObject(producerTransport.produceData.toString()))
                Log.d("producerIDasdfsdaf", producerId.toString())


                // TODO: Update ViewModel/Activity state here

            } else {
                Log.e(
                    "SIGNALING CLIENT",
                    "Received unexpected response type: ${responseData.javaClass.name}"
                )
            }
        })


        var newJsonPayload = JSONObject().apply {
            put("forceTcp", "false")
            put("rtpCapabilities", Device.rtpCapabilities)
        }

        socket.emit("createWebRtcTransport", newJsonPayload, Ack { args ->

            // This block runs when the server executes 'callback(roomList)'

            if (args.isEmpty() || args[0] == null) {
                Log.e("SIGNALING CLIENT", "no router rtpCaps")
                return@Ack
            }

            // Assuming the room list is the first argument in the callback's arguments array
            val responseData = args[0]

            if (responseData is JSONObject) {


                Log.d("SIGNALING CLIENT", "SUCCESS! recv transport created: $responseData")
                // 1. Initialize a mutable list to hold the extracted room IDs


                val gson = Gson()
                this.consumerTransportId = responseData.get("id").toString()
                Log.d("SIGNALINGCLIENT", "CONSUMERTRANSPORTID IS " + consumerTransportId)
                this.consumerTransport = Device.createRecvTransport(
                    responseData.get("id").toString(),
                    gson.fromJson(
                        responseData.get("iceParameters").toString(),
                        JsonObject::class.java
                    ),
                    gson.fromJson(
                        responseData.get("iceCandidates").toString(),
                        JsonArray::class.java
                    ),
                    gson.fromJson(
                        responseData.get("dtlsParameters").toString(),
                        JsonObject::class.java
                    )
                )

                this.consumerTransport.onConnect =
                    Consumer<JSONObject> { dtlsParameters: JSONObject ->
                        var jsonArray = JSONArray()
                        jsonArray.put(dtlsParameters.get("fingerprints"))
                        var jsonObject = JSONObject().put("fingerPrints", jsonArray)

                        var jsonPayload = JSONObject().apply {
                            put("transport_id", consumerTransportId)
                            put("dtlsParameters", dtlsParameters)
                        }
                        Log.d("ROOM CLIENT", "ON CONNECT params $dtlsParameters")

                        socket.emit("connectTransport", jsonPayload)
                    }
                this.consumerTransport.onTrack =
                    Consumer<MediaStreamTrack> { track: MediaStreamTrack ->
                        Log.d("ROOM CLIENT", "ON TRACK")

                    }

//                this.consumerTransport.receive(consumerTransportId, "audio", Device.rtpCapabilities)

                // TODO: Update ViewModel/Activity state here

            } else {
                Log.e(
                    "SIGNALING CLIENT",
                    "Received unexpected response type: ${responseData.javaClass.name}"
                )
            }
        })


    }

    @OptIn(UnstableApi::class)
    fun getRouterRtpCapabilities() {
        val jsonPayload = JSONObject().apply { }
        socket.emit("getRouterRtpCapabilities", jsonPayload, Ack { args ->

            // This block runs when the server executes 'callback(roomList)'

            if (args.isEmpty() || args[0] == null) {
                Log.e("SIGNALING CLIENT", "no router rtpCaps")
                return@Ack
            }

            // Assuming the room list is the first argument in the callback's arguments array
            val responseData = args[0]

            if (responseData is JSONObject) {


                Log.d("SIGNALING CLIENT", "SUCCESS! routerRTCCapabilities Recv $responseData")
                // 1. Initialize a mutable list to hold the extracted room IDs
                val gson = Gson()
                val jsonObject =
                    gson.fromJson(responseData.toString(), JsonObject::class.java)


//
//                Device.load(responseData)

                Device.load(jsonObject)


                // TODO: Update ViewModel/Activity state here

            } else {
                Log.e(
                    "SIGNALING CLIENT",
                    "Received unexpected response type: ${responseData.javaClass.name}"
                )
            }
        })

    }


    @OptIn(UnstableApi::class)
    fun joinRoom(room_id: String, name: String) {

        val jsonPayload = JSONObject().apply {
            put("room_id", room_id)
            put("name", name)
        }
        socket.emit("join", jsonPayload, Ack { args ->

            // This block runs when the server executes 'callback(roomList)'

            if (args.isEmpty() || args[0] == null) {
                Log.e("SIGNALING CLIENT", "no room joined")
                return@Ack
            }

            // Assuming the room list is the first argument in the callback's arguments array
            val responseData = args[0]

            if (responseData is JSONObject) {


                Log.d("SIGNALING CLIENT", "SUCCESS! Room joined: $responseData")
                // 1. Initialize a mutable list to hold the extracted room IDs
                //this.producers = responseData.get("producers")

                // TODO: Update ViewModel/Activity state here

            } else {
                Log.e(
                    "SIGNALING CLIENT",
                    "Received unexpected response type: ${responseData.javaClass.name}"
                )
            }
        })
        getRouterRtpCapabilities()
        createWebRTCTransport()
    }

    @OptIn(UnstableApi::class)
    fun createRoom(room_id: String) {
        val emptyPayload = JSONObject()

        socket.emit("createRoom", { room_id }, Ack { args ->

            // This block runs when the server executes 'callback(roomList)'

            if (args.isEmpty() || args[0] == null) {
                Log.e("SIGNALING CLIENT", "no room created")
                return@Ack
            }

            // Assuming the room list is the first argument in the callback's arguments array
            val responseData = args[0]

            if (responseData is JSONArray) {


                Log.d("SIGNALING CLIENT", "SUCCESS! Room List received: $responseData")
                // 1. Initialize a mutable list to hold the extracted room IDs

                var roomList = mutableListOf<String>()

                // 2. Loop through the JSONArray
                for (i in 0 until responseData.length()) {
                    // 3. Safely extract each element as a String
                    val roomId = responseData.getString(i)
                    roomList.add(roomId)

                }


                // TODO: Update ViewModel/Activity state here

            } else {
                Log.e(
                    "SIGNALING CLIENT",
                    "Received unexpected response type: ${responseData.javaClass.name}"
                )
            }
        })
    }

    @OptIn(UnstableApi::class)
    fun consume(producerId: String) {
        val gson = Gson()

        val jsonString: String = gson.toJson(Device.rtpCapabilities)
        var payload = JSONObject().apply {
            put("producerId", producerId)
            put("rtpCapabilities", JSONObject(jsonString))
            put("consumerTransportId", consumerTransportId)
        }
        Log.d("ROOM CLIENT", "CONSUMED CALLING params were $payload")

        this.socket.emit("consume", payload , Ack { args ->
            val response = args[0]
            if (response is JSONObject) {
                val jsonString: JsonObject = gson.fromJson(response.get("rtpParameters").toString(),
                    JsonObject::class.java)

                Log.d("ROOM CLIENT", "RESPONSE WAS $response")
                this.consumerTransport.receive(response.get("id").toString(), response.get("kind").toString(), jsonString)

            }
        })
    }



    init {


        this.socket = socket
        Device.initialize(context)



        joinRoom(room_id, "thera")

    }


}