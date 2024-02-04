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

    public ClusterBatch(){
    }

    public ClusterBatch(Batchable batchable){
        key = batchable.key();
        data = batchable.data();
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
        int sz = data.size();
        out.writeInt("size",sz);
        for(int i=0;i<sz;i++){
            out.writeByteArray("k"+i,key.get(i));
            out.writeByteArray("d"+i,data.get(i));
        }
    }

    @Override
    public void readPortable(PortableReader in) throws IOException {
        int sz = in.readInt("size");
        for(int i=0;i<sz;i++){
            key.add(in.readByteArray("k"+i));
            data.add(in.readByteArray("d"+i));
        }
    }

    @Override
    public int size() {
        return data.size();
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
