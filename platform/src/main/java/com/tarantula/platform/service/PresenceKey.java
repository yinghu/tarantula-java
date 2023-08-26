package com.tarantula.platform.service;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.CipherUtil;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.service.cluster.PortableRegistry;


public class PresenceKey extends RecoverableObject {

    private String base64Key;
    public PresenceKey(){
        this.label = "presenceKey";
    }

    @Override
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableRegistry.PRESENCE_KEY_CID;
    }



    @Override
    public Key key(){
        return new AssociateKey(id, label);
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

    @Override
    public boolean readKey(Recoverable.DataBuffer buffer){
        id = buffer.readLong();
        label = buffer.readUTF8();
        return true;
    }
    @Override
    public boolean writeKey(Recoverable.DataBuffer buffer){
        if(id==0 && label ==null) return false;
        buffer.writeLong(id);
        buffer.writeUTF8(label);
        return true;
    }


}
