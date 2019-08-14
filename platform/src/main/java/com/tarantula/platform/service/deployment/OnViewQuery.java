package com.tarantula.platform.service.deployment;

import com.tarantula.OnView;
import com.tarantula.Recoverable;
import com.tarantula.RecoverableFactory;
import com.tarantula.platform.OnViewTrack;
import com.tarantula.platform.service.cluster.PortableRegistry;

/**
 * Updated by yinghu on 11/20
 * /2018.
 */
public class OnViewQuery implements RecoverableFactory<OnView> {

    private String lobbyId;

    public OnViewQuery(String lobbyId){
        this.lobbyId = lobbyId;
    }


    public OnView create() {
        OnViewTrack viewTrack = new OnViewTrack();
        //viewTrack.distributable(true);
        //viewTrack.index(lobbyId+ Recoverable.PATH_SEPARATOR+label());
        return viewTrack;
    }


    public String distributionKey() {
        return lobbyId;
    }


    public  int registryId(){
        return PortableRegistry.ON_VIEW_OID;
    }

    public String label(){
        return "LVT";
    }
    public boolean onEdge(){
        return true;
    }
}
