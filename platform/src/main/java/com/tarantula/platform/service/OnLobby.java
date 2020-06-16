package com.tarantula.platform.service;


import com.tarantula.Recoverable;

/**
 * Updated by yinghu lu on 6/16/2020
 */
public interface OnLobby extends Recoverable {

    String typeId(); //associated with lobby id
    String gameClusterId(); //associated with game cluster
    String subscriptionId(); //associated with membership
    boolean resetEnabled();
    boolean closed();
    void closed(boolean closed);

    interface Listener{
        void onLobby(OnLobby onLobby);
    }
}
