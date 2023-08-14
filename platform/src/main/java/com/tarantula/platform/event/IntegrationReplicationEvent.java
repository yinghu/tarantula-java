package com.tarantula.platform.event;

import com.icodesoftware.service.AccessIndexService;


public class IntegrationReplicationEvent extends OnReplicationEvent {


    public IntegrationReplicationEvent(){

    }
    public IntegrationReplicationEvent(int pendingSize,String sourceNode,String targetNode){
        super(pendingSize,sourceNode,targetNode+"."+ AccessIndexService.NAME);
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.INTEGRATION_ON_REPLICATION_EVENT_CID;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }


}
