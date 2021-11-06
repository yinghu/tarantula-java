package com.icodesoftware;

import java.io.InputStream;

public interface Module {

    default void onJoin(Session session) throws Exception{}

    boolean onRequest(Session session, byte[] payload) throws Exception;

    void setup(ApplicationContext context) throws Exception;

    default void clear(){}
    default void onConnection(Connection connection){}
    default void onTimeout(Session session){}
    default void onIdle(Session session){}
    default void onTimer(){}
    default void onBucket(int bucket,int state){}

    interface OnResource{
        void on(InputStream in);
    }
}
