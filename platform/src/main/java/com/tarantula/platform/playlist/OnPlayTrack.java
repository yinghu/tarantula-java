package com.tarantula.platform.playlist;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.OnPlay;
import com.tarantula.platform.OnApplicationHeader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Updated by yinghu on 3/5/2018.
 */
public class OnPlayTrack extends OnApplicationHeader implements OnPlay {

    private String category;

    public OnPlayTrack(){
        this.vertex ="OnPlay";
        this.label = "ROP";
    }
    public OnPlayTrack(String systemId){
        this();
        this.systemId = systemId;
    }
    public int getFactoryId() {
        return BuddyListPortableRegistry.OID;
    }


    public int getClassId() {
        return BuddyListPortableRegistry.ON_PLAY_CID;
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
    public Map<String,Object> toMap(){
        this.properties.put("1",this.name);
        this.properties.put("2",this.systemId);
        this.properties.put("3",this.applicationId);
        this.properties.put("4",this.instanceId);
        this.properties.put("5",this.balance);
        this.properties.put("6",this.category);
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.name = (String)properties.get("1");
        this.systemId = (String)properties.get("2");
        this.applicationId = (String)properties.get("3");
        this.instanceId = (String)properties.get("4");
        this.balance = ((Number)properties.get("5")).doubleValue();
        this.category = (String)properties.get("6");
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
