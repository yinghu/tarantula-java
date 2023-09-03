package com.tarantula.platform.lobby;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class LobbyItemObjectQuery implements RecoverableFactory<LobbyItem> {

    public String label;
    private Recoverable.Key key;

    public LobbyItemObjectQuery(Recoverable.Key key,String query){
        this.key = key;
        this.label = query;
    }

    @Override
    public LobbyItem create() {
        return new LobbyItem();
    }

    @Override
    public int registryId() {
        return PresencePortableRegistry.LOBBY_ITEM_CID;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public Recoverable.Key key() {
        return key;
    }
}
