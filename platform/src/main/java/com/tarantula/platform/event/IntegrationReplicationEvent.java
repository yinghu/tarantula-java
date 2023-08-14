package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Event;
import com.icodesoftware.service.AccessIndexService;
import com.icodesoftware.service.OnReplication;
import com.tarantula.platform.service.persistence.OffHeapIntegrationScopeReplication;
import com.tarantula.platform.service.persistence.ScopedOnReplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public class IntegrationReplicationEvent extends Data implements Event {


    public ScopedOnReplication[] mp;
    public ArrayBlockingQueue<ScopedOnReplication> pendingQueue;
    public IntegrationReplicationEvent(){

    }
    public IntegrationReplicationEvent(int pendingSize,String sourceNode,String targetNode){
        this.source = sourceNode;
        this.destination = targetNode+"."+ AccessIndexService.NAME;
        this.pendingQueue = new ArrayBlockingQueue<>(pendingSize);
    }
    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.destination);
        out.writeUTF("2",source);
        ArrayList<ScopedOnReplication> list = new ArrayList<>();
        pendingQueue.drainTo(list);
        int sz = list.size();
        System.out.println("SIZE->"+sz);
        out.writeInt("3",sz);
        for(int i=0;i<sz;i++){
            OnReplication data = list.get(i).read();
            out.writeInt("p"+i,data.partition());
            out.writeByteArray("k"+i,data.key());
            out.writeByteArray("v"+i,data.value());
        }
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.destination = in.readUTF("1");
        this.source = in.readUTF("2");
        int size = in.readInt("3");
        System.out.println("OUT SZ->"+size);
        mp = new ScopedOnReplication[size];
        for(int i=0;i<size;i++){
            //data[i]=new ReplicationData(source,in.readInt("p"+i),in.readByteArray("k"+i),in.readByteArray("v"+i));
            mp[i]=new OffHeapIntegrationScopeReplication();
            mp[i].write(source,in.readInt("p"+i),in.readByteArray("k"+i),in.readByteArray("v"+i));
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
