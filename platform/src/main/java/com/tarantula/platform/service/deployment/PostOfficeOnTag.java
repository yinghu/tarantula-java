package com.tarantula.platform.service.deployment;

import com.icodesoftware.PostOffice;
import com.icodesoftware.Recoverable;
import com.icodesoftware.RoutingKey;
import com.icodesoftware.service.EventService;
import com.tarantula.platform.event.MapStoreSyncEvent;

public class PostOfficeOnTag implements PostOffice.OnTag {

    private final String tag;
    private final EventService eventService;
    public PostOfficeOnTag(String tag,EventService eventService){
        this.tag = tag;
        this.eventService = eventService;
    }
    @Override
    public <T extends Recoverable> void send(Object distributionKey,T data) {
        //String key = data.key().asString();
        //byte[] payload = data.toBinary();
        //data.writeKey()
        RoutingKey routingKey = eventService.routingKey(distributionKey,tag);
        MapStoreSyncEvent mapStoreSyncEvent = new MapStoreSyncEvent(data);//new MapStoreSyncEvent(routingKey.route(),data.owner(),data.getFactoryId(),data.getClassId(),key!=null?key:"",payload);
        mapStoreSyncEvent.destination(routingKey.route());
        mapStoreSyncEvent.routingNumber(routingKey.routingNumber());
        eventService.publish(mapStoreSyncEvent);
    }

}
