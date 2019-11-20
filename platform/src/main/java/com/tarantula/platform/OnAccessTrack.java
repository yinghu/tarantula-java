package com.tarantula.platform;

import com.tarantula.OnAccess;
import com.tarantula.Property;
import com.tarantula.platform.presence.UserPortableRegistry;
import com.tarantula.platform.util.SystemUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * Updated by yinghu on 8/23/19.
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

    public void property(String header,String value){
        this.properties.put(header,value);
    }
    public String property(String header){
        return (String)this.properties.get(header);
    }
    public byte[] payload() {
        return this.payload;
    }

    public void payload(byte[] payload) {
        this.payload = payload;
    }

    public List<Property> list() {
        List<Property> hm = new ArrayList<>();
        this.properties.forEach((String s,Object o)->{
            if(o instanceof String){
                hm.add(new DistributedProperty(s,(String)o));
            }
        });
        return hm;
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
