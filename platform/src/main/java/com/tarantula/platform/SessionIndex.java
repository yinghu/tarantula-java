package com.tarantula.platform;

import com.google.gson.JsonObject;
import com.icodesoftware.protocol.session.OnSessionTrack;
import com.tarantula.platform.service.cluster.PortableRegistry;


public class SessionIndex extends OnSessionTrack {

    public SessionIndex(){
       super();
    }

    public boolean write(DataBuffer buffer){
        buffer.writeLong(timestamp);
        buffer.writeBoolean(disabled);
        return true;
    }
    public boolean read(DataBuffer buffer) {
        this.timestamp = buffer.readLong();
        this.disabled = buffer.readBoolean();
        return true;
    }
    @Override
    public JsonObject toJson() {
        JsonObject jp = new JsonObject();
        jp.addProperty("distributionId",distributionKey());
        jp.addProperty("timestamp",Long.toString(timestamp));
        return jp;
    }

    @Override
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableRegistry.ON_SESSION_CID;
    }

}
