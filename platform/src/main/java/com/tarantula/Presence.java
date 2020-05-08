package com.tarantula;


/**
 * updated by yinghu on 4/11/2019.
 */
public interface Presence extends Balance,DataStore.Updatable,Countable{

    String LOBBY_TAG = "presence/lobby";

    Response onPlay(Session session,OnAccess onAccess,Descriptor descriptor);

    Response onPlay(Session session,Descriptor descriptor);

    Response onPlay(Session session,Descriptor descriptor,RoutingKey routingKey);

    boolean online();
}
