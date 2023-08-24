package com.icodesoftware;

//copy-free key-value store operations
public interface Bufferable {

    String UTF_NULL = "nil";
    default boolean read(Recoverable.DataBuffer buffer){ return false;}
    default boolean write(Recoverable.DataBuffer buffer){ return false;}

    default boolean readKey(Recoverable.DataBuffer buffer){ return false;}
    default boolean writeKey(Recoverable.DataBuffer buffer){ return false;}
}
