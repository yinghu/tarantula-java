package com.tarantula;

import java.io.InputStream;

public interface Module {

    default void onJoin(Session session, Connection onConnection,OnUpdate onUpdate) throws Exception{}

    boolean onRequest(Session session, byte[] payload,OnUpdate update) throws Exception;

    void setup(ApplicationContext context) throws Exception;

    String label();
    default void clear(){}

    default void onTimeout(Session session,OnUpdate onUpdate){}
    default void onIdle(Session session,OnUpdate onUpdate){}
    default void onTimer(OnUpdate update){}
    interface OnUpdate{
        void on(String updateId,byte[] delta);
    }
    interface OnResource{
        void on(InputStream in);
    }
}
