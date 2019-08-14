package com.tarantula.platform;

import com.tarantula.OnAccess;

import java.util.HashMap;
import java.util.Map;

/**
 * Updated by yinghu on 6/15/2018.
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

    @Override
    public int getFactoryId() {
        return 0;
    }

    @Override
    public int getClassId() {
        return 0;
    }


    @Override
    public String toString(){
        return "On Access ["+applicationId+","+instanceId+","+accessMode+"]";
    }
}
