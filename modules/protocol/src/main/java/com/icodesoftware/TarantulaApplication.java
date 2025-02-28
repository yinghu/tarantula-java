package com.icodesoftware;


public interface TarantulaApplication extends Initializer, EventListener{


    void initialize(Session session) throws Exception;

     void callback(Session session) throws Exception;

     void callback(Session session,byte[] payload) throws Exception;

     void onError(Session session, Exception ex);

     void clear();

}
