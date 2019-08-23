package com.tarantula.platform;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Recoverable;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Update by yinghu on 8/17/19
 */
public class DistributionKey extends RecoverableObject implements Recoverable.Key{

    private String bucket;
    private String oid;

    public DistributionKey(){}
    public DistributionKey(String bucket,String oid){
        this.bucket = bucket;
        this.oid = oid;
    }

    public int getFactoryId() {
        return PortableRegistry.OID;
    }


    public int getClassId() {
        return PortableRegistry.DISTRIBUTION_KEY_CID;
    }


    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.oid);
    }


    public void readPortable(PortableReader in) throws IOException {
        this.oid = in.readUTF("1");
    }
    public byte[] toByteArray(){
        return new StringBuffer(bucket).append(Recoverable.PATH_SEPARATOR).append(oid).toString().getBytes();
    }
    public void fromByteArray(byte[] data){

    }
    public String asString(){
        if(bucket!=null&&oid!=null){
            return new StringBuffer(bucket).append(Recoverable.PATH_SEPARATOR).append(oid).toString();
        }
        else{
            return null;
        }
    }
    @Override
    public String toString(){
        return asString();
    }
    @Override
    public boolean equals(Object obj){
        return this.oid.equals(((Recoverable.Key)obj).oid());
    }
    @Override
    public int hashCode(){
        return this.oid.hashCode();
    }
}