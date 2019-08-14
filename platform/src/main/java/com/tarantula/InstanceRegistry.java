package com.tarantula;

import java.util.List;

/**
 * updated by yinghu on 8/7/2019
 */
public interface InstanceRegistry extends OnApplication,Countable{

    int ON_INSTANCE =0;
    int ALREADY_ON_INSTANCE =1;
    int INSTANCE_FULL = 4;

    void  bank(boolean bank);
    boolean  bank();

    int capacity();
    void capacity(int capacity);

    House house();
    void house(House house);

    Statistics statistics();
    void statistics(Statistics statistics);


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
