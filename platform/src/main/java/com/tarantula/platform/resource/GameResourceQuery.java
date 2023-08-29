package com.tarantula.platform.resource;

import com.icodesoftware.RecoverableFactory;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class GameResourceQuery implements RecoverableFactory<GameResource> {

    public String label;


    public GameResourceQuery(String query){
        this.label = query;
    }

    @Override
    public GameResource create() {
        return new GameResource();
    }

    @Override
    public int registryId() {
        return PresencePortableRegistry.GAME_RESOURCE_CID;
    }

    @Override
    public String label() {
        return label;
    }

}
