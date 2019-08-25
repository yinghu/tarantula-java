package com.tarantula.service.payment;

import com.tarantula.Recoverable;
import com.tarantula.platform.AbstractRecoverableListener;

public class ItemServicePortableRegistry extends AbstractRecoverableListener {

    public static final int OID = 100;

    @Override
    public int registryId() {
        return OID;
    }

    @Override
    public Recoverable create(int i) {
        Recoverable pt = null;
        switch (i){

            default:
        }
        return pt;
    }
}
