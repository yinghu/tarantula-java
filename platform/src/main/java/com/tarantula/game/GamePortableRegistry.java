package com.tarantula.game;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.AbstractRecoverableListener;
import com.tarantula.game.service.GameServiceIndex;
import com.tarantula.platform.event.PortableEventRegistry;

public class GamePortableRegistry  extends AbstractRecoverableListener {

    public static final int OID = 10;

    public static final int RATING_CID = PortableEventRegistry.RATING_CID;//110
    public static final int GAME_ZONE_CID = 2;
    public static final int STUB_CID = PortableEventRegistry.GAME_STUB_CID;//109
    public static final int ARENA_CID = 4;
    public static final int MAPPING_OBJECT_CID = 5;
    public static final int GAME_SERVICE_INDEX_CID = 6;
    public static final int ROOM_CID = PortableEventRegistry.ROOM_CID;//111

    @Override
    public int registryId() {
        return OID;
    }

    @Override
    public Recoverable create(int i) {
        Recoverable pt = null;
        switch (i){
            case RATING_CID:
                pt = new Rating();
                break;
            case GAME_ZONE_CID:
                pt = new DynamicZone();
                break;
            case STUB_CID:
                pt = new Stub();
                break;
            case ROOM_CID:
                pt = new Room();
                break;
            case ARENA_CID:
                pt = new Arena();
                break;
            case MAPPING_OBJECT_CID:
                pt = new MappingObject();
                break;
            case GAME_SERVICE_INDEX_CID:
                pt = new GameServiceIndex();
                break;
            default:
        }
        return pt;
    }
}
