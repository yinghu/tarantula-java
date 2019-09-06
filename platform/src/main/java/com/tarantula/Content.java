package com.tarantula;

public interface Content extends Recoverable{
    String type();
    void type(String type);
    String name();
    void name(String name);
}
