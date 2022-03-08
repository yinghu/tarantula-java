package com.tarantula.platform.service.user;

import com.icodesoftware.*;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.service.UserService;
import com.tarantula.platform.PresenceIndex;
import com.tarantula.platform.presence.User;

public class PlatformUserService implements UserService {

    private DataStore userDataStore;
    private DataStore presenceDataStore;
    private TokenValidatorProvider tokenValidatorProvider;
    private TarantulaLogger logger;
    @Override
    public Access createUser(OnAccess onAccess) {
        Access acc = new User((String) onAccess.property(OnAccess.LOGIN),(Boolean)onAccess.property(OnAccess.VALIDATED),(String) onAccess.property(OnAccess.VALIDATOR));
        acc.distributionKey((String)onAccess.property(OnAccess.SYSTEM_ID));
        String pwd = (String)onAccess.property(OnAccess.PASSWORD);
        String hash = tokenValidatorProvider.tokenValidator().hashPassword(pwd);
        acc.password(hash);
        acc.activated((Boolean)onAccess.property(OnAccess.ACTIVATED));
        acc.primary((Boolean)onAccess.property(OnAccess.PRIMARY_USER));
        if(!acc.primary()){
            acc.owner(onAccess.owner());
        }
        acc.role((String)onAccess.property(OnAccess.ACCESS_CONTROL));
        if(userDataStore.create(acc)){
            PresenceIndex px = new PresenceIndex((Double)onAccess.property(OnAccess.BALANCE));
            px.distributionKey(acc.distributionKey());
            presenceDataStore.create(px);
        }
        return acc;
    }

    @Override
    public boolean createAccount(Account account) {
        return false;
    }

    @Override
    public String name() {
        return UserService.NAME;
    }

    @Override
    public void setup(ServiceContext serviceContext){
        logger = serviceContext.logger(PlatformUserService.class);
        tokenValidatorProvider = (TokenValidatorProvider) serviceContext.serviceProvider(TokenValidatorProvider.NAME);
        userDataStore = serviceContext.dataStore(User.DataStore,serviceContext.partitionNumber());
        presenceDataStore = serviceContext.dataStore(Presence.DataStore,serviceContext.partitionNumber());
        logger.warn("User service started");
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
