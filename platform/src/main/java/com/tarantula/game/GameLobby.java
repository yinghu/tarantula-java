package com.tarantula.game;

import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.IndexSet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameLobby extends IndexSet implements Configurable {

    private JsonObject payload;
    private ConcurrentHashMap<String,GameZone> gameZones;

    public GameLobby(){
        super("gameLobby");
        payload = new JsonObject();
        gameZones = new ConcurrentHashMap<>();
    }
    public void addGameZone(GameZone gameZone){
        gameZones.put(gameZone.distributionKey(),gameZone);
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("payload",payload.toString());
        return super.toMap();
    }

    @Override
    public void fromMap(Map<String,Object> properties){
        payload = JsonUtil.parse((String)properties.remove("payload"));
        super.fromMap(properties);
    }

    @Override
    public int getFactoryId() {
        return GamePortableRegistry.OID;
    }

    @Override
    public int getClassId() { return GamePortableRegistry.GAME_LOBBY_CID; }

    @Override
    public boolean configureAndValidate(byte[] data){
        payload = JsonUtil.parse(data);
        return true;
    }
}