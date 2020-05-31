package com.tarantula.platform.service;


import com.tarantula.Descriptor;
import com.tarantula.OnView;
import com.tarantula.admin.GameCluster;

public interface DeployService extends ServiceProvider {

    String NAME = "DeployService";

    Batch query(int registryId,String[] params);

    Batch query(String batchId,int count);

    void recover(String destination,String registerId,boolean fullBackup);

    boolean addLobby(Descriptor lobby);
    boolean enableLobby(String typeId,boolean enabled);
    String addApplication(Descriptor application);
    boolean addView(OnView view);
    boolean resetModule(String lobbyId,Descriptor descriptor);
    String enableApplication(String applicationId,boolean enabled);

    GameCluster createGameCluster(String owner,String name,String plan);
}
