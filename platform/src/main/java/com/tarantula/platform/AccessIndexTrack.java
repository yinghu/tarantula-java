package com.tarantula.platform;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.AccessIndex;
import com.tarantula.Recoverable;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * updated 8/23/19
 */
public class AccessIndexTrack extends IntegrationScopeObject implements AccessIndex, Portable {

    public AccessIndexTrack(){
        this.binary = true;
    }

    public AccessIndexTrack(String owner, String systemId){
        this();
        this.owner = owner;
        String[] sid = systemId.split(Recoverable.PATH_SEPARATOR);
        this.bucket = sid[0];
        this.oid = sid[1];
    }
    public AccessIndexTrack(String owner){
        this();
        this.owner = owner;
    }

    @Override
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableRegistry.ACCESS_INDEX_CID;
    }

    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.owner);
        out.writeUTF("2",bucket);
        out.writeUTF("3",this.oid);
    }

    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.owner = in.readUTF("1");
        this.bucket = in.readUTF("2");
        this.oid  = in.readUTF("3");
    }

    @Override
    public byte[] toByteArray(){
        ByteBuffer buffer = ByteBuffer.allocate(8+bucket.length()+oid.length());
        buffer.putInt(this.bucket.length());
        buffer.put(this.bucket.getBytes());
        buffer.putInt(this.oid.length());
        buffer.put(this.oid.getBytes());
        return buffer.array();
    }
    @Override
    public void fromByteArray(byte[] data){
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int len = buffer.getInt();
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<len;i++){
            sb.append((char) buffer.get());
        }
        this.bucket = sb.toString();
        len = buffer.getInt();
        sb.setLength(0);
        for(int i=0;i<len;i++){
            sb.append((char) buffer.get());
        }
        this.oid = sb.toString();
        //read data from byte array
    }
    @Override
    public String toString(){
        return "Access Index ["+bucket+"/"+owner+"/"+oid+"]";
    }
    public void distributionKey(String distributionKey){
       //skip the natural key
    }
    public Key key(){
        return new NaturalKey(this.owner);
    }
}
