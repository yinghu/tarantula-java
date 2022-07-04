package com.tarantula.platform.service.user;

import com.icodesoftware.*;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.service.UserService;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.IndexSet;
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
    private DataStore accountIndexDataStore;
    private TokenValidatorProvider tokenValidatorProvider;
    private TarantulaLogger logger;
    private int trialMaxUsersPerAccount = 10;
    private int subscribedMaxUsersPerAccount = 10;
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
        if(!userDataStore.create(acc)) throw new RuntimeException("Failed to create user");
        PresenceIndex px = new PresenceIndex((Double)onAccess.property(OnAccess.BALANCE));
        px.distributionKey(acc.distributionKey());
        presenceDataStore.create(px);
        return acc;
    }
    public Access createUser(String accountId,OnAccess access){
        Account account = new UserAccount();
        account.distributionKey(accountId);
        if(!accountDataStore.load(account)) throw new RuntimeException("Account not existed");
        if(!account.trial() && !account.subscribed()) throw new RuntimeException("Account expired");
        int maxUsersPerAccount = account.trial()?trialMaxUsersPerAccount:subscribedMaxUsersPerAccount;
        if(account.userCount(0)> maxUsersPerAccount) throw new RuntimeException("over max user count");
        account.userCount(1);
        account.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        accountDataStore.update(account);
        access.owner(accountId);
        Access user = createUser(access);
        IndexSet idx = new IndexSet();
        idx.distributionKey(account.distributionKey());
        idx.label(Account.UserLabel);
        idx.addKey(user.distributionKey());
        if(!accountIndexDataStore.createIfAbsent(idx,true)){
            idx.addKey(user.distributionKey());//update on existing
            accountIndexDataStore.update(idx);
        }
        return user;
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
            account.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
            accountDataStore.update(account);
        }
        return account;
    }

    public Subscription subscribe(String accountId,int durationMonth){
        Access access = new User();
        access.distributionKey(accountId);
        if(!userDataStore.load(access)){
            throw new RuntimeException("no such user");
        }
        Account account = new UserAccount();
        account.distributionKey(access.primary()?access.distributionKey(): access.owner());
        if(!accountDataStore.load(account)){
            throw new RuntimeException("no such account");
        }
        account.trial(false);
        account.subscribed(true);
        Membership membership = new Membership();
        membership.distributionKey(account.distributionKey());
        membershipDataStore.load(membership);
        LocalDateTime end = TimeUtil.fromUTCMilliseconds(membership.endTimestamp());
        membership.endTimestamp(TimeUtil.toUTCMilliseconds(end.plusMonths(durationMonth)));
        membership.count(1);
        membership.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        membershipDataStore.update(membership);
        accountDataStore.update(account);
        return membership;
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
        accountIndexDataStore = serviceContext.dataStore(Account.IndexDataStore,serviceContext.partitionNumber());
        membershipDataStore = serviceContext.dataStore(Subscription.DataStore,serviceContext.partitionNumber());
        Configuration configuration = serviceContext.configuration("account-role-user-settings");
        trialMaxUsersPerAccount = ((Number)configuration.property("trialMaxUserCount")).intValue();
        subscribedMaxUsersPerAccount = ((Number)configuration.property("subscribedMaxUserCount")).intValue();
        logger.warn("User service started with max users per account ["+trialMaxUsersPerAccount+","+subscribedMaxUsersPerAccount+"]");
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
