package com.tarantula.platform.item;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.AbstractRecoverableListener;


public class ItemPortableRegistry extends AbstractRecoverableListener {

    public static final int OID = 7;

    public static final int ITEM_CID = 4;



    public Recoverable create(int i) {
        Recoverable pt = null;
        switch (i){
            case ITEM_CID:
                pt = new Item();
                break;
            default:
        }
        return pt;
    }

    public int registryId() {
        return OID;
    }
}
