package com.tarantula;

public interface Account extends Access{

    String emailAddress();
    void emailAddress(String emailAddress);
    String name();
    void name(String name);
    boolean trial();
    void trial(boolean trial);

}
