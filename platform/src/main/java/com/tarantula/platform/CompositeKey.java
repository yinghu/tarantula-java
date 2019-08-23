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
public class CompositeKey extends RecoverableObject implements Recoverable.Key {

    public String owner;
    public String key;

    public CompositeKey(){}
    public CompositeKey(String owner, String key){
        this.owner = owner;
        this.key = key;
    }

    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    public int getClassId() {
        return PortableRegistry.COMPOSITE_KEY_CID;
    }

    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1", this.owner);
        out.writeUTF("2",this.key);
    }

    public void readPortable(PortableReader in) throws IOException {
        this.owner = in.readUTF("1");
        this.key = in.readUTF("2");
    }
    public byte[] toByteArray(){
        return this.key.getBytes(Charset.forName("UTF-8"));
    }
    public void fromByteArray(byte[] data){
        StringBuffer sb = new StringBuffer();
        for(byte b : data){
            sb.append((char)b);
        }
        this.key = sb.toString();
    }
    public String asString(){
        return this.owner+"_"+this.key;
    }
    @Override
    public String toString(){
        return "Owner access key ["+owner+","+key+"]";
    }
    @Override
    public int hashCode(){
        return this.owner.hashCode()+this.key.hashCode();
    }
    @Override
    public boolean equals(Object obj){
        CompositeKey r = (CompositeKey)obj;
        return owner.equals(r.owner)&&key.equals(r.key);
    }
}

