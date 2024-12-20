package com.tarantula.platform.presence.saves;

import com.google.gson.JsonObject;
import com.icodesoftware.Recoverable;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.OnApplicationHeader;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class SaveRevisionInfo extends OnApplicationHeader {

    public int clientRevisionNumber;
    public String deviceId;


    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.SAVE_REVISION_INFO_CID;
    }

    @Override
    public boolean write(DataBuffer buffer){
        buffer.writeInt(clientRevisionNumber);
        buffer.writeUTF8(deviceId);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        clientRevisionNumber = buffer.readInt();
        deviceId = buffer.readUTF8();
        return true;
    }

    @Override
    public JsonObject toJson() {
        JsonObject resp = new JsonObject();
        resp.addProperty("Successful",true);
        resp.addProperty("RevisionNumber",clientRevisionNumber);
        resp.addProperty("DeviceId",deviceId);
        resp.addProperty("Name",name);
        return resp;
    }

    @Override
    public byte[] toBinary() {
        return toJson().toString().getBytes();
    }

    @Override
    public boolean readKey(Recoverable.DataBuffer buffer){
        distributionId = buffer.readLong();
        name = buffer.readUTF8();
        return true;
    }
    @Override
    public boolean writeKey(Recoverable.DataBuffer buffer){
        if(distributionId==0 && name ==null) return false;
        buffer.writeLong(distributionId);
        buffer.writeUTF8(name);
        return true;
    }

    @Override
    public Key key() {
        return new AssociateKey(distributionId,name);
    }
}
