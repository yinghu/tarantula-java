package com.tarantula.platform.lobby;

import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class LobbyItem extends Application {

    public LobbyItem(){}
    public LobbyItem(ConfigurableObject configurableObject){
        super(configurableObject);
    }
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.LOBBY_ITEM_CID;
    }

    @Override
    public boolean configureAndValidate() {
        setup();
        return validated;
    }
}
