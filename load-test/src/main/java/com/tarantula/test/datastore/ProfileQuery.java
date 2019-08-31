package com.tarantula.test.datastore;


import com.tarantula.Profile;
import com.tarantula.RecoverableFactory;
import com.tarantula.platform.presence.ProfileTrack;
import com.tarantula.platform.presence.UserPortableRegistry;

/**
 * Updated by yinghu on 3/30/2018.
 */
public class ProfileQuery implements RecoverableFactory<Profile> {

    private String distributionKey;
    public ProfileQuery(){

    }

    public Profile create() {
        return new ProfileTrack();
    }


    public String distributionKey() {
        return this.distributionKey;
    }


    public  int registryId(){
        return UserPortableRegistry.PROFILE_CID;
    }

    public String label(){
        return "Profile";
    }
    public boolean onEdge(){
        return false;
    }


}
