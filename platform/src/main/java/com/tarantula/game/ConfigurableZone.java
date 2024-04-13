package com.tarantula.game;

import com.google.gson.JsonObject;
import com.icodesoftware.ApplicationContext;
import com.icodesoftware.DataStore;
import com.icodesoftware.Session;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.lobby.LobbyItem;
import com.tarantula.platform.lobby.ZoneItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class ConfigurableZone extends RecoverableObject implements GameZone {

    private ZoneItem zoneItem;
    private RoomProxy roomProxy;
    private List<GameArena> arenaList;
    private ConcurrentHashMap<Integer, GameArena> arenaIndex;

    private String configurationTypeId;

    private String gameModule;

    public String configurationTypeId() {
        return this.configurationTypeId;
    }

    public String configurationName() {
        return this.zoneItem.configurationName();
    }
    public String configurationVersion() {
        return this.zoneItem.configurationVersion();
    }

    public ConfigurableZone(LobbyItem lobbyItem, ZoneItem zoneItem){
        this.gameModule = lobbyItem.gameModule();
        this.configurationTypeId = lobbyItem.configurationName()+"_"+zoneItem.configurationName()+"_"+zoneItem.configurationVersion();
        this.zoneItem = zoneItem;
        this.arenaList = new ArrayList<>();
        this.arenaIndex = new ConcurrentHashMap<>();
        this.zoneItem.arenaList().forEach(a->{
            GameArena arena = new GameArena(a);
            arenaIndex.put(a.level(),arena);
            arenaList.add(arena);
        });
    }

    public ConfigurableZone(ZoneItem zoneItem){
        //this.gameModule = lobbyItem.configurationName();
        this.zoneItem = zoneItem;
        this.arenaList = new ArrayList<>();
        this.arenaIndex = new ConcurrentHashMap<>();
        this.zoneItem.arenaList().forEach(a->{
            GameArena arena = new GameArena(a);
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
        return this.gameModule;
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
    public Stub join(Session session) {

        return roomProxy.join(session);
    }

    @Override
    public boolean leave(Stub stub) {

        return roomProxy.leave(stub);
    }


    @Override
    public List<GameArena> arenas() {
        return arenaList;
    }

    @Override
    public GameArena arena(int level) {
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

    public long distributionId(){
        return this.zoneItem.distributionId();
    }

}
