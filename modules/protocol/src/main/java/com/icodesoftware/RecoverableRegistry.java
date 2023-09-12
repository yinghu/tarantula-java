package com.icodesoftware;

public interface RecoverableRegistry<T extends Recoverable> {

    int registryId();
    T create(int classId);
}
