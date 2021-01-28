package com.icodesoftware.service;

import com.icodesoftware.*;

public interface DeployService extends ServiceProvider {

    String NAME = "DeployService";

    boolean addLobby(Descriptor lobby,String publishingId);
    boolean addView(OnView onView);
    boolean updateView(OnView onView);

    String addApplication(Descriptor application);
    boolean launchApplication(String typeId,String applicationId);
    boolean shutdownApplication(String typeId,String applicationId);
    boolean resetModule(Descriptor descriptor);


    //update lobby or application to set disabled as true/false
    String enableApplication(String applicationId);
    boolean enableLobby(String typeId);
    String disableApplication(String applicationId);
    boolean disableLobby(String typeId);

    //GameCluster createGameCluster(String owner,String name);
    <T extends OnAccess> T createGameCluster(String owner,String name,String publishingId);
    boolean enableGameCluster(String gameClusterId);
    boolean disableGameCluster(String gameClusterId);

    boolean launchGameCluster(String gameClusterKey);
    boolean shutdownGameCluster(String gameClusterKey);

    void addServerPushEvent(Event serverPushEvent);
    void ackServerPushEvent(String serverId);
    void removeServerPushEvent(String serverId);

    void getConnection(String typeId,String lobbyTag,Session session);

    void syncServerPushEvent();
    boolean addServerPushEvent(String memberId,Event serverPushEvent);

    //distribute the module or view content in cluster
    boolean upload(String fileName,byte[] content);

    boolean launchModule(String typeId);
    boolean shutdownModule(String typeId);
    boolean updateModule(Descriptor descriptor);

    boolean sync(String key);

}
