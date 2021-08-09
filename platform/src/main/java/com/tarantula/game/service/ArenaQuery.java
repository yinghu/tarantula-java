package com.tarantula.game.service;

import com.icodesoftware.RecoverableFactory;
import com.tarantula.game.Arena;
import com.tarantula.game.GamePortableRegistry;

public class ArenaQuery implements RecoverableFactory<Arena> {

    private String instanceId;

    public ArenaQuery(String instanceId){
        this.instanceId = instanceId;
    }

    public Arena create() {
        Arena ocx = new Arena();
        return ocx;
    }

    public String distributionKey() {
        return this.instanceId;
    }

    public  int registryId(){
        return GamePortableRegistry.ARENA_CID;
    }

    public String label(){
        return Arena.LABEL;
    }
}
