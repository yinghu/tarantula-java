package com.icodesoftware;

public interface Bufferable {

    default void read(Recoverable.DataBuffer buffer){}
    default boolean write(Recoverable.DataBuffer buffer){ return false;}
}
