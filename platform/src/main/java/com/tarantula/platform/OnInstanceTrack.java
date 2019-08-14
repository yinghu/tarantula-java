package com.tarantula.platform;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.*;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.io.IOException;
import java.util.Map;

/**
 * Developer: YINGHU LU
 * Date Updated: 8/11/2019
 */
public class OnInstanceTrack extends OnApplicationHeader implements OnInstance {

    private boolean initialized;
    private boolean joined;
    private int idle;
    public OnInstanceTrack(){
        this.vertex="OnInstance";
        this.label = "IOI";
        this.onEdge = true;
    }

    public OnInstanceTrack(String systemId,int stub,String applicationId,String instanceId,boolean joined){
        this();
        this.systemId = systemId;
        this.stub = stub;
        this.applicationId = applicationId;
        this.instanceId = instanceId;
        this.joined = joined;
    }

    @Override
    public int getFactoryId() {
        return PortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PortableRegistry.ON_INSTANCE_CID;
    }
    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1", this.systemId);
        out.writeUTF("3", this.instanceId);
        out.writeInt("5", this.accessMode);
        out.writeBoolean("6", this.initialized);
        out.writeBoolean("7", this.joined);
        out.writeDouble("9", this.balance);
        out.writeLong("15",this.timestamp);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.systemId = in.readUTF("1");
        this.instanceId = in.readUTF("3");
        this.accessMode = in.readInt("5");
        this.initialized = in.readBoolean("6");
        this.joined = in.readBoolean("7");
        this.balance = in.readDouble("9");
        this.timestamp = in.readLong("15");
    }
    @Override
    public String toString(){
        return "["+this.systemId+","+this.stub+"]["+"On Instance "+this.applicationId+","+this.instanceId+","+this.joined+"]";
    }


    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1", systemId);
        this.properties.put("2",stub);
        this.properties.put("3",initialized);
        this.properties.put("4",joined);
        this.properties.put("5",accessMode);
        this.properties.put("6",balance);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.systemId = (String)properties.get("1");
        this.stub = ((Number)properties.get("2")).intValue();
        this.initialized =(Boolean)properties.get("3");
        this.joined = (Boolean)properties.get("4");
        this.accessMode = ((Number)properties.get("5")).intValue();
        this.balance = ((Number)properties.get("6")).doubleValue();
    }
    public boolean initialized() {
        return this.initialized;
    }

    public void initialized(boolean initialized) {
        this.initialized = initialized;
    }
    public  boolean joined(){
        return this.joined;
    }
    public void joined(boolean joined) {
        this.joined = joined;
    }


    public void reset(double reset){
        this.balance = reset;
    }


    public boolean transact(double delta){
        if((balance+(delta)>=0)){
            balance  = balance+(delta);
            this.dataStore.update(this);
            return true;
        }
        else{
            return false;
        }
    }
    public synchronized int idle(boolean reset){
        this.idle = reset?0:(this.idle+1);
        return this.idle;
    }
    @Override
    public void onUpdate(){
        this.dataStore.update(this);
    }

}
