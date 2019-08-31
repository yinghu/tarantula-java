package com.tarantula.test.datastore;


import com.tarantula.RecoverableFactory;
import com.tarantula.platform.leveling.XPLevel;

/**
 * Created by yinghu lu on 1/8/2017.
 */
public class XPLevelQuery implements RecoverableFactory<XPLevel> {

    public String distributionKey() {
        return null;
    }



    public XPLevel create() {
        return new XPLevel();
    }


    public int registryId() {
        return 0;
    }
    public String label(){
        return "XPLevel";
    }
    public boolean onEdge(){
        return false;
    }
}
