package com.icodesoftware.protocol;

import com.icodesoftware.*;

import com.icodesoftware.service.ApplicationSchema;
import com.icodesoftware.service.TokenValidatorProvider;

public interface GameContext extends Context {


    ApplicationSchema applicationSchema();

    TokenValidatorProvider.AuthVendor authorVendor(String name);

    void registerTournamentListener(Tournament.Listener listener);
    void recoverableRegistry(RecoverableListener recoverableListener);

    void onMetrics(String category,double delta);

    Statistics statistics(Session session);
    Rating rating(Session session);

    Achievement achievement(Session session);

    ApplicationResource.Redeemer redeemer(Session session);
}
