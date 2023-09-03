package com.tarantula.platform.service;

import com.icodesoftware.*;
import com.icodesoftware.lmdb.BufferProxy;
import com.icodesoftware.service.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.util.CipherUtil;
import com.icodesoftware.util.JWTUtil;
import com.icodesoftware.util.OidKey;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.*;
import com.tarantula.platform.presence.Membership;
import com.tarantula.platform.presence.User;
import com.tarantula.platform.presence.UserAccount;
import com.tarantula.platform.util.PresenceFetcher;
import com.tarantula.platform.util.SystemUtil;

import javax.crypto.Cipher;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class SystemValidatorProvider implements TokenValidatorProvider {

    private static TarantulaLogger log = JDKLogger.getLogger(SystemValidatorProvider.class);

    private SystemValidator systemValidator;
    private int timeoutInMinutes;
    private int timeoutInSeconds;

    private ServiceContext serviceContext;
    private ConcurrentHashMap<String,PresenceIndex> pMap;
    private HashMap<String,Access.Role> rMap;
    private HashMap<String,AuthVendorRegistry> aMap;
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

    private ConcurrentHashMap<String,PresenceFetcher> fMap;

    private boolean remotePresenceEnabled;
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
        Presence presence = pMap.computeIfAbsent(session.oid(),(k)->{
            PresenceIndex px = new PresenceIndex();
            px.oid(session.oid());
            if(!pdataStore.load(px)) return null;
            px.dataStore(pdataStore);
            px.registerEventService(this.serviceContext.eventService());
            return px;
        });
        if(presence==null&&remotePresenceEnabled){
            log.warn("Fetching presence from presence service ...");
            PresenceFetcher httpCaller = fMap.get(session.trackId());
            OnSession onSession = httpCaller.presence(session.token());
            PresenceIndex px = new PresenceIndex(onSession.stub(),session.trackId());
            px.distributionKey(onSession.systemId());
            pdataStore.update(px);
            px.dataStore(pdataStore);
            px.registerEventService(this.serviceContext.eventService());
            pMap.put(session.oid(),px);
            return px;
        }
        return presence;
    }
    public Presence presence(String id){
        return pMap.computeIfAbsent(id,(k)->{
            PresenceIndex px = new PresenceIndex();
            px.oid(id);
            pdataStore.load(px);
            px.dataStore(pdataStore);
            px.registerEventService(this.serviceContext.eventService());
            return px;
        });
    }

    public byte[] clusterKey(String clusterNameSuffix){
        if(!clusterNameSuffix.equals(this.serviceContext.node().clusterNameSuffix())) return null;
        return presenceKey.clusterKey();
    }
    public byte[] tokenKey(String clusterNameSuffix){
        if(!clusterNameSuffix.equals(this.serviceContext.node().clusterNameSuffix())) return null;
        return presenceKey.tokenKey();
    }
    public boolean enablePresenceService(String root,String password,String clusterNameSuffix,String presenceServiceHost){
        try {
            PresenceFetcher httpCaller = new PresenceFetcher(presenceServiceHost,this.serviceContext.httpClientProvider());
            OnSession onSession = httpCaller.login(root,password);
            byte[] key = httpCaller.presenceKey(onSession.token(),clusterNameSuffix);
            if(key==null) return false;
            httpCaller.encrypt = CipherUtil.encrypt(key);
            this.remotePresenceEnabled = true;
            fMap.put(clusterNameSuffix,httpCaller);
            return true;
        }catch (Exception ex){
            log.error("error",ex);
            return false;
        }
    }
    public  void disablePresenceService(String classNameSuffix){
        fMap.remove(classNameSuffix);
        this.remotePresenceEnabled = fMap.size()>0;
    }

    public boolean resetClusterKey(){
        try{
            presenceKey.clusterKey(CipherUtil.toBase64Key());
            this.deployDataStore.update(presenceKey);
            this.clusterStore.mapSet(presenceKey.distributionKey().getBytes(),presenceKey.clusterKey());
            return true;
        }catch (Exception ex){
            log.error("reset key error",ex);
            return false;
        }
    }

    public void reset(){
        try{
            byte[] key = this.clusterStore.mapGet(presenceKey.distributionKey().getBytes());
            if(key==null) return;
            presenceKey.clusterKey(CipherUtil.toBase64Key(key));
            encrypt = CipherUtil.encrypt(presenceKey.clusterKey());
            log.warn("Cluster key has set!");
        }catch (Exception ex){
            log.error("reset key error",ex);
        }
    }

    public String clusterNameSuffix(){
        return this.serviceContext.node().clusterNameSuffix();
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
    public byte[] encrypt(Presence presence,byte[] data){
        try{
            Cipher cipher = fMap.get(presence.index()).encrypt;
            synchronized (cipher){
                return cipher.doFinal(data);
            }
            //return fMap.get(presence.index()).encrypt.doFinal(data);
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
        AccessKeyQuery query = new AccessKeyQuery(new OidKey(this.serviceContext.node().oid()));
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
        GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(akey.index());
        if(gameCluster==null) return null;
        String validLobby = gameCluster.gameLobbyName;
        String wmark = SystemUtil.validAccessKey(messageDigest(),accessKey,validLobby,akey.timestamp());
        String wm = SystemUtil.toHexString(encrypt(ByteBuffer.allocate(8).putLong(akey.timestamp()).array()));
        if(wm.equals(wmark)) return (T)gameCluster;
        return null;
    }
    public String createGameClusterAccessKey(String gameClusterId){
        GameCluster gc = this.deploymentServiceProvider.gameCluster(gameClusterId);
        long stmp = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
        AccessKey accessKey = new AccessKey();
        accessKey.typeId(gc.gameLobbyName);
        accessKey.timestamp(stmp);
        accessKey.owner(gameClusterId);
        if(!this.deployDataStore.create(accessKey)) return null;
        byte[] wmark = encrypt(ByteBuffer.allocate(8).putLong(stmp).array());
        return SystemUtil.accessKey(messageDigest(),accessKey.typeId(),accessKey.distributionKey(),stmp,SystemUtil.toHexString(wmark));
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
        byte[] data = BufferProxy.buffer(200).writeUTF8(key).writeInt(stub).writeInt(duration).array();
        byte[] mark = encrypt(data);
        return SystemUtil.toBase64String(mark);
    }
    public boolean validateTicket(String key,int stub,String ticket){
        byte[] mark = decrypt(SystemUtil.fromBase64String(ticket));
        Recoverable.DataBuffer buffer = BufferProxy.wrap(mark);
        return buffer.readUTF8().equals(key) && buffer.readInt() == stub;
    }
    public List<Access.Role> list(){
        return roleList;
    }
    public Access.Role role(String systemId){
        if(systemId==null){
            return rMap.get("player");
        }
        Access acc = new User();
        acc.oid(systemId);
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
            GameCluster g = this.deploymentServiceProvider.gameCluster(k);//new GameCluster();
            g.oid(o.gameClusterId());
            if(g!=null){
                g.disabled(true);
                deployDataStore.update(g);
            }
            Account acc = new UserAccount();
            acc.oid(o.subscriptionId());
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
        this.idataStore = this.serviceContext.dataStore(Distributable.DATA_SCOPE,Account.IndexDataStore);
        this.mdatastore =  this.serviceContext.dataStore(Distributable.DATA_SCOPE,Subscription.DataStore);
        this.deployDataStore = this.serviceContext.dataStore(Distributable.DATA_SCOPE,DeploymentServiceProvider.DEPLOY_DATA_STORE);
        oMap = new ConcurrentHashMap<>();
        fMap = new ConcurrentHashMap<>();
        AuthVendorRegistry google = (AuthVendorRegistry)this.serviceContext.authVendor(OnAccess.GOOGLE);
        if(google!=null){
            google.registerMetricsLister(serviceContext.metrics(Metrics.ACCESS));
            google.setup(serviceContext);
            aMap.put(OnAccess.GOOGLE,(google));
        }
        AuthVendorRegistry facebook = (AuthVendorRegistry) this.serviceContext.authVendor(OnAccess.FACEBOOK);
        if(facebook!=null){
            facebook.registerMetricsLister(serviceContext.metrics(Metrics.ACCESS));
            facebook.setup(serviceContext);
            aMap.put(OnAccess.FACEBOOK,facebook);
        }
        AuthVendorRegistry appleStore = (AuthVendorRegistry) this.serviceContext.authVendor(OnAccess.APPLE_STORE);
        if(appleStore!=null){
            appleStore.registerMetricsLister(serviceContext.metrics(Metrics.PAYMENT));
            appleStore.setup(serviceContext);
            aMap.put(OnAccess.APPLE_STORE,appleStore);
        }
        AuthVendorRegistry gameCenter = (AuthVendorRegistry) this.serviceContext.authVendor(OnAccess.GAME_CENTER);
        if(gameCenter!=null){
            gameCenter.registerMetricsLister(serviceContext.metrics(Metrics.ACCESS));
            gameCenter.setup(serviceContext);
            aMap.put(OnAccess.GAME_CENTER,gameCenter);
        }
        AuthVendorRegistry developerStore = (AuthVendorRegistry)this.serviceContext.authVendor(OnAccess.DEVELOPER_STORE);
        if(developerStore!=null){
            developerStore.registerMetricsLister((k,v)->{});
            developerStore.setup(serviceContext);
            aMap.put(OnAccess.DEVELOPER_STORE,developerStore);
        }
        AuthVendorRegistry stripe = (AuthVendorRegistry)this.serviceContext.authVendor(OnAccess.STRIPE);
        if(stripe!=null){
            stripe.registerMetricsLister(serviceContext.metrics(Metrics.PAYMENT));
            stripe.setup(serviceContext);
            aMap.put(OnAccess.STRIPE,(stripe));
        }
        AuthVendorRegistry googleStore = (AuthVendorRegistry) this.serviceContext.authVendor(OnAccess.GOOGLE_STORE);
        if(googleStore!=null){
            googleStore.registerMetricsLister(serviceContext.metrics(Metrics.PAYMENT));
            googleStore.setup(serviceContext);
            aMap.put(OnAccess.GOOGLE_STORE,googleStore);
        }
        AuthVendorRegistry amazonAws = (AuthVendorRegistry) this.serviceContext.authVendor(OnAccess.AMAZON);
        if(amazonAws!=null){
            amazonAws.registerMetricsLister(serviceContext.metrics(Metrics.ACCESS));
            amazonAws.setup(serviceContext);
            aMap.put(OnAccess.AMAZON,amazonAws);
        }
        AuthVendorRegistry applicationStore = (AuthVendorRegistry)this.serviceContext.authVendor(OnAccess.APPLICATION_STORE);
        if(applicationStore!=null){
            applicationStore.registerMetricsLister(serviceContext.metrics(Metrics.PAYMENT));
            applicationStore.setup(serviceContext);
            aMap.put(OnAccess.APPLICATION_STORE,applicationStore);
        }
        //map only store
        clusterStore = serviceContext.clusterProvider().clusterStore(ClusterProvider.ClusterStore.SMALL,TokenValidatorProvider.NAME,true,false,false);
    }

    @Override

    public void waitForData() {
        try{
            PresenceKey pKey = new PresenceKey(serviceContext.node().nodeId());
            byte[] clusterKey = this.serviceContext.clusterProvider().deployService().onClusterKey();
            byte[] tokenKey = this.serviceContext.clusterProvider().deployService().onTokenKey();
            if(clusterKey!=null&&tokenKey!=null){
                pKey.clusterKey(CipherUtil.toBase64Key(clusterKey));
                pKey.tokenKey(CipherUtil.toBase64Key(tokenKey));
            }
            else{
                pKey.clusterKey(CipherUtil.toBase64Key());
                pKey.tokenKey(CipherUtil.toBase64Key(JWTUtil.key()));
                deployDataStore.createIfAbsent(pKey,true);
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

    public void registerAuthVendor(String provider,AuthVendor authVendor){
        aMap.get(provider).registerAuthVendor(authVendor);
    }
    public void releaseAuthVendor(String provider,AuthVendor authVendor){
        aMap.get(provider).registerAuthVendor(authVendor);
    }

    public String jwtToken(Access access,OnSession session){
        return jwt.token((h,p)->{
            long expiry = TimeUtil.toUTCMilliseconds(LocalDateTime.now().plusHours(24));
            Recoverable.DataBuffer dataBuffer = BufferProxy.buffer(200);
            dataBuffer.writeUTF8(access.oid()).writeInt(session.stub());
            byte[] mark = encrypt(dataBuffer.array());
            h.addProperty("kid",CipherUtil.toBase64Key(mark));
            p.addProperty("aud", access.role());
            p.addProperty("exp",expiry);
            return true;
        });
    }
    public OnSession jwtToken(String token){
        OnSession onSession = new OnSessionTrack();
        if(!jwt.verify(token,(h,p)->{
            long expiry = p.get("exp").getAsLong();
            if(TimeUtil.expired(TimeUtil.fromUTCMilliseconds(expiry))) return false;
            Access.Role r = rMap.get(p.get("aud").getAsString());
            if(r==null) return false;
            byte[] data = decrypt(CipherUtil.fromBase64Key(h.get("kid").getAsString()));
            Recoverable.DataBuffer dataBuffer =  BufferProxy.wrap(data);
            String id = dataBuffer.readUTF8();
            int stub = dataBuffer.readInt();
            onSession.oid(id);
            onSession.stub(stub);
            return true;
        })) return OnSessionTrack.INVALID_TOKEN;
        onSession.ticket(ticket(onSession.oid(),onSession.stub(),timeoutInSeconds));
        return onSession;
    }

}
