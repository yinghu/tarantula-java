package com.tarantula.platform.lobby;

import com.tarantula.platform.item.Item;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class ZoneItem extends Item {

    public ZoneItem(){

    }
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.ZONE_ITEM_CID;
    }
}
