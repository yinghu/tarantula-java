package com.tarantula.platform.service.deployment;

import com.icodesoftware.OnView;
import com.icodesoftware.RecoverableFactory;
import com.tarantula.platform.OnViewTrack;
import com.tarantula.platform.service.cluster.PortableRegistry;

/**
 * Updated by yinghu on 8/26/19
 */
public class OnViewQuery implements RecoverableFactory<OnView> {

    private String lobbyId;

    public OnViewQuery(String lobbyId){
        this.lobbyId = lobbyId;
    }


    public OnView create() {
        OnViewTrack viewTrack = new OnViewTrack();
        return viewTrack;
    }

    public String distributionKey() {
        return lobbyId;
    }

    public  int registryId(){
        return PortableRegistry.ON_VIEW_OID;
    }

    public String label(){
        return OnView.LABEL;
    }
}
