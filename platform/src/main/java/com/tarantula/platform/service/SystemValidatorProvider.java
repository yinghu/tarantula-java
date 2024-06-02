package com.tarantula.platform.service;

import com.icodesoftware.*;
import com.icodesoftware.lmdb.BufferProxy;
import com.icodesoftware.service.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.util.*;
import com.tarantula.platform.*;
import com.tarantula.platform.presence.Membership;
import com.tarantula.platform.presence.User;
import com.tarantula.platform.presence.UserAccount;
import com.tarantula.platform.presence.UserPortableRegistry;
import com.tarantula.platform.util.RecoverableQuery;
import com.tarantula.platform.util.SystemUtil;

import javax.crypto.Cipher;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SystemValidatorProvider implements TokenValidatorProvider {

    private static TarantulaLogger log = JDKLogger.getLogger(SystemValidatorProvider.class);

    private SystemValidator systemValidator;
    private int timeoutInMinutes;
    private int timeoutInSeconds;

    private int maxOnSessionCount;
    private ServiceContext serviceContext;
    private ConcurrentHashMap<Long,PresenceIndex> pMap;
    private HashMap<String,Access.Role> rMap;
    private HashMap<String,AuthVendorRegistry> aMap;
    private DataStore pdataStore;//presence
    private DataStore udataStore;//user
    private DataStore adataStore;//account
    private DataStore mdatastore;//membership

    private DataStore sdatastore;//onsession

    private DataStore deployDataStore;

    private List<Access.Role> roleList;
    private MessageDigest _messageDigest;

    private ConcurrentHashMap<String, OnLobby> oMap;
    private DeploymentServiceProvider deploymentServiceProvider;


    private PresenceKey presenceKey;
    private Cipher encrypt;
    private Cipher decrypt;
    private ClusterProvider.ClusterStore clusterStore;

    private JWTUtil.JWT jwt;
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
        Presence presence = presence(session.distributionId());
        return presence;
    }

    public Presence presence(long id){
        return pMap.computeIfAbsent(id,(k)->{
            PresenceIndex px = new PresenceIndex(sdatastore);
            px.distributionId(id);
            if(!pdataStore.load(px)) return null;
            px.dataStore(pdataStore);
            px.load(maxOnSessionCount);
            px.registerEventService(this.serviceContext.eventService());
            return px;
        });
    }

    public boolean resetClusterKey(){
        try{
            PresenceKey existing = new PresenceKey();
            existing.distributionId(presenceKey.distributionId());
            if(this.deployDataStore.load(existing)) return false;
            existing.clusterKey(CipherUtil.toBase64Key());
            existing.tokenKey(CipherUtil.toBase64Key(JWTUtil.key()));
            if(!this.deployDataStore.update(existing)) return false;
            byte[] ck = (serviceContext.node().bucketName()+"_ck").getBytes();
            byte[] jk = (serviceContext.node().bucketName()+"_jk").getBytes();
            this.clusterStore.mapSet(ck,existing.clusterKey());
            this.clusterStore.mapSet(jk,existing.tokenKey());
            return true;
        }catch (Exception ex){
            log.error("reset key error",ex);
            return false;
        }
    }

    public void reset(){
        try{
            byte[] ck = (serviceContext.node().bucketName()+"_ck").getBytes();
            byte[] jk = (serviceContext.node().bucketName()+"_jk").getBytes();
            byte[] ckey = this.clusterStore.mapGet(ck);
            byte[] jkey = this.clusterStore.mapGet(jk);
            if(ckey==null) {
                log.warn("Cluster key not set on cluster !");
                return;
            }
            if(jkey==null ) {
                log.warn("JWT key not set on cluster !");
                return;
            }
            PresenceKey existing = new PresenceKey(presenceKey.distributionId());
            if(!deployDataStore.load(existing)) return;
            if(!Arrays.equals(ckey,existing.clusterKey())) {
                log.warn("Cluster key not replicated ");
                return;
            }
            if(!Arrays.equals(jkey,existing.tokenKey())) {
                log.warn("JWT key not replicated ");
                return;
            }
            this.presenceKey = existing;
            encrypt = CipherUtil.encrypt(presenceKey.clusterKey());
            decrypt = CipherUtil.decrypt(presenceKey.clusterKey());
            jwt = JWTUtil.init(this.presenceKey.tokenKey());
            log.warn("Cluster key has been reset!");
        }catch (Exception ex){
            log.error("reset key error",ex);
        }
    }


    public byte[] encrypt(byte[] data){
        try{
            synchronized (encrypt){
                return encrypt.doFinal(data);
            }
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public byte[] decrypt(byte[] data){
        try{
            synchronized (decrypt){
                return decrypt.doFinal(data);
            }
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public void offSession(long systemId,long stub){
        Presence presence = pMap.get(systemId);
        if(presence==null) return;
        if(presence.offSession(stub)) return;
        pMap.remove(systemId);
        presence.disabled(true);
        presence.update();
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
        return SystemUtil.validAccessKey(messageDigest(),accessKey,label,stmp)!=null?label:null;
    }
    public String createAccessKey(String label){
        AccessKey ck = new AccessKey();
        long stmp = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
        ck.timestamp(stmp);//property(AccessKey.TIMESTAMP,stmp);
        ck.typeId(label);//property(AccessKey.KEY_LABEL,label);
        //???ck.owner(this.serviceContext.node().bucketId());
        if(deployDataStore.create(ck)){
            byte[] wmark = encrypt(ByteBuffer.allocate(8).putLong(stmp).array());
            return SystemUtil.accessKey(messageDigest(),label,ck.distributionKey(),stmp,SystemUtil.toHexString(wmark));
        }
        return null;
    }
    public List<OnAccess> accessKeyList(){
        AccessKeyQuery query = new AccessKeyQuery(new SnowflakeKey(this.serviceContext.node().nodeId()));
        ArrayList<OnAccess> keys = new ArrayList<>();
        deployDataStore.list(query,accessKey -> {
            if(!accessKey.disabled()) keys.add(accessKey);
            return true;
        });
        return keys;
    }
    public void revokeAccessKey(String accessKey){
        String[] sp = accessKey.split("-");
        AccessKey ck = new AccessKey();
        ck.distributionKey(sp[0]);
        if(!deployDataStore.load(ck)) return;
        ck.disabled(true);
        deployDataStore.update(ck);
    }
    public <T extends OnAccess> T validateGameClusterAccessKey(String accessKey){
        String[] sp = accessKey.split("-");
        AccessKey akey = new AccessKey();
        akey.distributionKey(sp[0]);
        if(!this.deployDataStore.load(akey) || akey.disabled()) return null;
        GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(Long.parseLong(akey.index()));
        if(gameCluster==null) return null;
        String validLobby = gameCluster.gameLobbyName;
        String wmark = SystemUtil.validAccessKey(messageDigest(),accessKey,validLobby,akey.timestamp());
        String wm = SystemUtil.toHexString(encrypt(ByteBuffer.allocate(8).putLong(akey.timestamp()).array()));
        if(wm.equals(wmark)) return (T)gameCluster;
        return null;
    }
    public String createGameClusterAccessKey(long gameClusterId){
        GameCluster gc = this.deploymentServiceProvider.gameCluster(gameClusterId);
        long stmp = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
        AccessKey accessKey = new AccessKey();
        accessKey.typeId(gc.gameLobbyName);
        accessKey.timestamp(stmp);
        accessKey.index(Long.toString(gameClusterId));
        accessKey.ownerKey(new SnowflakeKey(gc.distributionId()));
        if(!this.deployDataStore.create(accessKey)) return null;
        byte[] wmark = encrypt(ByteBuffer.allocate(8).putLong(stmp).array());
        return SystemUtil.accessKey(messageDigest(),accessKey.typeId(),accessKey.distributionKey(),stmp,SystemUtil.toHexString(wmark));
    }
    public List<String> gameClusterAccessKeyList(long gameClusterId){
        AccessKeyQuery query = new AccessKeyQuery(gameClusterId);
        ArrayList<String> keys = new ArrayList<>();
        deployDataStore.list(query,accessKey -> {
            if(!accessKey.disabled()) keys.add(accessKey.distributionKey());
            return true;
        });
        return keys;
    }

    public String ticket(long key,long stub,int duration){
        byte[] data = BufferProxy.buffer(200,false).writeLong(key).writeLong(stub).writeInt(duration).array();
        byte[] mark = encrypt(data);
        return SystemUtil.toBase64String(mark);
    }
    public boolean validateTicket(long key,long stub,String ticket){
        byte[] mark = decrypt(SystemUtil.fromBase64String(ticket));
        Recoverable.DataBuffer buffer = BufferProxy.wrap(mark);
        return buffer.readLong()==(key) && buffer.readLong() == stub;
    }
    public List<Access.Role> list(){
        return roleList;
    }
    public Access.Role role(long systemId){
        if(systemId==0){
            return rMap.get("player");
        }
        Access acc = new User();
        acc.distributionId(systemId);
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
        subscription.distributionId(onLobby.subscriptionId());
        if(mdatastore.load(subscription)) {
            oMap.put(onLobby.typeId(), onLobby);
            log.warn(onLobby+ " has been monitored at expiration time ->" + TimeUtil.fromUTCMilliseconds(subscription.endTimestamp()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
    }
    public boolean checkSubscription(long systemId){
        Subscription subscription = new Membership();
        subscription.distributionId(systemId);
        if(!this.mdatastore.load(subscription)){
            return false;
        }
        LocalDateTime end = TimeUtil.fromUTCMilliseconds(subscription.endTimestamp());
        return end.isAfter(LocalDateTime.now());
    }
    public int updateSubscription(long systemId,int months){
        Subscription subscription = new Membership();
        subscription.distributionId(systemId);
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
        acc.distributionId(systemId);
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
            subscription.distributionId(o.subscriptionId());
            if(this.mdatastore.load(subscription)){
                LocalDateTime end = TimeUtil.fromUTCMilliseconds(subscription.endTimestamp());
                if(end.isBefore(_curr)){
                    //deploymentServiceProvider.shutdownModule(o.typeId());
                    rlist.add(k);
                }
            }else{
                //deploymentServiceProvider.shutdownModule(o.typeId());
                rlist.add(k);
            }
        });
        rlist.forEach((k)->{
            OnLobby o = oMap.remove(k);
            GameCluster g = this.deploymentServiceProvider.gameCluster(o.gameClusterId());//new GameCluster();
            g.distributionId(o.gameClusterId());
            if(g!=null){
                g.disabled(true);
                deployDataStore.update(g);
            }
            Account acc = new UserAccount();
            acc.distributionId(o.subscriptionId());
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
        this.pdataStore =  this.serviceContext.dataStore(Distributable.DATA_SCOPE,Presence.DataStore);
        this.udataStore =  this.serviceContext.dataStore(Distributable.DATA_SCOPE,Access.DataStore);
        this.adataStore =  this.serviceContext.dataStore(Distributable.DATA_SCOPE,Account.DataStore);
        this.mdatastore =  this.serviceContext.dataStore(Distributable.DATA_SCOPE,Subscription.DataStore);
        this.sdatastore = this.serviceContext.dataStore(Distributable.DATA_SCOPE,OnSession.DataStore);
        this.deployDataStore = this.serviceContext.dataStore(Distributable.DATA_SCOPE,DeploymentServiceProvider.DEPLOY_DATA_STORE);
        oMap = new ConcurrentHashMap<>();

        AuthVendorRegistry google = TarantulaContext.getInstance().authVendor(OnAccess.GOOGLE);
        if(google!=null){
            google.registerMetricsLister(serviceContext.metrics(Metrics.ACCESS));
            google.setup(serviceContext);
            aMap.put(OnAccess.GOOGLE,(google));
        }
        AuthVendorRegistry facebook = TarantulaContext.getInstance().authVendor(OnAccess.FACEBOOK);
        if(facebook!=null){
            facebook.registerMetricsLister(serviceContext.metrics(Metrics.ACCESS));
            facebook.setup(serviceContext);
            aMap.put(OnAccess.FACEBOOK,facebook);
        }
        AuthVendorRegistry appleStore = TarantulaContext.getInstance().authVendor(OnAccess.APPLE_STORE);
        if(appleStore!=null){
            appleStore.registerMetricsLister(serviceContext.metrics(Metrics.PAYMENT));
            appleStore.setup(serviceContext);
            aMap.put(OnAccess.APPLE_STORE,appleStore);
        }
        AuthVendorRegistry gameCenter = TarantulaContext.getInstance().authVendor(OnAccess.GAME_CENTER);
        if(gameCenter!=null){
            gameCenter.registerMetricsLister(serviceContext.metrics(Metrics.ACCESS));
            gameCenter.setup(serviceContext);
            aMap.put(OnAccess.GAME_CENTER,gameCenter);
        }
        AuthVendorRegistry developerStore = TarantulaContext.getInstance().authVendor(OnAccess.DEVELOPER_STORE);
        if(developerStore!=null){
            developerStore.registerMetricsLister((k,v)->{});
            developerStore.setup(serviceContext);
            aMap.put(OnAccess.DEVELOPER_STORE,developerStore);
        }
        AuthVendorRegistry stripe = TarantulaContext.getInstance().authVendor(OnAccess.STRIPE);
        if(stripe!=null){
            stripe.registerMetricsLister(serviceContext.metrics(Metrics.PAYMENT));
            stripe.setup(serviceContext);
            aMap.put(OnAccess.STRIPE,(stripe));
        }
        AuthVendorRegistry googleStore = TarantulaContext.getInstance().authVendor(OnAccess.GOOGLE_STORE);
        if(googleStore!=null){
            googleStore.registerMetricsLister(serviceContext.metrics(Metrics.PAYMENT));
            googleStore.setup(serviceContext);
            aMap.put(OnAccess.GOOGLE_STORE,googleStore);
        }
        AuthVendorRegistry amazonAws = TarantulaContext.getInstance().authVendor(OnAccess.AMAZON);
        if(amazonAws!=null){
            amazonAws.registerMetricsLister(serviceContext.metrics(Metrics.ACCESS));
            amazonAws.setup(serviceContext);
            aMap.put(OnAccess.AMAZON,amazonAws);
        }
        AuthVendorRegistry applicationStore = TarantulaContext.getInstance().authVendor(OnAccess.APPLICATION_STORE);
        if(applicationStore!=null){
            applicationStore.registerMetricsLister(serviceContext.metrics(Metrics.PAYMENT));
            applicationStore.setup(serviceContext);
            aMap.put(OnAccess.APPLICATION_STORE,applicationStore);
        }
        AuthVendorRegistry webHook = TarantulaContext.getInstance().authVendor(OnAccess.WEB_HOOK);
        if(webHook!=null){
            webHook.registerMetricsLister(serviceContext.metrics(Metrics.ACCESS));
            webHook.setup(serviceContext);
            aMap.put(OnAccess.WEB_HOOK,webHook);
        }
        AuthVendorRegistry jdbcPool = TarantulaContext.getInstance().authVendor(OnAccess.JDBC_SQL);
        if(jdbcPool!=null){
            jdbcPool.registerMetricsLister(serviceContext.metrics(Metrics.ACCESS));
            jdbcPool.setup(serviceContext);
            aMap.put(OnAccess.JDBC_SQL,jdbcPool);
        }
        AuthVendorRegistry download = TarantulaContext.getInstance().authVendor(OnAccess.DOWNLOAD_CENTER);
        if(download!=null){
            download.registerMetricsLister(serviceContext.metrics(Metrics.ACCESS));
            download.setup(serviceContext);
            aMap.put(OnAccess.DOWNLOAD_CENTER,download);
        }
        Configuration configuration = serviceContext.configuration("account-role-user-settings");
        maxOnSessionCount = ((Number)configuration.property("maxOnSessionCount")).intValue();
        //map only store
        clusterStore = serviceContext.clusterProvider().clusterStore(ClusterProvider.ClusterStore.SMALL,TokenValidatorProvider.NAME,true,false,false);
    }

    @Override

    public void waitForData() {
        try{
            PresenceKey pKey = new PresenceKey(serviceContext.node().bucketId());
            pKey.clusterKey(CipherUtil.toBase64Key());
            pKey.tokenKey(CipherUtil.toBase64Key(JWTUtil.key()));
            if(deployDataStore.createIfAbsent(pKey,true)){
                log.warn("Cluster and token keys have initialized on node ["+serviceContext.node().nodeName()+"]");
            }
            else{
                log.warn("Cluster and token keys have loaded on node ["+serviceContext.node().nodeName()+"]");
            }
            encrypt = CipherUtil.encrypt(pKey.clusterKey());
            decrypt = CipherUtil.decrypt(pKey.clusterKey());
            this.presenceKey = pKey;
            jwt = JWTUtil.init(this.presenceKey.tokenKey());
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
        log.warn("System validator provider started ["+serviceContext.node().nodeId()+"]["+serviceContext.node().bucketId()+"]");
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
        RecoverableQuery<User> query = RecoverableQuery.query(owner.distributionId(),new User(), UserPortableRegistry.INS);
        boolean[] updated = {false};
        udataStore.list(query,(u)->{
            if(u.distributionId()==access.distributionId()){
                u.role(owner.role());
                updated[0] = udataStore.update(u);
                return false;
            }
            return true;
        });
        return updated[0];
    }
    public boolean revokeAccess(Access access){
        if(access.primary()){
            return false;
        }
        access.role(rMap.get("player").name());
        return udataStore.update(access);
    }

    public void registerAuthVendor(String provider,AuthVendor authVendor){
        aMap.get(provider).registerAuthVendor(authVendor);
    }
    public void releaseAuthVendor(String provider,AuthVendor authVendor){
        aMap.get(provider).registerAuthVendor(authVendor);
    }

    public String token(long systemId,long stub){
        Access acc = new User();
        acc.distributionId(systemId);
        udataStore.load(acc);
        OnSession onSession = new OnSessionTrack();
        onSession.stub(stub);
        return jwtToken(acc,onSession);
    }
    public String jwtToken(Access access,OnSession session){
        return jwt.token((h,p)->{
            long expiry = TimeUtil.toUTCMilliseconds(LocalDateTime.now().plusHours(24));
            Recoverable.DataBuffer dataBuffer = BufferProxy.buffer(16,false);
            dataBuffer.writeLong(access.distributionId()).writeLong(session.stub());
            byte[] mark = encrypt(dataBuffer.array());
            h.addProperty("kid",CipherUtil.toBase64Key(mark));
            p.addProperty("aud", access.role());
            p.addProperty("exp",expiry);
            return true;
        });
    }
    public OnSession jwtToken(String token){
        OnSession onSession = new OnSessionTrack();
        try {
            if (!jwt.verify(token, (h, p) -> {
                long expiry = p.get("exp").getAsLong();
                if (TimeUtil.expired(TimeUtil.fromUTCMilliseconds(expiry))) {
                    log.warn("Token expired  : "+expiry);
                    return false;
                }
                Access.Role r = rMap.get(p.get("aud").getAsString());
                if (r == null) {
                    log.warn("Role cannot be null");
                    return false;
                }
                byte[] data = decrypt(CipherUtil.fromBase64Key(h.get("kid").getAsString()));
                Recoverable.DataBuffer dataBuffer = BufferProxy.wrap(data);
                long id = dataBuffer.readLong();
                long stub = dataBuffer.readLong();
                onSession.distributionId(id);
                onSession.stub(stub);
                return true;
            })) return OnSessionTrack.INVALID_TOKEN;
        }
        catch (Exception ex){
            log.warn("Should not be here :",ex);
            return OnSessionTrack.INVALID_TOKEN;
        }
        onSession.ticket(ticket(onSession.distributionId(),onSession.stub(),timeoutInSeconds));
        onSession.successful(true);
        return onSession;
    }

}
