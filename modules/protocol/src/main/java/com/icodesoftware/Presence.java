package com.icodesoftware;

public interface Presence extends Recoverable,DataStore.Updatable,Countable{

    String DataStore = "tarantula_presence";

    String LOBBY_TAG = "presence/lobby";

    Response onPlay(Session session, Descriptor descriptor);

    boolean online();

    boolean local();

}
