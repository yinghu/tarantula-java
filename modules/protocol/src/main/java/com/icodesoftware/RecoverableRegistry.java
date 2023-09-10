package com.icodesoftware;

public interface RecoverableRegistry<T extends Recoverable> {

    int registryId();

    //<T extends Recoverable> Recoverable create(int classId);
    T create(int classId);
    //<T extends Recoverable> RecoverableFactory<T> query(int registerId, String[] params);
}
