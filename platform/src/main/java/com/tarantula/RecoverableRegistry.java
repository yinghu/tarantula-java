package com.tarantula;

/**
 * Created by yinghu lu on 3/31/2018.
 */
public interface RecoverableRegistry{
    int registryId();
    Recoverable create(int classId);
}
