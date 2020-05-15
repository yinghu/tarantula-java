package com.tarantula.platform;

import com.tarantula.OnAccess;
import com.tarantula.platform.presence.UserPortableRegistry;
import com.tarantula.platform.util.SystemUtil;


/**
 * Updated by yinghu on 5/15/2020
 */
public class OnAccessTrack extends OnApplicationHeader implements OnAccess {

    protected String accessKey;
    protected String accessId;
    protected byte[] payload;

    public String accessKey(){
        return this.accessKey;
    }
    public void accessKey(String accessKey){
        this.accessKey = accessKey;
    }
    public String accessId(){
        return this.accessId;
    }
    public void accessId(String accessId){
        this.accessId = accessId;
    }

    public byte[] payload() {
        return this.payload;
    }

    public void payload(byte[] payload) {
        this.payload = payload;
    }


    public int getFactoryId() {
        return UserPortableRegistry.OID;
    }


    public int getClassId() {
        return UserPortableRegistry.ON_ACCESS_CID;
    }
    @Override
    public String toString(){
        return new String(SystemUtil.toJson(properties));
    }
}
