package com.tarantula.platform.service.deployment;


import com.tarantula.platform.AssociateObject;
import com.tarantula.platform.service.cluster.PortableRegistry;

public class BucketIndex extends AssociateObject {

    public int lobbyCount;
    public BucketIndex(){
        this.label = "bucketIndex";
    }

    public BucketIndex(long associateId){
        this();
        this.id = associateId;
    }
    public boolean write(DataBuffer buffer){
        buffer.writeInt(lobbyCount);
        return true;
    }
    public boolean read(DataBuffer buffer) {
        this.lobbyCount = buffer.readInt();
        return true;
    }

    @Override
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableRegistry.BUCKET_INDEX_OID;
    }


}
