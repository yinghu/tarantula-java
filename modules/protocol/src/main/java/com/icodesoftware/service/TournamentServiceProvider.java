package com.icodesoftware.service;

import com.icodesoftware.Tournament;

import java.util.List;

public interface TournamentServiceProvider extends ConfigurationServiceProvider{

    //String registerListener(Tournament.Listener listener);

    Tournament register(Tournament.Schedule schedule);

    boolean available(String tournamentId);
    Tournament.Instance join(String tournamentId,String systemId);
    Tournament.Entry score(String instanceId,String systemId, double delta);
    List<Tournament.Entry> tournamentEntries(String instanceId);
}
