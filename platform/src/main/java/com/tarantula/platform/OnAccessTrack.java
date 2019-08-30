package com.tarantula.platform;

import com.tarantula.OnAccess;
import com.tarantula.platform.presence.UserPortableRegistry;
import com.tarantula.platform.util.SystemUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Updated by yinghu on 8/23/19.
 */
public class OnAccessTrack extends OnApplicationHeader implements OnAccess {

    protected String accessKey;
    protected String accessId;


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

    public void header(String header,String value){
        this.properties.put(header,value);
    }
    public String header(String header){
        return (String)this.properties.get(header);
    }


    public Map<String, String> header() {
        HashMap<String,String> _hs = new HashMap<>();
        this.properties.forEach((String s,Object o)->{
            if(o instanceof String){
                _hs.put(s,(String)o);
            }
        });
        return _hs;
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
