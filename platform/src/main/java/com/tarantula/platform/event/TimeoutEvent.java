package com.tarantula.platform.event;

import com.tarantula.Event;
import com.tarantula.platform.Data;

/**
 * Updated by yinghu on 4/8/2018.
 */
public class TimeoutEvent extends Data implements Event {
    public TimeoutEvent(String systemId,int stub,int routingNumber){
        this.systemId = systemId;
        this.stub = stub;
        this.routingNumber =routingNumber;
    }
    @Override
    public String toString(){
        return "Time out ["+systemId+"/"+stub+"/"+routingNumber+"]";
    }
}
