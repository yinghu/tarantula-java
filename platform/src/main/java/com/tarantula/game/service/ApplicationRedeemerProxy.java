package com.tarantula.game.service;

import com.icodesoftware.Descriptor;
import com.icodesoftware.Session;
import com.icodesoftware.protocol.ApplicationResource;
import com.icodesoftware.service.ApplicationPreSetup;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.inventory.ApplicationRedeemer;

public class ApplicationRedeemerProxy implements ApplicationResource.Redeemer {

    private final long systemId;
    private final GameCluster gameCluster;
    public ApplicationRedeemerProxy(Session session,GameCluster gameCluster){
        this.systemId = session.distributionId();
        this.gameCluster = gameCluster;
    }
    @Override
    public void redeem(ApplicationPreSetup applicationPreSetup, ApplicationResource resource) {
        Descriptor app = gameCluster.application(resource.configurationTypeId());
        ApplicationRedeemer redeemer = new ApplicationRedeemer(systemId,applicationPreSetup);
        redeemer.distributionKey(resource.distributionKey());
        if(!applicationPreSetup.load(app,redeemer)) throw new RuntimeException("Resource not existed : "+resource.distributionKey());
        redeemer.redeem();
    }
}
