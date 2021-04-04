package com.tarantula.game;

import com.icodesoftware.Distributable;
import com.icodesoftware.service.RecoverService;
import com.icodesoftware.service.ServiceContext;

public class PVEZone extends Zone{

    public PVEZone(){
        super();
        mode = Zone.PVE;
    }
    @Override
    public Stub join(Rating rating) {
        Stub stub = new Stub();
        stub.rating = rating;
        stub.successful(true);
        stub.arena = aMap.get(rating.xpLevel).name();
        stub.offline = true;
        stub.tag = descriptor.tag();
        return stub;
    }
    public void update(ServiceContext serviceContext){
        RecoverService deployService = serviceContext.clusterProvider(Distributable.INTEGRATION_SCOPE).recoverService();
        Zone zone = new PVEZone();
        zone.distributionKey(descriptor.distributionKey());
        byte[] _data = deployService.load(null,this.dataStore.name(),zone.distributionKey().getBytes());
        zone.fromBinary(_data);
        for(int i=1;i<descriptor.capacity()+1;i++){
            Arena a = new Arena(zone.bucket(),zone.oid(),i);
            _data = deployService.load(null,dataStore.name(),a.distributionKey().getBytes());
            if(_data!=null){
                a.fromBinary(_data);
                if(!a.disabled()){//skip disabled
                    zone.arenas.add(a);
                }
            }
        }
        this.listener.onUpdated(zone);
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
