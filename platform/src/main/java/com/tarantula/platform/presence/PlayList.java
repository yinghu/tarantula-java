package com.tarantula.platform.presence;

import com.icodesoftware.util.FIFOBuffer;
import com.icodesoftware.util.RecoverableObject;

import java.util.ArrayList;
import java.util.List;


public class PlayList extends RecoverableObject {

    private FIFOBuffer<Long> playListIndex;
    private int size;

    public PlayList(){
    }
    public PlayList(int size){
        this.size = size;
        this.playListIndex = new FIFOBuffer<>(size,new Long[size]);
    }
    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }


    @Override
    public int getClassId() {
        return PresencePortableRegistry.PLAY_LIST_CID;
    }


    public boolean read(DataBuffer buffer) {
        size = buffer.readInt();
        this.playListIndex = new FIFOBuffer<>(size,new Long[size]);
        for(int i=0;i<size;i++){
            playListIndex.push(buffer.readLong());
        }
        return true;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeInt(size);
        Long[] list = new Long[size];
        playListIndex.list(list);
        for(int i=0;i<size;i++){
            buffer.writeLong(list[i]==null?0:list[i]);
        }
        return true;
    }
    public void onList(long systemId){
        playListIndex.push(systemId);
    }
    public List<Long> list(){
        return playListIndex.list(new ArrayList<>());
    }
}