package com.tarantula.cci;

import com.tarantula.*;

/**
 * Created by yinghu lu on 4/27/2018.
 * RequestHandler provides the asynchronous way to exchange data between server and clients
 * Each client handler such as http , web socket, or raw socket should be using event service to exchange data
 */
public interface RequestHandler extends EventListener,Serviceable{
    String name();
    void onRequest(OnExchange exchange);
    void setup(TokenValidator tokenValidator, EventService eventService, AccessIndexService accessIndexService,String bucket);
    default void onCheck(){}
}
