package com.example.neurology_project_android.sampledata;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Peer {

    public String id;
    public String displayName;

    private List<Consumer> consumers;

    Peer(String id, String displayName){
        consumers = new ArrayList<>();
    }

    void addConsumer(String id, JsonObject rtpParameters){
        String mid = rtpParameters.get("mid").getAsString();
        Consumer consumer = new Consumer(id, mid);
        consumers.add(consumer);
    }

    Consumer getConsumer(String consumerId){
        return consumers.stream()
                .filter(c -> Objects.equals(c.id, consumerId))
                .findFirst()
                .orElse(null); // 没找到返回null
    }
}