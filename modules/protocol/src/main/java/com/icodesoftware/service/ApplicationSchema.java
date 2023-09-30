package com.icodesoftware.service;

import com.icodesoftware.Configurable;
import com.icodesoftware.Descriptor;
import com.icodesoftware.Transaction;

public interface ApplicationSchema extends Configurable,Configurable.Listener<OnLobby>, Transaction.Listener {
    void setup(ServiceContext serviceContext);

    ApplicationPreSetup applicationPreSetup();

    Transaction transaction();

    String name();

    String typeId();

    String playMode();

     String lobbyType();

     String serviceType();

     String dataType();

     boolean tournamentEnabled();

     boolean dedicated();


     int maxLobbyCount();

     int maxZoneCount();

     int maxArenaCount();

     int maxDataSizeCount();

     int upgradeVersion();

     long accountId();

     Descriptor application(String category);
}
