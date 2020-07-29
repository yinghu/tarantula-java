package com.tarantula.platform.service;

import com.tarantula.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.AccessControl;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.PresenceIndex;
import com.tarantula.platform.SystemValidator;
import com.tarantula.platform.presence.GameCluster;
import com.tarantula.platform.presence.Membership;
import com.tarantula.platform.presence.User;
import com.tarantula.platform.presence.UserAccount;
import com.tarantula.platform.util.SystemUtil;

import java.security.MessageDigest;
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
    private DataStore mdatastore;//membership

    private DataStore deployDataStore;

    private List<Access.Role> roleList;
    private MessageDigest _messageDigest;

    private ConcurrentHashMap<String,OnLobby> oMap;
    private DeploymentServiceProvider deploymentServiceProvider;

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

    public boolean validateAccessKey(String accessKey){
        String[] sp = accessKey.split("-");
        AccessKey ck = new AccessKey();
        ck.distributionKey(sp[0]);
        if(!deployDataStore.load(ck)){
            return false;
        }
        long stmp = ((Number)ck.property(AccessKey.TIMESTAMP)).longValue();
        String label = (String)ck.property(AccessKey.KEY_LABEL);
        return SystemUtil.validAccessKey(messageDigest(),accessKey,label,stmp);
    }
    public String accessKey(String label){
        AccessKey ck = new AccessKey();
        long stmp =SystemUtil.toUTCMilliseconds(LocalDateTime.now());
        ck.property(AccessKey.TIMESTAMP,stmp);
        ck.property(AccessKey.KEY_LABEL,label);
        if(deployDataStore.create(ck)){
            return SystemUtil.accessKey(messageDigest(),label,ck.distributionKey(),stmp);
        }
        return null;
    }

    public String validateGameClusterAccessKey(String accessKey){
        String[] sp = accessKey.split("-");
        GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(sp[0]);
        if(gameCluster==null){
            return null;
        }
        long stmp = ((Number)gameCluster.property(GameCluster.TIMESTAMP)).longValue();
        String validLobby = (String)gameCluster.property(GameCluster.GAME_LOBBY);
        return SystemUtil.validAccessKey(messageDigest(),accessKey,validLobby,stmp)?validLobby:null;
    }
    public String gameClusterAccessKey(String gameClusterId){
        GameCluster gc = this.deploymentServiceProvider.gameCluster(gameClusterId);
        long stmp =SystemUtil.toUTCMilliseconds(LocalDateTime.now());
        gc.property(GameCluster.TIMESTAMP,stmp);
        gc.update();
        return SystemUtil.accessKey(messageDigest(),(String)gc.property(GameCluster.GAME_LOBBY),gameClusterId,stmp);
    }

    public String ticket(String key,int stub,int duration){
        return SystemUtil.ticket(messageDigest(),key,stub,duration);
    }
    public boolean validateTicket(String key,int stub,String ticket){
        return SystemUtil.validTicket(messageDigest(),key,stub,ticket);
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
        mdatastore.load(subscription);
        oMap.put(onLobby.typeId(),onLobby);
        log.warn(onLobby.toString()+" has been monitored under ->"+SystemUtil.fromUTCMilliseconds(subscription.endTimestamp()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
    public boolean checkSubscription(String systemId){
        Subscription subscription = new Membership();
        subscription.distributionKey(systemId);
        if(!this.mdatastore.load(subscription)){
            return false;
        }
        LocalDateTime end = SystemUtil.fromUTCMilliseconds(subscription.endTimestamp());
        return end.isAfter(LocalDateTime.now());
    }
    public int updateSubscription(String systemId,int months){
        Subscription subscription = new Membership();
        subscription.distributionKey(systemId);
        if(!this.mdatastore.load(subscription)){
            return 0;
        }
        LocalDateTime end = SystemUtil.fromUTCMilliseconds(subscription.endTimestamp());
        subscription.endTimestamp(SystemUtil.toUTCMilliseconds(end.plusMonths(months)));
        subscription.timestamp(SystemUtil.toUTCMilliseconds(LocalDateTime.now()));
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
                LocalDateTime end = SystemUtil.fromUTCMilliseconds(subscription.endTimestamp());
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
        this.mdatastore =  this.serviceContext.dataStore(Subscription.DataStore,this.serviceContext.partitionNumber());
        this.deployDataStore = this.serviceContext.dataStore(DeploymentServiceProvider.DEPLOY_DATA_STORE,this.serviceContext.partitionNumber());
        oMap = new ConcurrentHashMap<>();
        AuthVendor google = this.serviceContext.authVendor(OnAccess.GOOGLE);
        if(google!=null){
            google.registerMetricsLister(this.deploymentServiceProvider);
            aMap.put(OnAccess.GOOGLE,(google));
        }
        AuthVendor stripe = this.serviceContext.authVendor(OnAccess.STRIPE);
        if(stripe!=null){
            stripe.registerMetricsLister(this.deploymentServiceProvider);
            aMap.put(OnAccess.STRIPE,(stripe));
        }
    }

    @Override
    public void waitForData() {
        log.info("System validator provider started");
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
        acc.timestamp(SystemUtil.toUTCMilliseconds(loc));
        if(!adataStore.createIfAbsent(acc,true)){
            acc.timestamp(SystemUtil.toUTCMilliseconds(loc));
            adataStore.update(acc);
        }
        if(t.accessControl()==AccessControl.admin.accessControl()){
            Membership mcc = new Membership();
            mcc.distributionKey(access.distributionKey());
            mcc.startTimestamp(SystemUtil.toUTCMilliseconds(loc));
            mcc.endTimestamp(SystemUtil.toUTCMilliseconds(loc.plusMonths(1)));
            mcc.timestamp(SystemUtil.toUTCMilliseconds(loc));
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
        if(access.primary()||!owner.primary()){
            return false;
        }
        IndexSet indexSet = new IndexSet();
        indexSet.distributionKey(owner.distributionKey());
        indexSet.label(Account.UserLabel);
        if(adataStore.load(indexSet)){
            boolean updated= false;
            for(String k : indexSet.keySet){
                if(k.equals(access.distributionKey())){
                    access.role(owner.role());
                    updated = udataStore.update(access);
                    break;
                }
            }
            return updated;
        }else{
            return false;
        }
    }
    public boolean revokeAccess(Access access){
        if(access.primary()){
            return false;
        }
        access.role(rMap.get("player").name());
        return udataStore.update(access);
    }
}
