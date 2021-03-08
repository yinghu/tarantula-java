package com.tarantula.game.service;

import com.icodesoftware.Recoverable;
import com.tarantula.game.GamePortableRegistry;
import com.tarantula.platform.CompositeKey;
import com.tarantula.platform.IndexSet;


public class GameServiceIndex extends IndexSet {

    public static final String TOURNAMENT ="tournament";
    public static final String TOURNAMENT_INSTANCE_JOIN ="join";
    public static final String TOURNAMENT_INSTANCE ="instance";


    public GameServiceIndex(){}
    public GameServiceIndex(String serviceName,String index){
        this.owner = serviceName;
        this.index = index;
    }
    public int getFactoryId() {
        return GamePortableRegistry.OID;
    }
    public int getClassId() {
        return GamePortableRegistry.GAME_SERVICE_INDEX_CID;
    }

    @Override
    public void distributionKey(String distributionKey){

    }
    public Recoverable.Key key(){
        return new CompositeKey(owner,index);
    }
}
