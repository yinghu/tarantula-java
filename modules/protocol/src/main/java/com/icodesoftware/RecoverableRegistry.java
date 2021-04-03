package com.icodesoftware;

public interface RecoverableRegistry {

    int registryId();

    Recoverable create(int classId);

    <T extends Recoverable> RecoverableFactory<T> query(int registerId, String[] params);
}
