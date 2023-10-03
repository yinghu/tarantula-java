package com.icodesoftware.protocol;

import com.icodesoftware.*;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.service.ApplicationSchema;

import java.util.List;

public interface GameContext extends Context {

    GameServiceProxy gameServiceProxy(short serviceId);

    ApplicationSchema applicationSchema();

    GameServiceProvider gameServiceProvider();

    //List<Inventory> inventory(long systemId, ApplicationPreSetup applicationPreSetup);
}
