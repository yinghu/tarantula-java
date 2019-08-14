package com.tarantula.platform.playlist;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.OnPlay;
import com.tarantula.platform.OnApplicationHeader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Updated by yinghu on 4/24/2018.
 */
public class OnBuddy extends OnApplicationHeader implements OnPlay {

    private String category;

    public OnBuddy(){
        this.vertex ="OnBuddy";
        this.label = "ROB";
        this.binary = true;
    }
    public OnBuddy(String systemId){
        this();
        this.systemId = systemId;
    }
    public int getFactoryId() {
        return BuddyListPortableRegistry.OID;
    }


    public int getClassId() {
        return BuddyListPortableRegistry.ON_BUDDY_CID;
    }
    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.systemId);
        out.writeUTF("2",this.name);
        out.writeUTF("3",this.category);
        out.writeLong("4",this.timestamp);
        out.writeUTF("5",this.applicationId);
        out.writeUTF("6",this.instanceId);
        out.writeDouble("7",this.balance);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.systemId = in.readUTF("1");
        this.name = in.readUTF("2");
        this.category = in.readUTF("3");
        this.timestamp = in.readLong("4");
        this.applicationId = in.readUTF("5");
        this.instanceId = in.readUTF("6");
        this.balance = in.readDouble("7");
    }
    @Override
    public String toString(){
        return "On play ["+systemId+"] on ["+name+"/"+category+"]["+timestamp+"]";
    }


    public String category(){
        return this.category;
    }
    public void category(String category){
        this.category = category;
    }

    @Override
    public byte[] toByteArray(){
        ByteBuffer buffer = ByteBuffer.allocate(4+systemId.length());
        buffer.putInt(systemId.length());
        buffer.put(systemId.getBytes(Charset.forName("UTF-8")));
        return buffer.array();
    }
    @Override
    public void fromByteArray(byte[] data){
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int len = buffer.getInt();
        StringBuffer sb = new StringBuffer(len);
        for(int i=0;i<len;i++){
            sb.append((char) buffer.get());
        }
        this.systemId = sb.toString();
    }
}
