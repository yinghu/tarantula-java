package com.tarantula;

import java.util.List;

/**
 * updated by yinghu on 8/7/2019
 */
public interface InstanceRegistry extends OnApplication,Countable{

    int ON_INSTANCE =0;
    int ALREADY_ON_INSTANCE =1;
    int INSTANCE_FULL = 4;
    String LABEL ="INS";
    int capacity();
    void capacity(int capacity);

    int onJoin(Event event);
    void onLeave(Session session);

    boolean transact(String systemId,double delta);
    double balance(String systemId);

    List<OnInstance> onInstance();
    OnInstance onInstance(String systemId);
    void registerOnInstanceListener(OnInstance.Listener listener);

    interface Listener{
        void onRegistry(InstanceRegistry instanceRegistry);
        String onLobby();
    }
}
