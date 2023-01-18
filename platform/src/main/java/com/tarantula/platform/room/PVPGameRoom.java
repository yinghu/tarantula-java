package com.tarantula.platform.room;

import com.hazelcast.nio.serialization.Portable;
import com.icodesoftware.Channel;
import com.tarantula.platform.event.PortableEventRegistry;

import java.util.HashMap;


public class PVPGameRoom extends GameRoomHeader implements Portable {

    public PVPGameRoom(int capacity){
       this.capacity = capacity;
       joinIndex = new HashMap<>(capacity);
    }
    public PVPGameRoom(){
        this(2);
    }

    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.PVP_ROOM_CID;
    }

    @Override
    public void channel(Channel channel){
        this.connection = channel.connection();
        this.channelId = channel.channelId();
        this.sessionId = channel.sessionId();
        this.serverKey = channel.serverKey();
        this.timeout = channel.connection().timeout();
    }
    public synchronized GameRoom join(String systemId){
        if(joinIndex.containsKey(systemId)) {
            return duplicate();
        };
        for(int i=0;i<capacity;i++){
            GameEntry e = entries[i];
            if(e!=null&&e.occupied) continue;
            if(e==null){
                e = new GameEntry(i);
                e.owner(this.distributionKey());
                this.dataStore.create(e);
                entries[i]=e;
            }
            e.systemId = systemId;
            e.occupied = true;
            e.seatIndex = i;
            this.dataStore.update(e);
            joinIndex.put(systemId,e);
            break;
        }
        return duplicate();
    }
    public synchronized void leave(String systemId){
        GameEntry rm = joinIndex.remove(systemId);
        if(rm!=null){
            rm.occupied = false;
            this.dataStore.update(rm);
        }
    }
    public synchronized GameRoom view(){
        PVPGameRoom _room = new PVPGameRoom();
        _room.entries = new GameEntry[joinIndex.size()];
        joinIndex.forEach((k,e)->_room.entries[e.seatIndex]=e);
        _room.capacity = _room.entries.length;
        _room.round = this.round;
        _room.bucket(this.bucket);
        _room.oid(this.oid);
        return _room;
    }

    private PVPGameRoom duplicate(){
        PVPGameRoom _room = new PVPGameRoom();
        if(channel!=null){
            _room.channelId = channel.channelId();
            _room.sessionId = channel.sessionId();
            _room.timeout = channel.timeout();
            _room.serverKey = channel.serverKey();
            _room.connection = channel.connection();
        }
        _room.entries = new GameEntry[joinIndex.size()];
        joinIndex.forEach((k,e)->_room.entries[e.seatIndex]=e);
        _room.capacity = _room.entries.length;
        _room.round = this.round;
        _room.bucket(this.bucket);
        _room.oid(this.oid);
        return _room;
    }
}