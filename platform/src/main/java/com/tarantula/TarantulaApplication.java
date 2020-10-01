package com.tarantula;

import com.icodesoftware.Connection;
import com.icodesoftware.EventListener;
import com.icodesoftware.Session;

public interface TarantulaApplication extends Initializer, EventListener,BucketListener, Connection.Listener{


    default void initialize(Session session) throws Exception{}
	
	void callback(Session session,byte[] payload) throws Exception;

    void onError(Session session, Exception ex);

    default void clear(){}

}
