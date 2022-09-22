package com.tarantula.game;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.DataStore;
import com.icodesoftware.Session;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.lobby.ZoneItem;
import com.tarantula.platform.room.GameRoomRegistry;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ConfigurableZone extends RecoverableObject implements GameZone {

    private ZoneItem zoneItem;
    private RoomProxy roomProxy;

    public ConfigurableZone(ZoneItem zoneItem){
        this.zoneItem = zoneItem;
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
        return 0;
    }

    @Override
    public void capacity(int capacity) {

    }

    @Override
    public int maxJoinsPerRoom() {
        return 0;
    }

    @Override
    public void maxJoinsPerRoom(int maxJoinsPerRoom) {

    }

    @Override
    public int joinsOnStart() {
        return 0;
    }

    @Override
    public void joinsOnStart(int joinsOnStart) {

    }

    @Override
    public long roundDuration() {
        return 0;
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
        return null;
    }

    @Override
    public Arena arena(int level) {
        return new Arena(zoneItem.room());
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
}
