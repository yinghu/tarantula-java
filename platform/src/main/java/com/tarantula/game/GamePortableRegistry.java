package com.tarantula.game;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.AbstractRecoverableListener;
import com.tarantula.game.service.GameServiceIndex;

public class GamePortableRegistry  extends AbstractRecoverableListener {

    public static final int OID = 10;

    public static final int RATING_CID = 1;
    public static final int PVP_ZONE_CID = 2;
    public static final int STUB_CID = 3;
    public static final int ARENA_CID = 4;
    public static final int MAPPING_OBJECT_CID = 5;
    public static final int GAME_SERVICE_INDEX_CID = 6;
    public static final int PVE_ZONE_CID = 7;
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
            case PVP_ZONE_CID:
                pt = new PVPZone();
                break;
            case STUB_CID:
                pt = new Stub();
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
            case PVE_ZONE_CID:
                pt = new PVEZone();
                break;
            default:
        }
        return pt;
    }
}
