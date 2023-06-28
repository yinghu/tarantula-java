package com.icodesoftware;

import java.io.InputStream;

public interface Module{

    default void onJoin(Session session) throws Exception{}

    boolean onRequest(Session session, byte[] payload) throws Exception;

    void setup(ApplicationContext context) throws Exception;

    default void clear(){}

    default Descriptor descriptor(){ return null;}

    interface OnResource{
        void on(InputStream in);
    }
}
