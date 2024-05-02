package com.tarantula.platform.presence;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;

public class ProfileNameSequenceQuery implements RecoverableFactory<ProfileNameSequence> {

    private long gameClusterId;


    public ProfileNameSequenceQuery(long gameClusterId){
        this.gameClusterId = gameClusterId;
    }

    public ProfileNameSequence create() {
        return new ProfileNameSequence();
    }


    public String label(){
        return ProfileNameSequence.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return new SnowflakeKey(gameClusterId);
    }
}
