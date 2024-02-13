package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

import com.icodesoftware.service.Batchable;

import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.util.List;

public class ClusterBatch implements Batchable, Portable {


    private int size;
    private Portable[] batch;

    public ClusterBatch(){
    }

    public ClusterBatch(List<Batchable.BatchData> batchable){
        this.size = batchable.size();
        batch = new KeyValueSet[size];
        for(int i=0;i< batchable.size();i++){
            BatchData data = batchable.get(i);
            batch[i]=new KeyValueSet(data.key(), data.value());
        }
    }

    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.CLUSTER_BATCH_CID;
    }

    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeInt("size",size);
        out.writePortableArray("batch",batch);
    }

    @Override
    public void readPortable(PortableReader in) throws IOException {
        size = in.readInt("size");
        batch = in.readPortableArray("batch");
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public BatchData[] batch() {
        BatchData[] batchData = new BatchData[size];
        for(int i=0;i<size;i++){
            batchData[i]= (BatchData)batch[i];
        }
        return batchData;
    }
}
