package com.tarantula.test.datastore;

import com.tarantula.Access;
import com.tarantula.RecoverableFactory;
import com.tarantula.platform.presence.AccessTrack;
import com.tarantula.platform.presence.UserPortableRegistry;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.util.Arrays;

/**
 * Updated by yinghu on 3/30/2018.
 */
public class AccessQuery implements RecoverableFactory<Access> {

    private String filter;
    public AccessQuery(){

    }
    public AccessQuery(String key){
        this.filter = key;
    }



    public Access create() {
        return new AccessTrack();
    }


    public String distributionKey() {
        return this.filter;
    }


    public  int registryId(){
        return UserPortableRegistry.ACCESS_CID;
    }

    public String label(){
        return "Access";
    }



}
