package com.tarantula.platform.service;

import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.util.CipherUtil;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.*;
import com.tarantula.platform.presence.Membership;
import com.tarantula.platform.presence.User;
import com.tarantula.platform.presence.UserAccount;
import com.tarantula.platform.util.PresenceFetcher;
import com.tarantula.platform.util.SystemUtil;

import javax.crypto.Cipher;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SystemValidatorProvider implements TokenValidatorProvider {

    private static TarantulaLogger log = JDKLogger.getLogger(SystemValidatorProvider.class);

    private SystemValidator systemValidator;
    private int timeoutInMinutes;
    private int timeoutInSeconds;

    private ServiceContext serviceContext;
    private ConcurrentHashMap<String,Presence> pMap;
    private HashMap<String,Access.Role> rMap;
    private HashMap<String,AuthVendor> aMap;
    private DataStore pdataStore;//presence
    private DataStore udataStore;//user
    private DataStore adataStore;//account
    private DataStore idataStore;//account index
    private DataStore mdatastore;//membership

    private DataStore deployDataStore;

    private List<Access.Role> roleList;
    private MessageDigest _messageDigest;

    private ConcurrentHashMap<String, OnLobby> oMap;
    private DeploymentServiceProvider deploymentServiceProvider;
    private PresenceFetcher httpCaller;
    private boolean remotePresenceEnabled;
    private PresenceKey presenceKey;
    private Cipher encrypt;
    private Cipher remoteEncrypt;
    public MessageDigest messageDigest(){
        try{
            return (MessageDigest)this._messageDigest.clone();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public TokenValidator tokenValidator(){
        return systemValidator.tokenValidator();
    }
    public Presence presence(Session session){
        Presence presence = pMap.computeIfAbsent(session.systemId(),(k)->{
            PresenceIndex px = new PresenceIndex();
            px.distributionKey(session.systemId());
            if(!pdataStore.load(px)) return null;
            px.dataStore(pdataStore);
            px.registerEventService(this.serviceContext.eventService(Distributable.INTEGRATION_SCOPE));
            return px;
        });
        if(presence==null&&remotePresenceEnabled){
            log.warn("Fetching presence from presence service ...");
            OnSession onSession = httpCaller.presence(session.trackId());
            PresenceIndex px = new PresenceIndex(onSession.stub(),onSession.balance());
            px.distributionKey(onSession.systemId());
            pdataStore.update(px);
            px.dataStore(pdataStore);
            px.registerEventService(this.serviceContext.eventService(Distributable.INTEGRATION_SCOPE));
            pMap.put(session.systemId(),px);
            return px;
        }
        return presence;
    }
    public Presence presence(String systemId){
        return pMap.computeIfAbsent(systemId,(k)->{
            PresenceIndex px = new PresenceIndex();
            px.distributionKey(systemId);
            pdataStore.load(px);
            px.dataStore(pdataStore);
            px.registerEventService(this.serviceContext.eventService(Distributable.INTEGRATION_SCOPE));
            return px;
        });
    }
    public byte[] key(){
        return presenceKey.key;
    }
    public boolean enablePresenceService(String root,String password,String presenceServiceHost){
        try {
            httpCaller = new PresenceFetcher(presenceServiceHost);
            httpCaller._init();
            OnSession onSession = httpCaller.login(root,password);
            byte[] key = httpCaller.presenceKey(onSession.token());
            remoteEncrypt = CipherUtil.encrypt(key);
            this.remotePresenceEnabled = true;
            return true;
        }catch (Exception ex){
            log.error("error",ex);
            return false;
        }
    }
    public  void disablePresenceService(){
        this.remotePresenceEnabled = false;
    }
    public byte[] encrypt(byte[] data){
        try{
            return encrypt.doFinal(data);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public byte[] encryptFromRemoteKey(byte[] data){
        try{
            return remoteEncrypt.doFinal(data);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public void offSession(String systemId){
        Presence presence = pMap.remove(systemId);
        if(presence!=null){
            presence.disabled(true);
            presence.update();
        }
    }
    public void timeout(int minutes,int seconds){
        this.timeoutInMinutes = minutes;
        this.timeoutInSeconds = seconds;
    }

    public String validateAccessKey(String accessKey){
        String[] sp = accessKey.split("-");
        AccessKey ck = new AccessKey();
        ck.distributionKey(sp[0]);
        if(!deployDataStore.load(ck) || ck.disabled()) return null;
        long stmp = ck.timestamp();//((Number)ck.property(AccessKey.TIMESTAMP)).longValue();
        String label = ck.typeId();//(String)ck.property(AccessKey.KEY_LABEL);
        return SystemUtil.validAccessKey(messageDigest(),accessKey,label,stmp)?label:null;
    }
    public String createAccessKey(String label){
        AccessKey ck = new AccessKey();
        long stmp = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
        ck.timestamp(stmp);//property(AccessKey.TIMESTAMP,stmp);
        ck.typeId(label);//property(AccessKey.KEY_LABEL,label);
        if(deployDataStore.create(ck)){
            return SystemUtil.accessKey(messageDigest(),label,ck.distributionKey(),stmp);
        }
        return null;
    }
    public void revokeAccessKey(String accessKey){
        String[] sp = accessKey.split("-");
        AccessKey ck = new AccessKey();
        ck.distributionKey(sp[0]);
        if(!deployDataStore.load(ck)) return;
        ck.disabled(true);
        deployDataStore.update(ck);
    }
    public String hashJoinTicket(String roomId,String systemId){
        return SystemUtil.hashPassword(messageDigest(),roomId+"_"+systemId);
    }
    public boolean validHash(String roomId,String systemId,String hash){
        return SystemUtil.hashPassword(messageDigest(),roomId+"_"+systemId).equals(hash);
    }

    public <T extends OnAccess> T validateGameClusterAccessKey(String accessKey){
        String[] sp = accessKey.split("-");
        AccessKey akey = new AccessKey();
        akey.distributionKey(sp[0]);
        if(!this.deployDataStore.load(akey)|| akey.disabled()) return null;
        GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(akey.index());
        if(gameCluster==null) return null;
        String validLobby = (String)gameCluster.property(GameCluster.GAME_LOBBY);
        if(SystemUtil.validAccessKey(messageDigest(),accessKey,validLobby,akey.timestamp())) return (T)gameCluster;
        return null;
    }
    public String createGameClusterAccessKey(String gameClusterId){
        GameCluster gc = this.deploymentServiceProvider.gameCluster(gameClusterId);
        long stmp =TimeUtil.toUTCMilliseconds(LocalDateTime.now());
        AccessKey accessKey = new AccessKey();
        accessKey.typeId((String)gc.property(GameCluster.GAME_LOBBY));
        accessKey.timestamp(stmp);
        accessKey.owner(gameClusterId);
        if(!this.deployDataStore.create(accessKey)) return null;
        //gc.property(GameCluster.TIMESTAMP,stmp);
        //gc.update();
        return SystemUtil.accessKey(messageDigest(),accessKey.typeId(),accessKey.distributionKey(),stmp);
    }
    public List<String> gameClusterAccessKeyList(String gameClusterId){
        AccessKeyQuery query = new AccessKeyQuery(gameClusterId);
        ArrayList<String> keys = new ArrayList<>();
        deployDataStore.list(query,accessKey -> {
            if(!accessKey.disabled()) keys.add(accessKey.distributionKey());
            return true;
        });
        return keys;
    }

    public String ticket(String key,int stub,int duration){
        return SystemUtil.ticket(messageDigest(),key,stub,duration,"");
    }
    public boolean validateTicket(String key,int stub,String ticket){
        return SystemUtil.validTicket(messageDigest(),key,stub,ticket)!=null;
    }
    public List<Access.Role> list(){
        return roleList;
    }
    public Access.Role role(String systemId){
        if(systemId==null){
            return rMap.get("player");
        }
        Access acc = new User();
        acc.distributionKey(systemId);
        if(udataStore.load(acc)){
            return rMap.get(acc.role());
        }
        else{
            return rMap.get("player");
        }
    }
    @Override
    public String name() {
        return TokenValidatorProvider.NAME;
    }
    public AuthVendor authVendor(String name){
        return aMap.get(name);
    }
    public void onCheck(OnLobby onLobby){
        Subscription subscription = new Membership();
        subscription.distributionKey(onLobby.subscriptionId());
        if(mdatastore.load(subscription)) {
            oMap.put(onLobby.typeId(), onLobby);
            log.warn(onLobby+ " has been monitored at expiration time ->" + TimeUtil.fromUTCMilliseconds(subscription.endTimestamp()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
    }
    public boolean checkSubscription(String systemId){
        Subscription subscription = new Membership();
        subscription.distributionKey(systemId);
        if(!this.mdatastore.load(subscription)){
            return false;
        }
        LocalDateTime end = TimeUtil.fromUTCMilliseconds(subscription.endTimestamp());
        return end.isAfter(LocalDateTime.now());
    }
    public int updateSubscription(String systemId,int months){
        Subscription subscription = new Membership();
        subscription.distributionKey(systemId);
        if(!this.mdatastore.load(subscription)){
            return 0;
        }
        LocalDateTime end = TimeUtil.fromUTCMilliseconds(subscription.endTimestamp());
        subscription.endTimestamp(TimeUtil.toUTCMilliseconds(end.plusMonths(months)));
        subscription.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        int cnt = subscription.count(1);
        this.mdatastore.update(subscription);
        boolean suc = end.isAfter(LocalDateTime.now());
        Account acc = new UserAccount();
        acc.distributionKey(systemId);
        if(adataStore.load(acc)){
            acc.trial(false);
            acc.subscribed(suc);
            adataStore.update(acc);
        }
        return cnt;
    }
    public void atMidnight(){
        ArrayList<String> rlist = new ArrayList<>();
        LocalDateTime _curr = LocalDateTime.now();
        oMap.forEach((k,o)->{
            Subscription subscription = new Membership();
            subscription.distributionKey(o.subscriptionId());
            if(this.mdatastore.load(subscription)){
                LocalDateTime end = TimeUtil.fromUTCMilliseconds(subscription.endTimestamp());
                if(end.isBefore(_curr)){
                    deploymentServiceProvider.shutdownModule(o.typeId());
                    rlist.add(k);
                }
            }else{
                deploymentServiceProvider.shutdownModule(o.typeId());
                rlist.add(k);
            }
        });
        rlist.forEach((k)->{
            OnLobby o = oMap.remove(k);
            GameCluster g = new GameCluster();
            g.distributionKey(o.gameClusterId());
            if(deployDataStore.update(g)){
                g.property(GameCluster.DISABLED,true);
                deployDataStore.update(g);
            }
            Account acc = new UserAccount();
            acc.distributionKey(o.subscriptionId());
            if(adataStore.load(acc)){
                acc.trial(false);
                acc.subscribed(false);
                adataStore.update(acc);
            }
        });
    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.deploymentServiceProvider = serviceContext.deploymentServiceProvider();
        this.pdataStore =  this.serviceContext.dataStore(Presence.DataStore,this.serviceContext.partitionNumber());
        this.udataStore =  this.serviceContext.dataStore(Access.DataStore,this.serviceContext.partitionNumber());
        this.adataStore =  this.serviceContext.dataStore(Account.DataStore,this.serviceContext.partitionNumber());
        this.idataStore = this.serviceContext.dataStore(Account.IndexDataStore,this.serviceContext.partitionNumber());
        this.mdatastore =  this.serviceContext.dataStore(Subscription.DataStore,this.serviceContext.partitionNumber());
        this.deployDataStore = this.serviceContext.dataStore(DeploymentServiceProvider.DEPLOY_DATA_STORE,this.serviceContext.partitionNumber());
        oMap = new ConcurrentHashMap<>();
        AuthVendor google = this.serviceContext.authVendor(OnAccess.GOOGLE);
        if(google!=null){
            google.registerMetricsLister(this.deploymentServiceProvider);
            google.setup(serviceContext);
            aMap.put(OnAccess.GOOGLE,(google));
        }
        AuthVendor facebook = this.serviceContext.authVendor(OnAccess.FACEBOOK);
        if(facebook!=null){
            facebook.registerMetricsLister(this.deploymentServiceProvider);
            facebook.setup(serviceContext);
            aMap.put(OnAccess.FACEBOOK,facebook);
        }
        AuthVendor appleStore = this.serviceContext.authVendor(OnAccess.APPLE_STORE);
        if(appleStore!=null){
            appleStore.registerMetricsLister(this.deploymentServiceProvider);
            appleStore.setup(serviceContext);
            aMap.put(OnAccess.APPLE_STORE,appleStore);
        }
        AuthVendor gameCenter = this.serviceContext.authVendor(OnAccess.GAME_CENTER);
        if(gameCenter!=null){
            gameCenter.registerMetricsLister(this.deploymentServiceProvider);
            gameCenter.setup(serviceContext);
            aMap.put(OnAccess.GAME_CENTER,gameCenter);
        }
        AuthVendor mockStore = this.serviceContext.authVendor(OnAccess.MOCK_STORE);
        if(mockStore!=null){
            mockStore.registerMetricsLister(this.deploymentServiceProvider);
            mockStore.setup(serviceContext);
            aMap.put(OnAccess.MOCK_STORE,mockStore);
        }
        AuthVendor stripe = this.serviceContext.authVendor(OnAccess.STRIPE);
        if(stripe!=null){
            stripe.registerMetricsLister(this.deploymentServiceProvider);
            stripe.setup(serviceContext);
            aMap.put(OnAccess.STRIPE,(stripe));
        }
        AuthVendor googleStore = this.serviceContext.authVendor(OnAccess.GOOGLE_STORE);
        if(googleStore!=null){
            googleStore.registerMetricsLister(this.deploymentServiceProvider);
            googleStore.setup(serviceContext);
            aMap.put(OnAccess.GOOGLE_STORE,googleStore);
        }
    }

    @Override
    public void waitForData() {
        try{
            PresenceKey pKey = new PresenceKey();
            pKey.distributionKey(serviceContext.nodeId());
            if(!deployDataStore.load(pKey)){
                SecureRandom secureRandom = new SecureRandom();
                byte[] _key = new byte[DeploymentServiceProvider.KEY_SIZE];
                secureRandom.nextBytes(_key);
                pKey.key = _key;
                deployDataStore.update(pKey);
            }
            encrypt = CipherUtil.encrypt(pKey.key);
            this.presenceKey = pKey;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
        log.info("System validator provider started ["+serviceContext.nodeId()+"]");
    }

    @Override
    public void start() throws Exception {
        this.pMap = new ConcurrentHashMap<>();
        this.rMap = new HashMap<>();
        this.aMap = new HashMap<>();
        this.roleList = new ArrayList<>();
        rMap.put(AccessControl.root.name(),AccessControl.root);
        rMap.put(AccessControl.admin.name(),AccessControl.admin);
        rMap.put(AccessControl.account.name(),AccessControl.account);
        rMap.put(AccessControl.player.name(),AccessControl.player);
        roleList.add(AccessControl.admin);
        roleList.add(AccessControl.account);
        _messageDigest = MessageDigest.getInstance(MDA);
        this.systemValidator = new SystemValidator();
        this.systemValidator.systemValidatorProvider(this);
        this.systemValidator.timeout(this.timeoutInMinutes,this.timeoutInSeconds);
    }
    @Override
    public void shutdown() throws Exception {
        //this.systemValidator.shutdown();
    }
    public boolean checkRole(Access access,String role){
        Access.Role cr = rMap.get(access.role());
        Access.Role fr = rMap.get(role);
        if(fr==null){
            return false;
        }
        return fr.accessControl()>cr.accessControl();
    }
    @Override
    public boolean upgradeRole(Access access,String role){
        if(!access.primary()||role==null||(!rMap.containsKey(role))){
            return false;
        }
        Access.Role t = rMap.get(role);
        Access.Role s = rMap.get(access.role());
        if(t.accessControl()==AccessControl.root.accessControl()||t.accessControl()<=s.accessControl()){//not allow downgrade
            return false;
        }
        UserAccount acc = new UserAccount();
        acc.distributionKey(access.distributionKey());
        //acc.emailAddress(access.emailAddress());
        LocalDateTime loc = LocalDateTime.now();
        acc.timestamp(TimeUtil.toUTCMilliseconds(loc));
        if(!adataStore.createIfAbsent(acc,true)){
            acc.timestamp(TimeUtil.toUTCMilliseconds(loc));
            adataStore.update(acc);
        }
        if(t.accessControl()==AccessControl.admin.accessControl()){
            Membership mcc = new Membership();
            mcc.distributionKey(access.distributionKey());
            mcc.startTimestamp(TimeUtil.toUTCMilliseconds(loc));
            mcc.endTimestamp(TimeUtil.toUTCMilliseconds(loc.plusMonths(1)));
            mcc.timestamp(TimeUtil.toUTCMilliseconds(loc));
            mdatastore.create(mcc);//create membership for admin role
            acc.trial(true);
            acc.subscribed(false);
            adataStore.update(acc);
        }
        access.role(t.name());
        udataStore.update(access);
        return true;
    }
    public boolean grantAccess(Access access,Access owner){
        if(access.primary()||!owner.primary()) return false;
        IndexSet indexSet = new IndexSet();
        indexSet.distributionKey(owner.distributionKey());
        indexSet.label(Account.UserLabel);
        if(!idataStore.load(indexSet)) return false;
        boolean updated= false;
        for(String k : indexSet.keySet()){
            if(k.equals(access.distributionKey())){
                access.role(owner.role());
                updated = udataStore.update(access);
                break;
            }
        }
        return updated;
    }
    public boolean revokeAccess(Access access){
        if(access.primary()){
            return false;
        }
        access.role(rMap.get("player").name());
        return udataStore.update(access);
    }
}
