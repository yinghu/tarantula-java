package com.tarantula.game;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.AbstractRecoverableListener;
import com.tarantula.platform.event.PortableEventRegistry;
import com.tarantula.platform.room.*;

public class GamePortableRegistry<T extends Recoverable>  extends AbstractRecoverableListener {

    public static final int OID = 10;

    public static final int STUB_CID = 3;

    public static final int GAME_UPDATE_OBJECT_CID = 5;

    public static final int GAME_ENTRY_CID = 6;

    public static final int GAME_ROOM_CID = 7;

    public static final int RATING_CID = PortableEventRegistry.RATING_CID;//110


    public static GamePortableRegistry INS;

    public GamePortableRegistry(){
        INS = this;
    }
    @Override
    public int registryId() {
        return OID;
    }

    @Override
    public   T create(int i) {
        Recoverable pt = null;
        switch (i){
            case GAME_UPDATE_OBJECT_CID:
                pt = new GameUpdateObject();
                break;
            case RATING_CID:
                pt = new GameRating();
                break;
            case STUB_CID:
                pt = new Stub();
                break;
            case GAME_ENTRY_CID:
                pt = new GameEntry();
                break;
            case GAME_ROOM_CID:
                pt = new GameRoomHeader();
                break;
            default:
        }
        return (T)pt;
    }
}
