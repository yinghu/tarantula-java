package com.icodesoftware.service;


import com.icodesoftware.Configurable;

/**
 * Updated by yinghu lu on 6/16/2020
 */
public interface OnLobby extends Configurable {

    String typeId(); //associated with lobby id
    String gameClusterId(); //associated with game cluster
    String subscriptionId(); //associated with membership
    int deployCode();
    boolean resetEnabled();
    boolean closed();
    void closed(boolean closed);

    interface Listener{
        void onLobby(OnLobby onLobby);
    }
}
