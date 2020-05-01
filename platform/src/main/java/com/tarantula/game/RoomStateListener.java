package com.tarantula.game;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tarantula.Connection;
import com.tarantula.game.service.GameServiceProvider;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

public class RoomStateListener implements Connection.StateListener {

    private final Room room;
    private final GameServiceProvider gameServiceProvider;
    public RoomStateListener(Room room,GameServiceProvider gameServiceProvider){
        this.room = room;
        this.gameServiceProvider = gameServiceProvider;
    }

    @Override
    public void onUpdated(byte[] updated) {
        JsonParser jp = new JsonParser();
        InputStreamReader inr = new InputStreamReader(new ByteArrayInputStream(updated));
        JsonObject j = jp.parse(inr).getAsJsonObject();
        j.entrySet().forEach((e)->{
            if(e.getKey().equals("stats")){
                e.getValue().getAsJsonArray().forEach((st)->{
                    JsonObject jo = st.getAsJsonObject();

                });
            }
        });
    }

    @Override
    public void onEnded(byte[] ended) {
        room.end();
        JsonParser jp = new JsonParser();
        InputStreamReader inr = new InputStreamReader(new ByteArrayInputStream(ended));
        JsonObject j = jp.parse(inr).getAsJsonObject();
        j.entrySet().forEach((e)->{
            if(e.getKey().equals("stats")){
                System.out.println(e.getValue().toString());
            }
            if(e.getKey().equals("ratings")){
                System.out.println(e.getValue().toString());
            }
        });
    }
}
