package com.tarantula.game;

import com.tarantula.platform.util.SystemUtil;

/**
 * Created by yinghu lu on 12/19/2018.
 */
public class CheckPoint extends GameComponent {

    protected long duration;

    public long duration(){
        return this.duration;
    }
    public void reset(){}
    public boolean onTurn(String systemId){
        return true;
    }
    public boolean check(){
        return SystemUtil.timeout(this.timestamp);
    }
}
