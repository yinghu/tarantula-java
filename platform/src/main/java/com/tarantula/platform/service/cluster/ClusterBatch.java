package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

import com.icodesoftware.service.Batchable;

import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClusterBatch implements Batchable, Portable {


    private List<byte[]> key = new ArrayList<>();

    private List<byte[]> data = new ArrayList<>();

    private KeyValueSet[] batch;

    public ClusterBatch(){
    }

    public ClusterBatch(Batchable batchable){
        key = batchable.key();
        data = batchable.data();
        batch = new KeyValueSet[batchable.size()];
        for(int i=0;i< batchable.size();i++){
            batch[i]=new KeyValueSet(key.get(i),data.get(i));
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
        out.writePortableArray("batch",batch);
    }

    @Override
    public void readPortable(PortableReader in) throws IOException {
        batch = (KeyValueSet[])in.readPortableArray("batch");
    }

    @Override
    public int size() {
        int sz = batch.length;
        for(KeyValueSet kv : batch){
            key.add(kv.key);
            data.add(kv.value);
        }
        return sz;
    }

    @Override
    public List<byte[]> data() {
        return data;
    }

    @Override
    public List<byte[]> key() {
        return key;
    }

}
