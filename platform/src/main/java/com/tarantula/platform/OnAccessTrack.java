package com.tarantula.platform;

import com.icodesoftware.OnAccess;
import com.tarantula.platform.presence.UserPortableRegistry;
import com.icodesoftware.util.JsonUtil;
import java.util.Map;


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

    @Override
    public Map<String,Object> toMap(){
        super.toMap();
        this.properties.put("owner",this.owner);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        super.fromMap(properties);
        this.owner = (String)properties.get("owner");
    }

    public int getFactoryId() {
        return UserPortableRegistry.OID;
    }


    public int getClassId() {
        return UserPortableRegistry.ON_ACCESS_CID;
    }
    @Override
    public String toString(){
        return new String(JsonUtil.toJson(properties));
    }
}
