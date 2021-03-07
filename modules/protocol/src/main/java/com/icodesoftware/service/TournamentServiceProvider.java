package com.icodesoftware.service;

import com.icodesoftware.Tournament;

public interface TournamentServiceProvider extends ServiceProvider{

    void registerListener(Tournament.Listener listener);

    Tournament register(Tournament.Schedule schedule);

    boolean available(String tournamentId);
    Tournament.Entry join(String tournamentId,String systemId);
    Tournament.Entry score(String instanceId,String systemId, double delta);

}
