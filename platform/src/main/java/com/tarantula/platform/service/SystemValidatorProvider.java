package com.tarantula.platform.service;

import com.tarantula.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.AccessControl;
import com.tarantula.platform.PresenceIndex;
import com.tarantula.platform.SystemValidator;
import com.tarantula.admin.GameCluster;
import com.tarantula.platform.presence.Membership;
import com.tarantula.platform.presence.User;
import com.tarantula.platform.presence.UserAccount;
import com.tarantula.platform.util.SystemUtil;

import java.security.MessageDigest;
import java.time.LocalDateTime;
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
        GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(sp[0]);
        long stmp = ((Number)gameCluster.property(GameCluster.TIMESTAMP)).longValue();
        return SystemUtil.validAccessKey(messageDigest(),accessKey,(String)gameCluster.property(GameCluster.GAME_LOBBY),stmp);
    }
    public String accessKey(String gameClusterId){
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
        log.warn(onLobby.toString()+" has been monitored under ->"+subscription.toString());
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
                    deploymentServiceProvider.shutdown(o.typeId());
                    rlist.add(k);
                }
            }else{
                deploymentServiceProvider.shutdown(o.typeId());
                rlist.add(k);
            }
        });
        rlist.forEach((k)->{
            oMap.remove(k);
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
        acc.emailAddress(access.emailAddress());
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
}
