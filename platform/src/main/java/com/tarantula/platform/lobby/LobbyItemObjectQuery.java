package com.tarantula.platform.lobby;

import com.icodesoftware.RecoverableFactory;
import com.tarantula.platform.item.ItemPortableRegistry;
import com.tarantula.platform.presence.PresencePortableRegistry;
import com.tarantula.platform.store.Shop;

public class LobbyItemObjectQuery implements RecoverableFactory<LobbyItem> {

    public String label;


    public LobbyItemObjectQuery(String query){
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
    public String distributionKey() {
        return null;
    }
}
