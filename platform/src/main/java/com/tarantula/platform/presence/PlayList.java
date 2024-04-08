package com.tarantula.platform.presence;

import com.icodesoftware.util.FIFOBuffer;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.GameCluster;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayList extends RecoverableObject {

    private final static String PLAY_LIST_INDEX ="playlist";

    public FIFOBuffer<Long> playListIndex;

    public PlayList(){}
    public PlayList(int size){
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

    @Override
    public Map<String,Object> toMap(){
        List<Long> list = this.playListIndex.list(new ArrayList<>());
        ///list.forEach(a-> this.properties.put(a,"1"));
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        //properties.forEach((k,v)->this.playListIndex.push(k));
    }
    @Override
    public Key key(){
        return new AssociateKey(this.distributionId, PLAY_LIST_INDEX);
    }


}