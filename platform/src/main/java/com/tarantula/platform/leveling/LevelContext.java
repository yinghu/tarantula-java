package com.tarantula.platform.leveling;


import com.tarantula.XP;
import com.tarantula.platform.ResponseHeader;

import java.util.List;
import java.util.Set;


/**
 * Developer: YINGHU LU
 * Updated: 3/5/2018
 * Time: 1:13 PM
 */
public class LevelContext extends ResponseHeader {

    public XPLevel level;
    public List<XP> xp;
    public Set<XPHeader> headers;

    public LevelContext(){
        this.code = 200;
        this.successful=true;
    }
}
