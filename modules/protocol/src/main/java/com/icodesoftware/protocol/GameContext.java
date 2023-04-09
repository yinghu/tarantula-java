package com.icodesoftware.protocol;

import com.icodesoftware.*;

public interface GameContext extends Context {

    GameServiceProxy gameServiceProxy(short serviceId);

    default DataStore dataStore(String name){ return null;}
    default PostOffice postOffice(){ return null;}

}
