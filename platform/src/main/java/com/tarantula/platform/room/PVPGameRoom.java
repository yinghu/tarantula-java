package com.tarantula.platform.room;

import com.hazelcast.nio.serialization.Portable;
import com.tarantula.game.service.GameEntryQuery;
import com.tarantula.platform.event.PortableEventRegistry;

import java.util.HashMap;


public class PVPGameRoom extends GameRoomHeader implements Portable {

    public PVPGameRoom(int capacity){
       this.capacity = capacity;
       joinIndex = new HashMap<>(capacity);
    }
    public PVPGameRoom(){
        this(12);
    }

    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.PVP_ROOM_CID;
    }

    public void load(){
        entries = new GameEntry[capacity];
        dataStore.list(new GameEntryQuery(this.distributionKey()),(ge)->{
            entries[ge.seatIndex]=ge;
            if(ge.occupied) joinIndex.put(ge.systemId,ge);
            return true;
        });
    }

    public synchronized GameRoom join(String systemId){
        if(joinIndex.containsKey(systemId)) return duplicate();
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
            this.dataStore.update(e);
            joinIndex.put(systemId,e);
            break;
        }
        return duplicate();
    }
    public synchronized boolean leave(String systemId){
        GameEntry rm = joinIndex.remove(systemId);
        if(rm!=null){
            rm.occupied = false;
            this.dataStore.update(rm);
        }
        return joinIndex.isEmpty();
    }
    public synchronized GameRoom view(){
        return this.duplicate();
    }
    public synchronized String[] joined(){
        if(joinIndex.isEmpty()) return new String[0];
        String[] joined = new String[joinIndex.size()];
        int[] i={0};
        joinIndex.forEach((k,v)->{
            joined[i[0]]=v.systemId;
            i[0]++;
        });
        return joined;
    }
    private PVPGameRoom duplicate(){
        PVPGameRoom _room = new PVPGameRoom();
        _room.entries = new GameEntry[joinIndex.size()];
        joinIndex.forEach((k,e)->_room.entries[e.seatIndex]=e);
        _room.capacity = _room.entries.length;
        _room.round = this.round;
        _room.bucket(this.bucket);
        _room.oid(this.oid);
        return this;
    }
}