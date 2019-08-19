package com.tarantula.demo;

import com.tarantula.Recoverable;
import com.tarantula.platform.AbstractRecoverableListener;

public class DemoPortableRegistry extends AbstractRecoverableListener {

    public static final int OID = 100;
    public static final int TIMER_OID = 1;
    @Override
    public int registryId() {
        return OID;
    }

    @Override
    public Recoverable create(int i) {
        Recoverable pt = null;
        switch (i){
            case TIMER_OID:
                pt = new Timer();
                break;
            default:
        }
        return pt;
    }
}
