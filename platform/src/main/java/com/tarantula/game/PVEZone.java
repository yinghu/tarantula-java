package com.tarantula.game;

import com.icodesoftware.Descriptor;
import com.icodesoftware.service.ServiceContext;

public class PVEZone extends Zone{

    public PVEZone(){
        super();
        mode = Zone.PVE;
    }

    public PVEZone(Descriptor descriptor){
        super(descriptor);
        mode = Zone.PVE;
    }
    @Override
    public Stub join(Rating rating) {
        Stub stub = new Stub();
        stub.rating = rating;
        stub.successful(true);
        stub.arena = "level1";
        stub.offline = true;
        stub.tag = descriptor.tag();
        return stub;
    }
    public void update(ServiceContext serviceContext){
        System.out.println("loading zone from cluster->"+this.distributionKey()+dataStore.name());

    }
    @Override
    public int getFactoryId() {
        return GamePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return GamePortableRegistry.PVE_ZONE_CID;
    }

}
