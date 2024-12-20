package com.tarantula.platform.service.cluster.presence;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;

import com.icodesoftware.LeaderBoard;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.LoginProvider;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.presence.ThirdPartyLogin;
import com.tarantula.platform.presence.User;


import java.util.Properties;


public class PresenceClusterService implements ManagedService, RemoteService {

    private static TarantulaLogger log = JDKLogger.getLogger(PresenceClusterService.class);

    private NodeEngine nodeEngine;
    private TarantulaContext tarantulaContext;

    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        this.nodeEngine = nodeEngine;
        this.tarantulaContext = TarantulaContext.getInstance();
        log.warn("Start presence cluster service");
    }

    @Override
    public void reset() {

    }

    @Override
    public void shutdown(boolean b) {

    }

    @Override
    public DistributedObject createDistributedObject(String objName) {
        return new DistributionPresenceServiceProxy(objName,nodeEngine,this);
    }

    @Override
    public void destroyDistributedObject(String s) {

    }

    public int onProfileSequence(String gameServiceName,String profileName){
        PlatformGameServiceProvider gameServiceProvider = (PlatformGameServiceProvider) this.tarantulaContext.serviceProvider(gameServiceName);
        return gameServiceProvider.presenceServiceProvider().onProfileSequence(profileName);
    }

    public void onUpdateLeaderBoard(String serviceName, LeaderBoard.Entry leaderBoardEntry){
        PlatformGameServiceProvider gameServiceProvider = (PlatformGameServiceProvider)this.tarantulaContext.serviceProvider(serviceName);
        gameServiceProvider.leaderBoardProvider().onLeaderBoardUpdated(leaderBoardEntry);
    }
    public byte[] onLoadLeaderBoard(String serviceName,String category,String classifier){
        PlatformGameServiceProvider gameServiceProvider = (PlatformGameServiceProvider)this.tarantulaContext.serviceProvider(serviceName);
        return gameServiceProvider.leaderBoardProvider().onLeaderBoardLoaded(category,classifier);
    }



    public boolean delete(long systemId){
        DataStore userDataStore = this.userDataStore();
        User user = new User();
        user.distributionId(systemId);
        boolean userDelete = userDataStore.delete(user);

        DataStore loginDataStore = this.loginDataStore();
        ThirdPartyLogin thirdPartyLogin = new ThirdPartyLogin();
        thirdPartyLogin.distributionId(systemId);
        boolean loginDelete = loginDataStore.delete(thirdPartyLogin);

        return userDelete && loginDelete;
    }


    private DataStore userDataStore(){
        return this.tarantulaContext.dataStore(Distributable.DATA_SCOPE,User.DataStore);
    }

    private DataStore loginDataStore(){
        return this.tarantulaContext.dataStore(Distributable.DATA_SCOPE, LoginProvider.DataStore);
    }

}
