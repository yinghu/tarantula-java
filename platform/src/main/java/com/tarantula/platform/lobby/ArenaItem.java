package com.tarantula.platform.lobby;

import com.tarantula.platform.item.Item;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class ArenaItem extends Item {

    public ArenaItem(){

    }
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.ARENA_ITEM_CID;
    }
}
