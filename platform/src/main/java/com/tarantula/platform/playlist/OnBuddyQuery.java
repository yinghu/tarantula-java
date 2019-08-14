package com.tarantula.platform.playlist;

import com.tarantula.OnPlay;
import com.tarantula.RecoverableFactory;

/**
 * Created by yinghu lu on 4/24/2018.
 */
public class OnBuddyQuery implements RecoverableFactory<OnPlay> {

    String playListKey;

    public OnBuddyQuery(String key){
        this.playListKey = key;
    }

    public OnPlay create() {
        return new OnBuddy();
    }

    public int registryId() {
        return 0;
    }


    public String distributionKey(){
        return this.playListKey;
    }
    public String label(){
        return "ROB";
    }
    public boolean onEdge(){
        return true;
    }
}
