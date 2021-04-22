package com.icodesoftware.service;


import com.icodesoftware.Configurable;

public interface OnLobby extends Configurable {

    String TYPE = "OnLobby";

    String typeId(); //associated with lobby id
    String gameClusterId(); //associated with game cluster
    String subscriptionId(); //associated with membership
    int deployCode();
    boolean resetEnabled();
    boolean closed();
    void closed(boolean closed);
}
