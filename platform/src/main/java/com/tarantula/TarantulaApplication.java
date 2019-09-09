package com.tarantula;

public interface TarantulaApplication extends Initializer,EventListener,BucketListener,OnConnection.Listener{


    default void initialize(Session session) throws Exception{}
	
	void callback(Session session,byte[] payload) throws Exception;

    void onError(Session session, Exception ex);

    default void clear(){}

}
