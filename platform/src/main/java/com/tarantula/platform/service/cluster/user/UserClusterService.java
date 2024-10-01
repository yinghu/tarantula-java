package com.tarantula.platform.service.cluster.user;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;
import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.LoginProvider;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.presence.ThirdPartyLogin;
import com.tarantula.platform.presence.User;
import com.tarantula.platform.service.cluster.tournament.TournamentClusterService;

import java.util.Properties;

public class UserClusterService implements ManagedService, RemoteService {

    private static TarantulaLogger log = JDKLogger.getLogger(TournamentClusterService.class);

    private NodeEngine nodeEngine;
    private TarantulaContext tarantulaContext;

    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        this.nodeEngine = nodeEngine;
        this.tarantulaContext = TarantulaContext.getInstance();
        log.warn("Start user cluster service");
    }

    @Override
    public void reset() {

    }

    @Override
    public void shutdown(boolean b) {
        log.warn("Shutting down user cluster service");
    }

    @Override
    public DistributedObject createDistributedObject(String objectName) {
        return new UserServiceProxy(objectName,this.nodeEngine,this);
    }

    @Override
    public void destroyDistributedObject(String objectName)  {
        log.warn(objectName+" destroyed");
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

        log.warn("USER DELETE: " + userDelete + " | LOGIN DELETE: " + loginDelete);

        return userDelete;
    }


    private DataStore userDataStore(){
        return this.tarantulaContext.dataStore(Distributable.DATA_SCOPE,User.DataStore);
    }

    private DataStore loginDataStore(){
        return this.tarantulaContext.dataStore(Distributable.DATA_SCOPE, LoginProvider.DataStore);
    }
}
