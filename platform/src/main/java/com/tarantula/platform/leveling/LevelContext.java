package com.tarantula.platform.leveling;

import com.tarantula.XP;
import com.tarantula.platform.ResponseHeader;
import java.util.List;


/**
 * Developer: YINGHU LU
 * Updated: 8/24/19
 * Time: 1:13 PM
 */
public class LevelContext extends ResponseHeader {

    public XPLevel level;
    public List<XP> xp;

    public LevelContext(){
        this.label = "level";
        this.code = 200;
        this.successful=true;
    }
}
