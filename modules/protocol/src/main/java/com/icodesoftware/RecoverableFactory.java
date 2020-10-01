package com.icodesoftware;

/**
 * Updated by yinghu on 3/26/2018.
 * The vertex query factory with a filter
 */
public interface RecoverableFactory<T extends Recoverable>{


    T create();

    /**
     * The class id of vertex
     * */
    int registryId();

    /**
     * The name of the vertex or edge
     * */
    String label();

    /**
     * The owner key of the edge mapping or the vertext filter string
     * */
    String distributionKey();

}
