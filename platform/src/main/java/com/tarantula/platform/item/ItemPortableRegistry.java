package com.tarantula.platform.item;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.AbstractRecoverableListener;


public class ItemPortableRegistry extends AbstractRecoverableListener {

    public static final int OID = 7;

    public static final int ASSET_CID = 2;
    public static final int COMMODITY_CID = 3;
    public static final int ITEM_CID = 4;

    public Recoverable create(int i) {
        Recoverable pt = null;
        switch (i){
            case ASSET_CID:
                pt = new Asset();
                break;
            case COMMODITY_CID:
                pt = new Commodity();
                break;
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
