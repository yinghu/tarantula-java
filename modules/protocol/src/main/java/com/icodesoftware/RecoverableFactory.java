package com.icodesoftware;

public interface RecoverableFactory<T extends Recoverable>{

    T create();
    /**
     * The name of the vertex or edge
     * */
    String label();


    Recoverable.Key key();

}
