package com.icodesoftware.protocol;

import com.icodesoftware.*;

public interface GameContext extends Context {

    GameServiceProxy gameServiceProxy(short serviceId);
    Transaction transaction();
}
