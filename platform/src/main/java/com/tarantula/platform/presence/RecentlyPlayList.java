package com.tarantula.platform.presence;

import com.icodesoftware.Configurable;
import com.icodesoftware.Descriptor;
import com.icodesoftware.Recoverable;
import com.icodesoftware.util.FIFOBuffer;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.GameCluster;


import java.util.Map;

public class RecentlyPlayList extends RecoverableObject {

    private FIFOBuffer<String> playListIndex;

    public RecentlyPlayList(){}
    public RecentlyPlayList(int size,Descriptor lobby){
        this.playListIndex = new FIFOBuffer<>(size,new String[size]);
    }
    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.ACHIEVEMENT_CID;
    }

    @Override
    public Map<String,Object> toMap(){
        //playListIndex.
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){

    }
    @Override
    public Key key(){
        return new AssociateKey(this.bucket,this.oid, GameCluster.RECENTLY_PLAY_LIST_INDEX);
    }

    public interface Listener extends Configurable.Listener {
        void onPlay(String systemId, Descriptor lobby);
    }
}