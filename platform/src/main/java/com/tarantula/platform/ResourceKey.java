package com.tarantula.platform;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Recoverable;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Updated by yinghu on 6/15/2018.
 */
public class ResourceKey implements Recoverable.Key {

    public String bucket;
    public String oid;
    public String[] key;

    public ResourceKey(){}
    public ResourceKey(String bucket,String oid, String[] key){
        this.bucket = bucket;
        this.oid = oid;
        this.key = key;
    }
    public String getPartitionKey() {
        return this.oid;
    }

    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    public int getClassId() {
        return PortableRegistry.RESOURCE_KEY_CID;
    }

    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1", this.bucket);
        out.writeUTF("2",this.oid);
        out.writeUTFArray("3",this.key);
    }

    public void readPortable(PortableReader in) throws IOException {
        this.bucket = in.readUTF("1");
        this.oid = in.readUTF("2");
        this.key = in.readUTFArray("3");
    }
    public byte[] toByteArray(){
        return this.asString().getBytes();
    }
    public void fromByteArray(byte[] data){
        StringBuffer sb = new StringBuffer();
        for(byte b : data){
            sb.append((char)b);
        }
    }
    public String asString(){
        StringBuffer sb = new StringBuffer(bucket);
        sb.append(Recoverable.PATH_SEPARATOR).append(oid);
        for(String s : this.key){
            sb.append(Recoverable.PATH_SEPARATOR);
            sb.append(s);
        }
        return sb.toString();
    }
    @Override
    public String toString(){
        return asString();
    }

    /**
    @Override
    public int hashCode(){
        return this.owner.hashCode()+this.key.hashCode();
    }
    @Override
    public boolean equals(Object obj){
        ResourceKey r = (ResourceKey)obj;
        return owner.equals(r.owner)&&key.equals(r.key);
    }**/
}

