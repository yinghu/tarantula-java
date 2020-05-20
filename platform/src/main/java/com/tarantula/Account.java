package com.tarantula;

public interface Account extends Access{

    String DataStore = "account";
    String UserLabel = "users";
    String GameClusterLabel = "games";

    int userCount(int delta);
    int gameClusterCount(int delta);
    boolean trial();
    void trial(boolean trial);

}
