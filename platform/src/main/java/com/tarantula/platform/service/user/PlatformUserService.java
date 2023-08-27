package com.tarantula.platform.service.user;

import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.*;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.PresenceIndex;
import com.tarantula.platform.presence.Membership;
import com.tarantula.platform.presence.ThirdPartyLogin;
import com.tarantula.platform.presence.User;
import com.tarantula.platform.presence.UserAccount;
import com.tarantula.platform.service.metrics.AccessMetrics;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PlatformUserService implements UserService {

    private DataStore userDataStore;
    private DataStore presenceDataStore;
    private DataStore accountDataStore;
    private DataStore membershipDataStore;
    private DataStore accountIndexDataStore;

    private DataStore loginProviderDataStore;
    private TokenValidatorProvider tokenValidatorProvider;
    private TarantulaLogger logger;
    private int trialMaxUsersPerAccount = 10;
    private int subscribedMaxUsersPerAccount = 10;

    private MetricsListener metricsListener = (m,v)->{};

    @Override
    public Access createUser(OnAccess onAccess) {
        Access acc = new User((String) onAccess.property(OnAccess.LOGIN),(Boolean)onAccess.property(OnAccess.VALIDATED),(String) onAccess.property(OnAccess.VALIDATOR));
        acc.emailAddress((String)onAccess.property(OnAccess.EMAIL_ADDRESS));
        acc.id((Long)onAccess.property(OnAccess.SYSTEM_ID));
        String pwd = (String)onAccess.property(OnAccess.PASSWORD);
        String hash = tokenValidatorProvider.tokenValidator().hashPassword(pwd);
        acc.password(hash);
        acc.activated((Boolean)onAccess.property(OnAccess.ACTIVATED));
        acc.primary((Boolean)onAccess.property(OnAccess.PRIMARY_USER));
        if(!acc.primary()){
            if(onAccess.ownerKey()==null) throw new IllegalArgumentException("No owner for sub user");
            acc.ownerKey(onAccess.ownerKey());
        }
        acc.role((String)onAccess.property(OnAccess.ACCESS_CONTROL));
        if(!userDataStore.createIfAbsent(acc,false)) throw new RuntimeException("Failed to create user");
        PresenceIndex px = new PresenceIndex();
        px.id(acc.id());
        presenceDataStore.createIfAbsent(px,false);
        //this.metricsListener.onUpdated(AccessMetrics.ACCOUNT_USER_CREATION_COUNT,1);
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
        this.metricsListener.onUpdated(AccessMetrics.ACCOUNT_USER_CREATION_COUNT,1);
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
        boolean suc = userDataStore.update(user);
        return suc;
    }
    @Override
    public Account createAccount(Access access,Subscription subscription){
        if(!userDataStore.load(access)) throw new RuntimeException("No such user existed");
        if(!access.primary()) throw new RuntimeException("Only primary user can have an account");
        subscription.id(access.id());
        subscription.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        subscription.count(1);
        if(!membershipDataStore.createIfAbsent(subscription,false)){
            throw new RuntimeException("Subscription already existed");
        }
        UserAccount account = new UserAccount();
        account.id(access.id());
        account.trial(subscription.trial());
        account.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        if(!accountDataStore.createIfAbsent(account,false)){
            throw new RuntimeException("Account already existed");
        }
        //this.metricsListener.onUpdated(AccessMetrics.ACCOUNT_ACCOUNT_CREATION_COUNT,1);
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
        if(!membershipDataStore.load(membership)){
            throw new RuntimeException("no subscription existed");
        }
        LocalDateTime end = TimeUtil.fromUTCMilliseconds(membership.endTimestamp());
        membership.endTimestamp(TimeUtil.toUTCMilliseconds(end.plusMonths(durationMonth)));
        membership.count(1);
        membership.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        accountDataStore.update(account);
        this.metricsListener.onUpdated(AccessMetrics.ACCOUNT_SUBSCRIPTION_COUNT,1);
        return membership;
    }

    public Access loadUser(long systemId){
        User u = new User();
        u.id(systemId);
        u.dataStore(userDataStore);
        if(userDataStore.load(u)) return u;
        return null;
    }

    public Account loadAccount(Access access){
        Account account = new UserAccount();
        account.distributionKey(access.primary()?access.distributionKey():access.owner());
        account.dataStore(accountDataStore);
        if(accountDataStore.load(account)) return account;
        return null;
    }

    public List<Access> loadUsers(Access access){
        ArrayList<Access> alist = new ArrayList<>();
        IndexSet indexSet = new IndexSet();
        indexSet.distributionKey(access.primary()?access.distributionKey():access.owner());
        indexSet.label(Account.UserLabel);
        if(!accountIndexDataStore.load(indexSet)) return alist;
        indexSet.keySet().forEach((k)->{
            //Access u = loadUser(k);
            //if(u!=null){
                //alist.add(u);
            //}
        });
        return alist;
    }
    public List<Access> loadUsers(Account account){
        ArrayList<Access> alist = new ArrayList<>();
        IndexSet indexSet = new IndexSet();
        indexSet.distributionKey(account.distributionKey());
        indexSet.label(Account.UserLabel);
        if(!accountIndexDataStore.load(indexSet)) return alist;
        indexSet.keySet().forEach((k)->{
          //Access u = loadUser(k);
            //if(u!=null){
              //alist.add(u);
            //}
        });
        return alist;
    }

    public Subscription loadSubscription(Account account){
        Membership acc = new Membership();
        acc.distributionKey(account.distributionKey());
        if(membershipDataStore.load(acc)) return acc;
        return null;
    }
    public Subscription loadSubscription(Access access){
        Membership acc = new Membership();
        acc.distributionKey(access.primary()?access.distributionKey():access.owner());
        if(membershipDataStore.load(acc)) return acc;
        return null;
    }

    public <T extends Recoverable> T loadGameClusterIndex(Access access){
        IndexSet idx = new IndexSet();
        idx.distributionKey(access.primary()?access.distributionKey():access.owner());
        idx.label(Account.GameClusterLabel);
        idx.dataStore(accountIndexDataStore);
        accountIndexDataStore.createIfAbsent(idx,true);
        return (T)idx;
    }

    public LoginProvider loginProvider(String systemId){
        ThirdPartyLogin thirdPartyLogin = new ThirdPartyLogin();
        thirdPartyLogin.distributionKey(systemId);
        return loginProviderDataStore.load(thirdPartyLogin)?thirdPartyLogin:null;
    }
    public void createLoginProvider(LoginProvider loginProvider){
        this.loginProviderDataStore.createIfAbsent(loginProvider,false);
    }

    @Override
    public String name() {
        return UserService.NAME;
    }

    @Override
    public void setup(ServiceContext serviceContext){
        logger = JDKLogger.getLogger(PlatformUserService.class);
        tokenValidatorProvider = (TokenValidatorProvider) serviceContext.serviceProvider(TokenValidatorProvider.NAME);
        userDataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,User.DataStore);
        presenceDataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,Presence.DataStore);
        accountDataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,Account.DataStore);
        accountIndexDataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,Account.IndexDataStore);
        membershipDataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,Subscription.DataStore);
        loginProviderDataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,LoginProvider.DataStore);
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

    public void registerMetricsListener(MetricsListener metricsListener){
        if(metricsListener == null) return;
        this.metricsListener = metricsListener;
    }

    public void onUpdated(String mkey,double delta){
        metricsListener.onUpdated(mkey,delta);
    }

}
