package com.icodesoftware;


public interface Closable extends AutoCloseable{
    default void close(){}
}
