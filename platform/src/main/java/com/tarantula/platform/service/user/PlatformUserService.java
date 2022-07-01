package com.tarantula.platform.service.user;

import com.icodesoftware.*;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.service.UserService;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.PresenceIndex;
import com.tarantula.platform.presence.Membership;
import com.tarantula.platform.presence.User;
import com.tarantula.platform.presence.UserAccount;

import java.time.LocalDateTime;

public class PlatformUserService implements UserService {

    private DataStore userDataStore;
    private DataStore presenceDataStore;
    private DataStore accountDataStore;
    private DataStore membershipDataStore;
    private TokenValidatorProvider tokenValidatorProvider;
    private TarantulaLogger logger;
    @Override
    public Access createUser(OnAccess onAccess) {
        Access acc = new User((String) onAccess.property(OnAccess.LOGIN),(Boolean)onAccess.property(OnAccess.VALIDATED),(String) onAccess.property(OnAccess.VALIDATOR));
        acc.emailAddress((String)onAccess.property(OnAccess.EMAIL_ADDRESS));
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
    public boolean updateEmail(OnAccess access){
        String email = (String)access.property(OnAccess.EMAIL_ADDRESS);
        if(!email.contains("@")) return false;
        User user = new User();
        user.distributionKey((String) access.property(OnAccess.SYSTEM_ID));
        if(!userDataStore.load(user)) return false;
        user.emailAddress(email);
        return userDataStore.update(user);
    }
    public boolean changePassword(OnAccess access){
        User user = new User();
        user.distributionKey((String) access.property(OnAccess.SYSTEM_ID));
        if(!userDataStore.load(user)) return false;
        String pwd = (String)access.property(OnAccess.PASSWORD);
        String hash = tokenValidatorProvider.tokenValidator().hashPassword(pwd);
        user.password(hash);
        return userDataStore.update(user);
    }
    @Override
    public Account createOrUpdateAccount(Access access,Subscription subscription){
        Membership _existing = new Membership();
        _existing.distributionKey(access.distributionKey());
        if(membershipDataStore.load(_existing)){
            subscription.count(_existing.count(0));
        }
        else{
            subscription.count(1);
        }
        subscription.distributionKey(access.distributionKey());
        subscription.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        membershipDataStore.update(subscription);
        UserAccount account = new UserAccount();
        account.distributionKey(access.distributionKey());
        account.trial(subscription.trial());
        if(!accountDataStore.createIfAbsent(account,true)){
            account.trial(subscription.trial());
            accountDataStore.update(account);
        }
        return account;
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
        accountDataStore = serviceContext.dataStore(Account.DataStore,serviceContext.partitionNumber());
        membershipDataStore = serviceContext.dataStore(Subscription.DataStore,serviceContext.partitionNumber());
        logger.warn("User service started");
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
