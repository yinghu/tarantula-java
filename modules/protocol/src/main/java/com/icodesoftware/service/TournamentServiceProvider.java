package com.icodesoftware.service;

import com.icodesoftware.Tournament;


public interface TournamentServiceProvider extends ServiceProvider{

    void registerCreator(Tournament.Creator creator);

    Tournament register(String type,Tournament.Schedule schedule,Tournament.Listener listener);
    Tournament tournament(String type);
    void reload(TournamentServiceProvider.TournamentReload reload);

    interface TournamentReload{
        void onReload(Tournament tournament);
    }
}
