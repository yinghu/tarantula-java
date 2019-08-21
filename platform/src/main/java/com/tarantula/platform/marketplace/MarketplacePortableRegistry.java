package com.tarantula.platform.marketplace;

import com.tarantula.Recoverable;
import com.tarantula.platform.AbstractRecoverableListener;


/**
 * Created by yinghu lu on 3/31/2018.
 */
public class MarketplacePortableRegistry extends AbstractRecoverableListener {

    public static final int OID = 7;

    public static final int VIRTUAL_CREDITS_PACK = 1;


    public Recoverable create(int i) {
        Recoverable pt = null;
        switch (i){
            case VIRTUAL_CREDITS_PACK:
                pt = new VirtualCreditsPack();
                break;

            default:
        }
        return pt;
    }

    public int registryId() {
        return OID;
    }
}
