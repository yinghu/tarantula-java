package com.icodesoftware.service;


import com.icodesoftware.*;

public interface DeployService extends ServiceProvider {

    String NAME = "DeployService";

    Batch query(int registryId,String[] params);

    Batch query(String batchId,int count);

    boolean addLobby(Descriptor lobby);
    boolean addView(OnView onView);

    String addApplication(Descriptor application);
    boolean launchApplication(String typeId,String applicationId);
    boolean shutdownApplication(String typeId,String applicationId);
    boolean resetModule(Descriptor descriptor);


    //update lobby or application to set disabled as true/false
    String enableApplication(String applicationId,boolean enabled);
    boolean enableLobby(String typeId,boolean enabled);

    //GameCluster createGameCluster(String owner,String name);
    <T extends OnAccess> T createGameCluster(String owner,String name);
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
    byte[] load(String dataSource,byte[] key);

}
