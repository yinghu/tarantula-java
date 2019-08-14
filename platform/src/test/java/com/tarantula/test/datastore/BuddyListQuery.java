package com.tarantula.test.datastore;

import com.tarantula.RecoverableFactory;
import com.tarantula.platform.playlist.BuddyList;

/**
 * Created by yinghu lu on 11/8/2017.
 */
public class BuddyListQuery implements RecoverableFactory<BuddyList> {


    public BuddyList create() {
        return new BuddyList();
    }


    public int registryId() {
        return 0;
    }

    public String label(){
        return "BuddyList";
    }
    public boolean onEdge(){
        return false;
    }
    public String distributionKey(){
        return null;
    }
}
