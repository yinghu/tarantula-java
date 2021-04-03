package com.icodesoftware;

import java.io.InputStream;

public interface Module {

    default void onJoin(Session session, OnUpdate onUpdate) throws Exception{}

    boolean onRequest(Session session, byte[] payload,OnUpdate update) throws Exception;

    void setup(ApplicationContext context) throws Exception;

    default void clear(){}
    default void onConnection(Connection connection){}
    default void onTimeout(Session session,OnUpdate onUpdate){}
    default void onIdle(Session session,OnUpdate onUpdate){}
    default void onTimer(OnUpdate update){}
    default void onBucket(int bucket,int state){}
    interface OnUpdate{
        void on(Connection connection,String label,byte[] delta);
    }
    interface OnResource{
        void on(InputStream in);
    }
}
