package com.icodesoftware.protocol;

import com.icodesoftware.Context;

public interface GameContext extends Context {

    GameServiceProxy gameServiceProxy(short serviceId);


}
