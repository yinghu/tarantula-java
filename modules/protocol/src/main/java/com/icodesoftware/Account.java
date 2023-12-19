package com.icodesoftware;

public interface Account extends Access {

    String DataStore = "tarantula_account";

    int userCount(int delta);
    int gameClusterCount(int delta);

    boolean trial();
    void trial(boolean trial);

    boolean subscribed();
    void subscribed(boolean subscribed);
}
