package com.tarantula.game;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.AbstractRecoverableListener;
import com.tarantula.platform.event.PortableEventRegistry;
import com.tarantula.platform.room.*;

public class GamePortableRegistry  extends AbstractRecoverableListener {

    public static final int OID = 10;



    public static final int STUB_CID = 3;
    public static final int MAPPING_OBJECT_CID = 4;
    public static final int GAME_UPDATE_OBJECT_CID = 5;

    public static final int RATING_CID = PortableEventRegistry.RATING_CID;//110
    public static final int PVE_ROOM_CID = PortableEventRegistry.PVE_ROOM_CID;
    public static final int PVP_ROOM_CID = PortableEventRegistry.PVP_ROOM_CID;
    public static final int TVE_ROOM_CID = PortableEventRegistry.TVE_ROOM_CID;
    public static final int TVT_ROOM_CID = PortableEventRegistry.TVT_ROOM_CID;

    public static final int GAME_ENTRY_CID = PortableEventRegistry.GAME_ENTRY_CID;

    @Override
    public int registryId() {
        return OID;
    }

    @Override
    public Recoverable create(int i) {
        Recoverable pt = null;
        switch (i){
            case MAPPING_OBJECT_CID:
                pt = new MappingObject();
                break;
            case GAME_UPDATE_OBJECT_CID:
                pt = new GameUpdateObject();
                break;
            case RATING_CID:
                pt = new Rating();
                break;
            case STUB_CID:
                pt = new Stub();
                break;
            case PVE_ROOM_CID:
                pt = new PVEGameRoom();
                break;
            case PVP_ROOM_CID:
                pt = new PVPGameRoom();
                break;
            case TVE_ROOM_CID:
                pt = new TVEGameRoom();
                break;
            case TVT_ROOM_CID:
                pt = new TVTGameRoom();
                break;
            case GAME_ENTRY_CID:
                pt = new GameEntry();
                break;
            default:
        }
        return pt;
    }
}
