package com.tarantula.game;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Configurable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.IndexKey;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.util.Map;

public class Arena extends RecoverableObject implements Configurable, Portable {
    public int level;
    public int xp;
    public int capacity;
    public int joinsOnStart;
    public long duration;

    public JsonObject payload = new JsonObject();

    public Arena(){}
    public Arena(String bucket,String oid,int level){
        this.bucket = bucket;
        this.oid = oid;
        this.routingNumber = level;
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("name",name);
        this.properties.put("level",level);
        this.properties.put("xp",xp);
        this.properties.put("capacity",capacity);
        this.properties.put("joinsOnStart",joinsOnStart);
        this.properties.put("duration",duration);
        this.properties.put("index",index);
        this.properties.put("disabled",disabled);
        this.properties.put("payload",payload.toString());
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.name =(String)properties.get("name");
        this.level = ((Number)properties.get("level")).intValue();
        this.xp = ((Number)properties.get("xp")).intValue();
        this.capacity = ((Number)properties.getOrDefault("capacity",0)).intValue();
        this.joinsOnStart = ((Number)properties.getOrDefault("joinsOnStart",capacity)).intValue();
        this.duration = ((Number)properties.getOrDefault("duration",0)).longValue();
        this.index =(String)properties.get("index");
        this.disabled = (boolean)properties.get("disabled");
        this.payload = JsonUtil.parse((String) properties.getOrDefault("payload","{}"));
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.ARENA_CID;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeInt("1",level);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        level = portableReader.readInt("1");
    }

    @Override
    public String distributionKey() {
        if(this.bucket!=null&&this.oid!=null){
            return new StringBuffer(this.bucket).append(Recoverable.PATH_SEPARATOR).append(oid).append(Recoverable.PATH_SEPARATOR).append(routingNumber).toString();
        }
        else{
            return null;
        }
    }
    @Override
    public void distributionKey(String distributionKey) {
        String[] klist = distributionKey.split(Recoverable.PATH_SEPARATOR);
        this.bucket = klist[0];
        this.oid = klist[1];
        this.routingNumber = Integer.parseInt(klist[2]);
    }
    @Override
    public Key key(){
        return new IndexKey(this.bucket,this.oid,this.routingNumber);
    }
    public Arena copy(){
        Arena _cp = new Arena();
        _cp.level = this.level;
        _cp.xp = this.xp;
        _cp.capacity = this.capacity;
        _cp.joinsOnStart = this.joinsOnStart;
        _cp.duration = this.duration;
        _cp.name = this.name;
        _cp.index = this.index;
        return _cp;
    }
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name",name);
        jsonObject.addProperty("capacity",capacity);
        jsonObject.addProperty("duration",duration/60000);
        jsonObject.addProperty("xp",xp);
        jsonObject.addProperty("level",level);
        return jsonObject;
    }
    public boolean configureAndValidate(byte[] data){
        Map<String,Object> map = JsonUtil.toMap(data);
        this.disabled = (boolean)map.get("disabled");
        this.name = (String)map.get("name");
        this.capacity = ((Number)map.get("capacity")).intValue();
        this.duration = ((Number)map.get("duration")).intValue()*60000;
        this.xp = ((Number)map.get("xp")).intValue();
        if(map.containsKey("payload")){
            payload = ((JsonElement)map.get("payload")).getAsJsonObject();
        }
        return true;
    }
}
