package com.tarantula.game;

import com.hazelcast.nio.serialization.Portable;

import com.tarantula.platform.AbstractRecoverableListener;

/**
 * Update by yinghu lu on 5/10/2019.
 */
public class GameRecoverableRegistry extends AbstractRecoverableListener {

    public static final int OID = 100;

    @Override
    public int registryId() {
        return 100;
    }

    @Override
    public Portable create(int i) {
        Portable po=null;

        return po;
    }
}
