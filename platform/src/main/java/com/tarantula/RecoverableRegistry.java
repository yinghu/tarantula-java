package com.tarantula;

import com.icodesoftware.Recoverable;

/**
 * Updated by yinghu lu on 8/5/2020
 */
public interface RecoverableRegistry{

    int registryId();

    Recoverable create(int classId);

    <T extends Recoverable> RecoverableFactory<T> query(int registerId,String[] params);
}
