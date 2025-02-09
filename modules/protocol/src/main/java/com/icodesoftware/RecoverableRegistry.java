package com.icodesoftware;

public interface RecoverableRegistry<T extends Recoverable> {

    int registryId();
    default T create(int classId){throw new UnsupportedOperationException();}
    default T create(int classId,EventListener eventListener){ throw new UnsupportedOperationException();}
}
