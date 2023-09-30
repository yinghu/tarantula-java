package com.icodesoftware.protocol;

import com.icodesoftware.*;
import com.icodesoftware.service.ApplicationSchema;

public interface GameContext extends Context {

    GameServiceProxy gameServiceProxy(short serviceId);

    ApplicationSchema applicationSchema();

    GameServiceProvider gameServiceProvider();
}
