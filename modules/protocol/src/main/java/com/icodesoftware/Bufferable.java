package com.icodesoftware;

public interface Bufferable {

    default boolean read(Recoverable.DataBuffer buffer){ return false;}
    default boolean write(Recoverable.DataBuffer buffer){ return false;}

    default boolean readKey(Recoverable.DataBuffer buffer){ return false;}
    default boolean writeKey(Recoverable.DataBuffer buffer){ return false;}
}
