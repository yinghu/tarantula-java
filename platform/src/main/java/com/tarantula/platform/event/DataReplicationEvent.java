package com.tarantula.platform.event;

import com.icodesoftware.service.RecoverService;

public class DataReplicationEvent extends OnReplicationEvent {


    public DataReplicationEvent(){

    }
    public DataReplicationEvent(int pendingSize,String sourceNode,String targetNode){
        super(pendingSize,sourceNode,targetNode+"."+RecoverService.NAME);
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.DATA_ON_REPLICATION_EVENT_CID;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

}
