package com.icodesoftware.protocol;

import com.icodesoftware.*;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.service.ApplicationSchema;
import com.icodesoftware.service.TokenValidatorProvider;

import java.util.List;

public interface GameContext extends Context {

    GameServiceProxy gameServiceProxy(short serviceId);

    ApplicationSchema applicationSchema();

    GameServiceProvider gameServiceProvider();

    TokenValidatorProvider.AuthVendor authorVendor(String name);

    void registerTournamentListener(Tournament.Listener listener);
}
