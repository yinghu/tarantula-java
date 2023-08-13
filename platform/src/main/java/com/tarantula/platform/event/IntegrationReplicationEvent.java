package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Event;
import com.icodesoftware.service.AccessIndexService;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.OnReplication;
import com.tarantula.platform.service.persistence.OffHeapIntegrationScopeReplication;
import com.tarantula.platform.service.persistence.ReplicationData;
import com.tarantula.platform.service.persistence.ScopedOnReplication;

import java.io.IOException;

public class IntegrationReplicationEvent extends Data implements Event {

    public OnReplication[] data;
    //public ScopedOnReplication[] mp;
    public IntegrationReplicationEvent(){

    }
    public IntegrationReplicationEvent(ClusterProvider.Node sourceNode,OnReplication[] onReplication,ClusterProvider.Node targetNode){
        this.source = sourceNode.nodeName();
        this.data = onReplication;
        this.destination = targetNode.nodeName()+"."+ AccessIndexService.NAME;
    }
    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.destination);
        out.writeUTF("2",source);
        out.writeInt("3",data.length);
        for(int i=0;i<data.length;i++){
            out.writeInt("p"+i,data[i].partition());
            out.writeByteArray("k"+i,data[i].key());
            out.writeByteArray("v"+i,data[i].value());
        }
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.destination = in.readUTF("1");
        this.source = in.readUTF("2");
        int size = in.readInt("3");
        data = new OnReplication[size];
        //mp = new ScopedOnReplication[size];
        for(int i=0;i<data.length;i++){
            data[i]=new ReplicationData(source,in.readInt("p"+i),in.readByteArray("k"+i),in.readByteArray("v"+i));
            //mp[i]=new OffHeapIntegrationScopeReplication();
            //mp[i].write(source,in.readInt("p"+i),in.readByteArray("k"+i),in.readByteArray("v"+i));
        }
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.INTEGRATION_ON_REPLICATION_EVENT_CID;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    @Override
    public String toString(){
        return "on replication event ->"+destination;
    }
}
