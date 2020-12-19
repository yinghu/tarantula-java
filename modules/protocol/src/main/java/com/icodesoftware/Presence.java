package com.icodesoftware;

/**
 * updated by yinghu on 4/11/2019.
 */
public interface Presence extends Balance,DataStore.Updatable,Countable{

    String DataStore = "presence";

    String LOBBY_TAG = "presence/lobby";

    Response onPlay(Session session, Descriptor descriptor);

    boolean online();
}
