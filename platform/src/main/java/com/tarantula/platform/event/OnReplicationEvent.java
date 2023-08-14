package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.service.OnReplication;
import com.tarantula.platform.service.persistence.ReplicationData;
import com.tarantula.platform.service.persistence.ScopedOnReplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public class OnReplicationEvent extends Data implements EventOnReplication {

    private OnReplication[] data;

    private ArrayBlockingQueue<ScopedOnReplication> pendingQueue;

    public OnReplicationEvent(){}
    public OnReplicationEvent(int pendingSize, String sourceNode, String destination){
        this.source = sourceNode;
        this.destination = destination;
        this.pendingQueue = new ArrayBlockingQueue<>(pendingSize);
    }
    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.destination);
        out.writeUTF("2",source);
        out.writeInt("3",data.length);
        for(int i=0;i<data.length;i++){
            out.writeUTF("p"+i,data[i].source());
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
        for(int i=0;i<data.length;i++){
            data[i]=new ReplicationData(source,in.readUTF("p"+i),in.readByteArray("k"+i),in.readByteArray("v"+i));
        }
    }
    @Override
    public String toString(){
        return "on replication event ->"+destination;
    }
    public boolean offer(ScopedOnReplication scopedOnReplication){
        return pendingQueue.offer(scopedOnReplication);
    }
    public void drain(){
        ArrayList<ScopedOnReplication> list = new ArrayList<>();
        int drained = pendingQueue.drainTo(list);
        data = new OnReplication[drained];
        for (int i=0; i< drained;i++){
            data[i] = list.get(i).read();
        }
    }
    public void drop(){
        ArrayList<ScopedOnReplication> list = new ArrayList<>();
        pendingQueue.drainTo(list);
        list.forEach(e->e.drop());
    }
    public OnReplication[] data(){
        return data;
    }
}
