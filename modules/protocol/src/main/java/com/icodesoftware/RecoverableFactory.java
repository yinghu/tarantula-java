package com.icodesoftware;

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

    //String distributionKey();

    default Recoverable.Key key(){return null;}

}
