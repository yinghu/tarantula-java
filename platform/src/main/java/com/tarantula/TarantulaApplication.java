package com.tarantula;

public interface TarantulaApplication extends Initializer,EventListener,BucketListener{


    default void initialize(Session session,OnConnection onConnection) throws Exception{}
	
	void callback(Session session,byte[] payload) throws Exception;

    void onError(Session session, Exception ex);

    default void clear(){}

}
