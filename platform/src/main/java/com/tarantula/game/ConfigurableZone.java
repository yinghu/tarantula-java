package com.tarantula.game;

import com.google.gson.JsonObject;
import com.icodesoftware.ApplicationContext;
import com.icodesoftware.DataStore;
import com.icodesoftware.Session;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.lobby.ZoneItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class ConfigurableZone extends RecoverableObject implements GameZone {

    private ZoneItem zoneItem;
    private RoomProxy roomProxy;
    private List<Arena> arenaList;
    private ConcurrentHashMap<Integer,Arena> arenaIndex;

    private String configurationTypeId;
    private String configurationName;

    public String configurationTypeId() {
        return this.configurationTypeId;
    }

    public void configurationTypeId(String configurationTypeId) {
        this.configurationTypeId = configurationTypeId;
    }

    public String configurationName() {
        return this.configurationName;
    }

    public void configurationName(String configurationName) {
        this.configurationName = configurationName;
    }

    public ConfigurableZone(ZoneItem zoneItem){
        this.zoneItem = zoneItem;
        this.arenaList = new ArrayList<>();
        this.arenaIndex = new ConcurrentHashMap<>();
        this.zoneItem.arenaList().forEach(a->{
            Arena arena = new Arena(a);
            arenaIndex.put(a.level(),arena);
            arenaList.add(arena);
        });
    }

    @Override
    public String name(){
        return zoneItem.name();
    }



    @Override
    public String playMode() {
        return zoneItem.playMode();
    }


    @Override
    public String gameModule(){
        return zoneItem.gameModule();
    }
    @Override
    public int capacity() {
        return this.zoneItem.room().capacity();
    }


    @Override
    public int joinsOnStart() {
        return zoneItem.room().joinsOnStart();
    }


    @Override
    public long roundDuration() {
        return zoneItem.room().duration();
    }

    @Override
    public long roundOvertime() {
        return zoneItem.room().overtime();
    }

    @Override
    public boolean connected() {
        return false;
    }

    @Override
    public Stub join(Session session, Rating rating) {

        return roomProxy.join(session,rating);
    }

    @Override
    public boolean leave(Stub stub) {
        return roomProxy.leave(stub);
    }


    @Override
    public List<Arena> arenas() {
        return arenaList;
    }

    @Override
    public Arena arena(int level) {
        return arenaIndex.get(level);
    }

    @Override
    public void setup(ApplicationContext applicationContext, GameLobby gameLobby) {

    }

    @Override
    public DataStore dataStore() {
        return this.dataStore;
    }

    @Override
    public void roomProxy(RoomProxy roomProxy) {
        this.roomProxy = roomProxy;
    }

    @Override
    public void close() {

    }

    public String toString(){
        return "Zone ["+zoneItem.name()+"] Rank ["+zoneItem.rank()+"] Play Mode ["+zoneItem.playMode()+"]";
    }

    @Override
    public JsonObject toJson(){
        return this.zoneItem.toJson();
    }

    public String distributionKey(){
        return this.zoneItem.distributionKey();
    }
    public String oid(){
        return this.zoneItem.oid();
    }
    public String bucket(){
        return this.zoneItem.bucket();
    }


}
