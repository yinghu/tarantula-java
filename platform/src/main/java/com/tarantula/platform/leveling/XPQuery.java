package com.tarantula.platform.leveling;

import com.tarantula.RecoverableFactory;
import com.tarantula.XP;

/**
 * Updated 4/23/2018 yinghu
 */
public class XPQuery implements RecoverableFactory<XP> {

    String levelDistributionKey;


    public XPQuery(String levelDistributionKey){
        this.levelDistributionKey = levelDistributionKey;

    }

    public XP create() {
        return new XPGain();
    }


    public String distributionKey() {
        return levelDistributionKey;
    }

    public  int registryId(){
        return LevelingPortableRegistry.XP_CID;
    }

    public String label(){
        return "LXG";
    }

}
