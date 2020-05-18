package com.tarantula;

public interface Account extends Access{

    int userCount();
    void userCount(int userCount);
    int gameClusterCount();
    void gameClusterCount(int userCount);
    boolean trial();
    void trial(boolean trial);

}
