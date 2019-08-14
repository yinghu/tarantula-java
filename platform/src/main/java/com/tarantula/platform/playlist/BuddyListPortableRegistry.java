package com.tarantula.platform.playlist;

import com.hazelcast.nio.serialization.Portable;
import com.tarantula.platform.AbstractRecoverableListener;

/**
 * Created by yinghu lu on 3/31/2018.
 */
public class BuddyListPortableRegistry extends AbstractRecoverableListener {

    public static final int OID = 9;

    public static final int MY_PLAY_LIST_CID = 1;
    public static final int ON_PLAY_CID = 3;
    public static final int RECENT_PLAY_LIST_CID = 4;
    public static final int ON_BUDDY_CID = 6;
    public Portable create(int i) {
        Portable pt = null;
        switch (i){
            case MY_PLAY_LIST_CID:
                pt = new BuddyList();
                break;
            case ON_PLAY_CID:
                pt = new OnPlayTrack();
                break;
            case RECENT_PLAY_LIST_CID:
                pt = new RecentPlayList();
                break;
            case ON_BUDDY_CID:
                pt = new OnBuddy();
                break;
            default:
        }
        return pt;
    }

    public int registryId() {
        return OID;
    }
}
