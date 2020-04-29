package com.tarantula;


/**
 * updated by yinghu on 4/11/2019.
 */
public interface Presence extends Balance,Updatable,Countable{

    String LOBBY_TAG = "presence/lobby";

    int NOT_ENOUGH_BALANCE = 3;
    int IN_TOURNAMENT_MODE = 4;

    Response onPlay(Session session,OnAccess onAccess,Descriptor descriptor);

    boolean online();
}
