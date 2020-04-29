package com.tarantula.platform.service;


import com.tarantula.Recoverable;

/**
 * Updated by yinghu lu on 7/20/19
 */
public interface OnLobby extends Recoverable {

    String typeId();
    void typeId(String typeId);

    boolean closed();
    void closed(boolean closed);

    interface Listener{
        void onLobby(OnLobby onLobby);
    }
}
