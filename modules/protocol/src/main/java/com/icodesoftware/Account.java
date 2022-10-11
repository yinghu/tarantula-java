package com.icodesoftware;

public interface Account extends Access {

    String DataStore = "tarantula_account";
    String IndexDataStore = "tarantula_account_index";

    String UserLabel = "users";
    String GameClusterLabel = "games";
    String ModuleLabel = "modules";

    int userCount(int delta);
    int gameClusterCount(int delta);

    boolean trial();
    void trial(boolean trial);

    boolean subscribed();
    void subscribed(boolean subscribed);
}
