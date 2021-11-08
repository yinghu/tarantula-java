package com.icodesoftware;

import com.icodesoftware.service.BucketListener;

public interface TarantulaApplication extends Initializer, EventListener, BucketListener{


    default void initialize(Session session) throws Exception{}
	
	void callback(Session session,byte[] payload) throws Exception;

    void onError(Session session, Exception ex);

    default void clear(){}

}
