package com.tarantula.platform.service;

import com.icodesoftware.util.CipherUtil;
import com.tarantula.platform.AssociateObject;
import com.tarantula.platform.service.cluster.PortableRegistry;


public class PresenceKey extends AssociateObject {

    private String base64Key;

    public PresenceKey(){
        this.label = "presenceKey";
    }

    public PresenceKey(String associateId){
        this();
        this.oid = associateId;
    }
    @Override
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableRegistry.PRESENCE_KEY_CID;
    }


    public void base64key(String base64Key){
        this.base64Key = base64Key;
    }
    public byte[] toKey(){
        return CipherUtil.fromBase64Key(base64Key);
    }


    public boolean write(DataBuffer buffer){
        buffer.writeUTF8(base64Key);
        return true;
    }
    public boolean read(DataBuffer buffer) {
        this.base64Key = buffer.readUTF8();
        return true;
    }


}
