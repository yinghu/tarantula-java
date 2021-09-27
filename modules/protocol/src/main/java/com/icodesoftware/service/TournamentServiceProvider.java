package com.icodesoftware.service;

import com.icodesoftware.Tournament;

import java.util.List;

public interface TournamentServiceProvider extends ServiceProvider{

    String registerTournamentListener(Tournament.Listener listener);
    void unregisterTournamentListener(String registryKey);

    Tournament register(Tournament.Schedule schedule);

    boolean available(String tournamentId);
    Tournament.Instance join(String tournamentId,String systemId);
    Tournament.Entry score(String instanceId,String systemId, double delta);
    Tournament.Entry configure(String instanceId,String systemId,byte[] payload);
    void leave(String instanceId,String systemId);

    Tournament.RaceBoard list(String instanceId);

    List<Tournament.History> history(String systemId);
}
