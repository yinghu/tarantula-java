package com.tarantula;

public interface Account extends Access{

    String name();
    void name(String name);
    int userCount();
    void userCount(int userCount);
    boolean trial();
    void trial(boolean trial);

}
