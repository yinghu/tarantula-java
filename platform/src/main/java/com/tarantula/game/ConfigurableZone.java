package com.tarantula.game;

import com.google.gson.JsonObject;
import com.icodesoftware.ApplicationContext;
import com.icodesoftware.DataStore;
import com.icodesoftware.Session;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.lobby.ZoneItem;
import com.tarantula.platform.room.GameRoomRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ConfigurableZone extends RecoverableObject implements GameZone {

    private ZoneItem zoneItem;
    private RoomProxy roomProxy;
    private List<Arena> arenaList;
    private ConcurrentHashMap<Integer,Arena> arenaIndex;

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
    public int levelMatch() {
        return 0;
    }

    @Override
    public void levelMatch(int levelMatch) {

    }

    @Override
    public String playMode() {
        return zoneItem.playMode();
    }

    @Override
    public int arenaLimit() {
        return 0;
    }

    @Override
    public void arenaLimit(int arenaLimit) {

    }

    @Override
    public int capacity() {
        return this.zoneItem.room().capacity();
    }

    @Override
    public void capacity(int capacity) {

    }

    @Override
    public int maxJoinsPerRoom() {
        return zoneItem.room().capacity();
    }

    @Override
    public void maxJoinsPerRoom(int maxJoinsPerRoom) {

    }

    @Override
    public int joinsOnStart() {
        return zoneItem.room().joinsOnStart();
    }

    @Override
    public void joinsOnStart(int joinsOnStart) {

    }

    @Override
    public long roundDuration() {
        return zoneItem.room().duration();
    }

    @Override
    public void roundDuration(long roundDuration) {

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
    public void leave(Stub stub) {
        roomProxy.leave(stub);
    }

    @Override
    public void update(Session session, Stub stub, byte[] payload){
        roomProxy.update(session,stub,payload);
    }

    @Override
    public void list(Session session,Stub stub){
        roomProxy.list(session,stub);
    }


    @Override
    public void addArena(Arena arena) {

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

    @Override
    public ConcurrentHashMap<String, GameRoomRegistry> roomRegistry() {
        return null;
    }


    @Override
    public ConcurrentLinkedDeque<GameRoomRegistry> roomRegistryQueue() {
        return null;
    }

    public String toString(){
        return zoneItem.name()+">>"+zoneItem.rank()+">>"+zoneItem.playMode();
    }
    @Override
    public JsonObject toJson(){
        return this.zoneItem.toJson();
    }

    public String distributionKey(){
        return this.zoneItem.distributionKey();
    }
}
