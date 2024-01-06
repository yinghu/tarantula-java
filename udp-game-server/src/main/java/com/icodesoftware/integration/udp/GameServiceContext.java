package com.icodesoftware.integration.udp;

import com.icodesoftware.*;
import com.icodesoftware.protocol.GameContext;
import com.icodesoftware.service.ApplicationSchema;
import com.icodesoftware.service.TokenValidatorProvider;

import java.util.concurrent.ScheduledFuture;

public class GameServiceContext implements GameContext {
    @Override
    public ScheduledFuture<?> schedule(SchedulingTask task) {
        return null;
    }

    @Override
    public void log(String message, int level) {

    }

    @Override
    public void log(String message, Exception error, int level) {

    }

    @Override
    public ApplicationSchema applicationSchema() {
        return null;
    }

    @Override
    public TokenValidatorProvider.AuthVendor authorVendor(String name) {
        return null;
    }

    @Override
    public void registerTournamentListener(Tournament.Listener listener) {

    }

    @Override
    public void recoverableRegistry(RecoverableListener recoverableListener) {

    }

    @Override
    public void onMetrics(String category, double delta) {

    }

    @Override
    public Statistics statistics(Session session) {
        return null;
    }

    @Override
    public Rating rating(Session session) {
        return null;
    }

    @Override
    public Achievement achievement(Session session) {
        return null;
    }
}
