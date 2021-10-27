package com.tarantula.game;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.AbstractRecoverableListener;
import com.tarantula.platform.event.PortableEventRegistry;
import com.tarantula.platform.room.GameEntry;
import com.tarantula.platform.room.GameRoom;
import com.tarantula.platform.room.GameRoomRegistry;

public class GamePortableRegistry  extends AbstractRecoverableListener {

    public static final int OID = 10;

    public static final int GAME_LOBBY_CID = 1;
    public static final int GAME_ZONE_CID = 2;
    public static final int STUB_CID = 3;
    public static final int MAPPING_OBJECT_CID = 4;

    public static final int RATING_CID = PortableEventRegistry.RATING_CID;//110
    public static final int ARENA_CID = PortableEventRegistry.ARENA_CID;//112
    public static final int ROOM_CID = PortableEventRegistry.ROOM_CID;//111
    public static final int GAME_ROOM_REGISTRY_CID = PortableEventRegistry.GAME_ROOM_REGISTRY_CID;
    public static final int GAME_ENTRY_CID = PortableEventRegistry.GAME_ENTRY_CID;

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
            case GAME_LOBBY_CID:
                pt = new DynamicGameLobby();
                break;
            case GAME_ZONE_CID:
                pt = new DynamicZone();
                break;
            case STUB_CID:
                pt = new Stub();
                break;
            case ROOM_CID:
                pt = new GameRoom();
                break;
            case ARENA_CID:
                pt = new Arena();
                break;
            case MAPPING_OBJECT_CID:
                pt = new MappingObject();
                break;
            case GAME_ROOM_REGISTRY_CID:
                pt = new GameRoomRegistry();
                break;
            case GAME_ENTRY_CID:
                pt = new GameEntry();
                break;
            default:
        }
        return pt;
    }
}
