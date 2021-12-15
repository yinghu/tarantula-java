package com.icodesoftware;

public interface Presence extends Balance,DataStore.Updatable,Countable{

    String DataStore = "presence";

    String LOBBY_TAG = "presence/lobby";

    Response onPlay(Session session, Descriptor descriptor);

    boolean online();

}
