package com.tarantula.game;

import com.tarantula.Recoverable;
import com.tarantula.platform.OnBalanceTrack;

/**
 * Created by yinghu lu on 2/28/2019.
 */
public class GameComponent extends OnBalanceTrack {
    public String componentId;
    public int subscript;
    public boolean broadcasting = true;

    public GameComponent(){}

    public void duplicate(String systemId,String componentId,String name,String label,int subscript,boolean broadcasting){
        this.systemId = systemId;
        this.componentId = componentId;
        this.name = name;
        this.label = label;
        this.subscript = subscript;
        this.broadcasting = broadcasting;
    }


    public void distributionKey(String distributionKey){
        String[] klist = distributionKey.split(Recoverable.PATH_SEPARATOR);
        this.bucket = klist[0];
        this.oid = klist[1];
    }
}
