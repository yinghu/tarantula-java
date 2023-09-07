package com.tarantula.platform.service;

import com.icodesoftware.util.CipherUtil;
import com.tarantula.platform.AssociateObject;
import com.tarantula.platform.service.cluster.PortableRegistry;


public class PresenceKey extends AssociateObject {

    private String clusterKey;
    private String tokenKey;

    public PresenceKey(){
        this.label = "presenceKey";
    }

    public PresenceKey(long associateId){
        this();
        this.distributionId = associateId;
    }
    @Override
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableRegistry.PRESENCE_KEY_CID;
    }


    public void clusterKey(String base64Key){
        this.clusterKey = base64Key;
    }

    public void tokenKey(String base64Key){
        this.tokenKey = base64Key;
    }
    public byte[] clusterKey(){
        return CipherUtil.fromBase64Key(clusterKey);
    }

    public byte[] tokenKey(){
        return CipherUtil.fromBase64Key(tokenKey);
    }

    public boolean write(DataBuffer buffer){
        buffer.writeUTF8(clusterKey);
        buffer.writeUTF8(tokenKey);
        return true;
    }
    public boolean read(DataBuffer buffer) {
        this.clusterKey = buffer.readUTF8();
        this.tokenKey = buffer.readUTF8();
        return true;
    }


}
