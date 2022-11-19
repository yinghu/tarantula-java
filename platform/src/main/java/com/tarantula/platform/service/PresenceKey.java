package com.tarantula.platform.service;

import com.icodesoftware.util.CipherUtil;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.service.cluster.PortableRegistry;


public class PresenceKey extends RecoverableObject {

    private final static String _KEY = "_key";

    public PresenceKey(){

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
        return new AssociateKey(this.bucket,this.oid, "presenceKey");
    }

    public void base64key(String base64Key){
        properties.put(_KEY,base64Key);
    }
    public byte[] toKey(){
        return CipherUtil.fromBase64Key((String)properties.get(_KEY));
    }


}
