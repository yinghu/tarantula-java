package com.tarantula.platform.service;


import com.tarantula.Descriptor;
import com.tarantula.ServiceProvider;

public interface DeployService extends ServiceProvider {

    String NAME = "DeployService";

    Batch query(int registryId,String[] params);

    Batch query(String batchId,int count);

    void recover(String destination,String registerId,boolean fullBackup);

    String addLobby(Descriptor lobby);
    String enableLobby(String typeId,boolean enabled);
    String addApplication(Descriptor application);
    String resetModule(String lobbyId,Descriptor descriptor);
    String enableApplication(String applicationId,boolean enabled);
}
