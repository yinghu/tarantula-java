package com.tarantula.admin;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.icodesoftware.DataStore;
import com.icodesoftware.Descriptor;
import com.icodesoftware.Lobby;
import com.icodesoftware.Session;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.game.*;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.presence.*;

import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.service.Metrics;
import com.tarantula.platform.service.TokenValidatorProvider;
import com.tarantula.platform.util.OnAccessDeserializer;
import com.tarantula.platform.util.ResponseSerializer;
import com.tarantula.platform.util.SystemUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class AdminRoleModule implements Module {

    private ApplicationContext context;
    private GsonBuilder builder;
    private DataStore account;
    private DataStore user;
    private DataStore purchase;
    private DeploymentServiceProvider deploymentServiceProvider;
    private TokenValidatorProvider tokenValidatorProvider;
    private int maxGameClusterCount;
    private int maxGameLobbyCount;
    private int minGameLobbyCount;
    private int defaultGameLevelCount;
    private int maxGameLevelCount;
    private SubscriptionFee monthly;
    private SubscriptionFee yearly;
    private ConcurrentHashMap<String,GameLobbyContext> pendingLobby;

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        if(session.action().equals("onCheckPermission")){
            User user = _user(session.systemId());
            Account acc = new UserAccount();
            acc.distributionKey(user.primary()?session.systemId():user.owner());
            account.load(acc);
            boolean ex = this.tokenValidatorProvider.checkSubscription(user.primary()?session.systemId():user.owner());
            session.write(new PermissionContext(maxGameClusterCount,acc.gameClusterCount(0),!ex).toJson().toString().getBytes(),label());
        }
        else if(session.action().equals("onGameClusterList")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            User user = _user(session.systemId());
            int index = ((Number)onAccess.property("index")).intValue();
            GameClusterContext adminContext = new GameClusterContext();
            adminContext.gameClusterList = new ArrayList<>();
            adminContext.index = index;
            IndexSet idx = new IndexSet();
            idx.distributionKey(user.primary()?session.systemId():user.owner());
            idx.label(Account.GameClusterLabel);
            if(account.load(idx)){
                idx.keySet.forEach((k)->{
                    GameCluster g = this.deploymentServiceProvider.gameCluster(k);
                    if(g!=null){
                        adminContext.gameClusterList.add(g);
                    }
                });
            }
            session.write(adminContext.toJson().toString().getBytes(),label());
        }
        else if(session.action().equals("onGameLobbyList")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            int page = ((Number)onAccess.property("page")).intValue();
            String accessId = (String)onAccess.property(OnAccess.ACCESS_ID);
            GameLobbyContext lobbyContext = this.gameLobbyContext(accessId);
            lobbyContext.page = page;
            session.write(lobbyContext.toJson().toString().getBytes(),label());
        }
        else if(session.action().equals("onGameServiceList")){
            GameServiceContext gsc = new GameServiceContext();
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            GameCluster gc = this.deploymentServiceProvider.gameCluster((String)onAccess.property(OnAccess.ACCESS_ID));
            gsc.lobby=(this.deploymentServiceProvider.lobby((String) gc.property(GameCluster.GAME_SERVICE)));
            session.write(gsc.toJson().toString().getBytes(),label());
        }
        else if(session.action().equals("onGameDataList")){
            GameDataStoreContext gsc = new GameDataStoreContext();
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            GameCluster gc = this.deploymentServiceProvider.gameCluster((String)onAccess.property(OnAccess.ACCESS_ID));
            Lobby lobby =(this.deploymentServiceProvider.lobby((String) gc.property(GameCluster.GAME_DATA)));
            DataStore ds = this.context.dataStore(lobby.descriptor().typeId().replace("-","_"));
            gsc.name = lobby.descriptor().typeId();
            gsc.tag = lobby.entryList().get(0).tag();
            gsc.dataStore = ds.name();//lobby.descriptor().typeId();
            gsc.dataStoreCount = ds.count();
            DataStore ss = this.context.dataStore(lobby.descriptor().typeId().replace("-data","_service"));
            gsc.serviceStore = ss.name();
            gsc.serviceStoreCount = ss.count();
            session.write(gsc.toJson().toString().getBytes(),label());
        }
        else if(session.action().equals("onShoppingList")){
            session.write(new ShoppingContext(monthly,yearly).toJson().toString().getBytes(),this.label());
        }
        else if(session.action().equals("onMetrics")){
            Metrics  metrics = this.deploymentServiceProvider.metrics();
            MetricsContext adminContext = new MetricsContext();
            adminContext.metrics = metrics;
            session.write(adminContext.toJson().toString().getBytes(),label());
        }
        else if(session.action().equals("onCreateAccessKey")){
            //generate access key from game cluster id
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            String key = tokenValidatorProvider.gameClusterAccessKey((String)onAccess.property("gameClusterId"));
            session.write(new PermissionContext(key).toJson().toString().getBytes(),label());
        }
        else if(session.action().equals("onTestAccessKey")){
            //test access key
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            String key = tokenValidatorProvider.validateGameClusterAccessKey((String)onAccess.property(OnAccess.ACCESS_KEY));
            session.write(toMessage(key!=null?"key passed":"key failed",key!=null).toString().getBytes(),label());
        }
        else if(session.action().equals("onCheckLobbySlot")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            String accessId = (String)onAccess.property(OnAccess.ACCESS_ID);
            GameLobbyContext gameLobbyContext = this.gameLobbyContext(accessId);
            session.write(gameLobbyContext.availableSlots().toString().getBytes(),label());
        }
        else if(session.action().equals("onAddLobby")){//subscription only
            //this.context.log(new String(payload),OnLog.WARN);
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            ArrayList<Integer> _alist = new ArrayList<>();
            for(int i=1;i<maxGameLobbyCount+1;i++){
                Object slot = onAccess.property("slot-"+i);
                if(slot!=null){
                    _alist.add(i);
                }
            }
            if(!_alist.isEmpty()){
                String accessId = (String)onAccess.property(OnAccess.ACCESS_ID);
                GameCluster gc = this.deploymentServiceProvider.gameCluster(accessId);
                Lobby lobby = this.deploymentServiceProvider.lobby((String)gc.property(GameCluster.GAME_LOBBY));
                String gname = (String) gc.property(GameCluster.NAME);
                boolean disabled = (Boolean)gc.property(GameCluster.DISABLED);
                int idx = lobby.entryList().size()+1;
                if(idx>maxGameLobbyCount){
                    session.write(toMessage("max lobby count has reached",false).toString().getBytes(),label());
                }else {
                    int[] added = {0};
                    HashMap<Integer,Descriptor> _ex = new HashMap<>();
                    lobby.entryList().forEach((a)->{
                        _ex.put(a.accessRank(),a);
                    });
                    _alist.forEach((rk) -> {
                        if(!_ex.containsKey(rk)){
                            Descriptor desc = lobby.entryList().get(0).copy();
                            desc.name("Game Lobby " + rk);
                            desc.tag(gname + "/lobby" + rk);
                            desc.accessRank(rk);
                            if(this.deploymentServiceProvider.createApplication(desc, !disabled)){
                                added[0]++;
                            }
                        }
                    });
                    if(added[0]>0){
                        pendingLobby.remove(accessId);
                    }
                    session.write(toMessage(added[0]>0?"total lobbies added ["+added[0]+"]":"lobby not added",added[0]>0).toString().getBytes(),label());
                }
            }
            else{
                session.write(toMessage("No lobby slot seleccted",false).toString().getBytes(),label());
            }
        }
        else if(session.action().equals("onDisableLobby")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            String accessId = (String) onAccess.property(OnAccess.ACCESS_ID);
            int index = ((Number)onAccess.property("index")).intValue();
            GameLobbyContext pending = this.gameLobbyContext(accessId);
            if(pending.checkEnabledLobbyCount()>minGameLobbyCount){
                //do disable operation
                GameLobby gameLobby = pending.gameLobbyList.get(index);
                if(pending.page==index&&(!gameLobby.lobby.disabled())){//set to disable
                    boolean suc = this.deploymentServiceProvider.enableApplication(gameLobby.lobby.distributionKey(),false);
                    if(suc){
                        this.pendingLobby.remove(accessId);
                    }
                    session.write(toMessage(suc?gameLobby.lobby.name()+" disabled":"failed to disble lobby",suc).toString().getBytes(),label());
                }
                else{
                    session.write(toMessage("Updated lobby ["+index+"] not matched with loaded lobby["+pending.page+"]",false).toString().getBytes(),label());
                }
            }
            else{
                session.write(toMessage("Min lobby count ["+minGameLobbyCount+"] required",false).toString().getBytes(),label());
            }
        }
        else if(session.action().equals("onEnableLobby")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            String accessId = (String) onAccess.property(OnAccess.ACCESS_ID);
            int index = ((Number)onAccess.property("index")).intValue();
            GameLobbyContext pending = this.gameLobbyContext(accessId);
            //do enable operation
            GameLobby gameLobby = pending.gameLobbyList.get(index);
            if(pending.page==index&&(gameLobby.lobby.disabled())){//set to enable
                boolean suc = this.deploymentServiceProvider.enableApplication(gameLobby.lobby.distributionKey(),true);
                if(suc){
                    this.pendingLobby.remove(accessId);
                }
                session.write(toMessage(suc?gameLobby.lobby.name()+" enabled":"failed to enable lobby",suc).toString().getBytes(),label());
            }
            else{
                session.write(toMessage("Updated lobby ["+index+"] not matched with loaded lobby["+pending.page+"]",false).toString().getBytes(),label());
            }
        }
        else if(session.action().equals("onReloadLobby")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            String accessId = (String) onAccess.property(OnAccess.ACCESS_ID);
            int index = ((Number)onAccess.property("index")).intValue();
            GameLobbyContext pending = this.gameLobbyContext(accessId);
            GameLobby gameLobby = pending.gameLobbyList.get(index);
            this.deploymentServiceProvider.configure(gameLobby.zone.distributionKey());
            session.write(toMessage("Lobby reloaded",true).toString().getBytes(),label());
        }
        else if(session.action().equals("onCreateGameCluster")){
            User ua = _user(session.systemId());
            Account acc = new UserAccount();
            acc.distributionKey(ua.primary()?session.systemId():ua.owner());
            if(account.load(acc)&&acc.gameClusterCount(0)<maxGameClusterCount){
                OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
                GameCluster gc = this.deploymentServiceProvider.createGameCluster(acc.distributionKey(),(String)onAccess.property("name"));
                if(gc.successful()){
                    IndexSet idx = new IndexSet();
                    idx.distributionKey(acc.distributionKey());
                    idx.label(Account.GameClusterLabel);
                    idx.keySet.add(gc.distributionKey());
                    if(!account.createIfAbsent(idx,true)){
                        idx.keySet.add(gc.distributionKey());//update on existing
                        account.update(idx);
                    }
                    acc.gameClusterCount(1);
                    acc.timestamp(SystemUtil.toUTCMilliseconds(LocalDateTime.now()));
                    account.update(acc);
                    gc.message("Game cluster created successfully");
                }
                session.write(gc.toJson().toString().getBytes(),label());
            }
            else{
                //reach max count
                session.write(this.builder.create().toJson(new ResponseHeader(session.action(),"you already have max game clusters",false)).getBytes(),label());
            }
        }

        else if(session.action().equals("onUpdateGameLobby")){
            //this.context.log(new String(payload),OnLog.WARN);
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            String accessId = (String) onAccess.property(OnAccess.ACCESS_ID);
            int index = ((Number)onAccess.property("page")).intValue();
            GameLobbyContext pending = this.gameLobbyContext(accessId);
            if(index==pending.page){
                GameLobby gameLobby = pending.gameLobbyList.get(pending.page);
                Zone zone = gameLobby.zone;
                zone.name = onAccess.name();
                zone.capacity = ((Number)onAccess.property("capacity")).intValue();
                zone.roundDuration = ((Number)onAccess.property("duration")).intValue()*60000;
                zone.playMode = ((Number)onAccess.property("playMode")).intValue();
                int lmit = ((Number)onAccess.property("levelLimit")).intValue();
                if(lmit>=defaultGameLevelCount&&lmit<=maxGameLevelCount){
                    zone.levelLimit = lmit;
                }
                zone.update();
                session.write(pending.toJson().toString().getBytes(),label());
            }
            else{
                session.write(toMessage("Updated lobby ["+index+"] not matched with loaded lobby["+pending.page+"]",false).toString().getBytes(),label());
            }
        }
        else if(session.action().equals("onUpdateGameLevel")){
            //this.context.log(new String(payload),OnLog.WARN);
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            String accessId = (String) onAccess.property(OnAccess.ACCESS_ID);
            int index = ((Number)onAccess.property("index")).intValue();
            int page = ((Number)onAccess.property("page")).intValue();
            boolean disabled = (Boolean)onAccess.property("disabled");
            GameLobbyContext pending = this.gameLobbyContext(accessId);
            if(page==pending.page){
                GameLobby gameLobby = pending.gameLobbyList.get(pending.page);
                Zone zone = gameLobby.zone;
                if(index<=maxGameLevelCount){
                    boolean updated  = false;
                    Arena pu = new Arena();
                    for(Arena a : zone.arenas){
                        if(a.level==index){
                            pu.name(a.name());
                            pu.xp = a.xp;
                            pu.level = a.level;
                            pu.capacity = a.capacity;
                            pu.duration = a.duration;
                            pu.disabled(a.disabled());
                            a.name(onAccess.name());
                            a.xp = ((Number)onAccess.property("xp")).intValue();
                            a.capacity = ((Number)onAccess.property("capacity")).intValue();
                            a.duration = ((Number)onAccess.property("duration")).intValue()*60000;
                            a.disabled(disabled);
                            updated = true;
                            break;
                        }
                    }
                    if(updated){
                        int mc = 0;
                        Arena ap=null;
                        for(Arena a: zone.arenas){
                            if(!a.disabled()){
                                mc++;
                            }
                            if(a.level==pu.level){
                                ap = a;
                            }
                        }
                        if(mc>0){
                            zone.update();
                            session.write(pending.toJson().toString().getBytes(),label());
                        }
                        else{
                            ap.level = pu.level;
                            ap.xp = pu.xp;
                            ap.capacity = pu.capacity;
                            ap.duration = pu.duration;
                            ap.name(pu.name());
                            ap.disabled(pu.disabled());
                            session.write(toMessage("at least one level per lobby",false).toString().getBytes(),label());
                        }
                    }
                    else{
                        Arena a = new Arena(zone.bucket(),zone.oid(),index);
                        a.name(onAccess.name());
                        a.xp = ((Number)onAccess.property("xp")).intValue();
                        a.level = index;
                        a.capacity = ((Number)onAccess.property("capacity")).intValue();
                        a.duration = ((Number)onAccess.property("duration")).intValue()*60000;
                        a.disabled((Boolean)onAccess.property("disabled"));
                        zone.arenas.add(a);
                        zone.update();
                        session.write(pending.toJson().toString().getBytes(),label());
                    }
                }
                else{
                    session.write(toMessage("level overflow",false).toString().getBytes(),label());
                }
            }else{
                session.write(toMessage("Updated lobby ["+index+"] not matched with loaded lobby["+pending.page+"]",false).toString().getBytes(),label());
            }
        }
        else if(session.action().equals("onLaunchGameCluster")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            String accessId = (String) onAccess.property(OnAccess.ACCESS_ID);
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(accessId);
            User _u = _user(session.systemId());
            Account acc = new UserAccount();
            acc.distributionKey(_u.primary()?session.systemId():_u.owner());
            account.load(acc);
            if(acc.trial()||acc.subscribed()){
                boolean suc = this.deploymentServiceProvider.launchGameCluster(gameCluster);
                session.write(this.builder.create().toJson(new ResponseHeader(session.action(),suc?"operation successfully":"operation failed",suc)).getBytes(),label());
            }
            else {
                session.write(this.builder.create().toJson(new ResponseHeader(session.action(), "no subscription or trial found", false)).getBytes(), label());
            }
        }
        else if(session.action().equals("onCommitPurchase")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            String cid = (String)onAccess.property("checkoutId");
            SubscriptionFee fee = cid.equals("monthlyAccess")?monthly:yearly;
            Map<String, Object> chargeParams = new HashMap<>();
            chargeParams.put("amount",Double.valueOf(fee.amount).intValue()*100);//pass penney number as integer
            chargeParams.put("currency", "usd");
            chargeParams.put("description", "Charge for ["+fee.name+"]");
            chargeParams.put("source",onAccess.property("orderId")); //orderId from client stripe call
            chargeParams.put("name",OnAccess.STRIPE);
            if(this.context.validator().validateToken(chargeParams)){
                //update subscription
                User u = this._user(session.systemId());
                int cnt = this.tokenValidatorProvider.updateSubscription(u.primary()?session.systemId():u.owner(),fee.durationMonths);
                SubscriptionFee _log = fee.log(u.primary()?session.systemId():u.owner(),cnt);
                purchase.create(_log);
                session.write(this.builder.create().toJson(new ResponseHeader("onCommit", "your purchase is successful", true)).getBytes(),this.label());
            }
            else{
                session.write(this.builder.create().toJson(new ResponseHeader("onCommit", "failed to commit your purchase", false)).getBytes(),this.label());
            }
        }
        else if(session.action().equals("onShutdownGameCluster")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            String accessId = (String) onAccess.property(OnAccess.ACCESS_ID);
            GameCluster gc = this.deploymentServiceProvider.gameCluster(accessId);
            boolean suc = this.deploymentServiceProvider.shutdownGameCluster(gc);
            session.write(this.builder.create().toJson(new ResponseHeader(session.action(),suc?"operation successfully":"operation failed",suc)).getBytes(),label());
        }
        else{
            session.write(this.builder.create().toJson(new ResponseHeader("onError", "operation not supported", false)).getBytes(),this.label());
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        Configuration ya = this.context.configuration("yearlyAccess");
        Configuration ma = this.context.configuration("monthlyAccess");
        monthly = new SubscriptionFee("monthlyAccess",ma.property("description"),ma.property("price"),ma.property("currency"),Integer.parseInt(ma.property("durationMonths")));
        yearly = new SubscriptionFee("yearlyAccess",ya.property("description"),ya.property("price"),ya.property("currency"),Integer.parseInt(ya.property("durationMonths")));
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.account = this.context.dataStore(Account.DataStore);
        this.user = this.context.dataStore(Access.DataStore);
        this.purchase = this.context.dataStore(SubscriptionFee.DataStore);
        this.tokenValidatorProvider = this.context.serviceProvider(TokenValidatorProvider.NAME);
        this.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        ya.registerListener((cf)->{
            //reload monthly
            this.context.log("UPDATE->"+SystemUtil.toJsonString(cf.toMap()),OnLog.WARN);
            Configuration c = (Configuration)cf;
            yearly =  new SubscriptionFee("yearlyAccess",c.property("description"),c.property("price"),c.property("currency"),Integer.parseInt(c.property("durationMonths")));
        });
        ma.registerListener((cf)->{
            //reload monthly
            this.context.log("UPDATE->"+SystemUtil.toJsonString(cf.toMap()),OnLog.WARN);
            Configuration c = (Configuration)cf;
            monthly = new SubscriptionFee("monthlyAccess",c.property("description"),c.property("price"),c.property("currency"),Integer.parseInt(c.property("durationMonths")));
        });
        this.deploymentServiceProvider.register(ya);
        this.deploymentServiceProvider.register(ma);
        this.maxGameClusterCount = Integer.parseInt(this.context.configuration("setup").property("maxGameClusterCount"));
        this.maxGameLobbyCount = Integer.parseInt(this.context.configuration("setup").property("maxGameLobbyCount"));
        this.defaultGameLevelCount = Integer.parseInt(this.context.configuration("setup").property("defaultGameLevelCount"));
        this.maxGameLevelCount = Integer.parseInt(this.context.configuration("setup").property("maxGameLevelCount"));
        this.minGameLobbyCount = Integer.parseInt(this.context.configuration("setup").property("minGameLobbyCount"));
        this.pendingLobby = new ConcurrentHashMap<>();
        //this.rankComparator = new GameLobbyComparator();
        this.context.log("Admin role module started with max game cluster count ["+maxGameClusterCount+"]", OnLog.INFO);
    }
    @Override
    public String label() {
        return "admin-role";
    }

    private GameLobbyContext gameLobbyContext(String accessId){
        return pendingLobby.computeIfAbsent(accessId,(k)-> {
            GameLobbyContext gameLobbyContext = new GameLobbyContext();
            gameLobbyContext.maxLobbyCount = maxGameLobbyCount;
            gameLobbyContext.gameLobbyList = new ConcurrentHashMap<>();
            gameLobbyContext.successful(true);
            GameCluster gc = this.deploymentServiceProvider.gameCluster(accessId);
            //query from master node to make sure data available always
            Lobby lobby = this.deploymentServiceProvider.lobby((String) gc.property(GameCluster.GAME_LOBBY));
            lobby.entryList().forEach((a) -> {
                GameLobby gameLobby = new GameLobby();
                gameLobby.lobby = a;
                gameLobby.zone = _zone(gc, a);
                gameLobbyContext.gameLobbyList.put(a.accessRank(),gameLobby);
            });
            //Collections.sort(gameLobbyContext.gameLobbyList,rankComparator);
            return gameLobbyContext;
        });
    }
    private Zone _zone(GameCluster gameCluster,Descriptor descriptor){
        String dn = (String)gameCluster.property(GameCluster.GAME_SERVICE);
        DataStore dataStore = this.context.dataStore(dn.replace("-","_"));
        Zone mZone = new Zone();
        mZone.distributionKey(descriptor.distributionKey());
        mZone.capacity=1;
        mZone.roundDuration = 60*1000;
        mZone.overtime = 5000;
        mZone.playMode = Room.OFF_LINE_MODE;
        mZone.timestamp(SystemUtil.toUTCMilliseconds(LocalDateTime.now()));
        mZone.dataStore(dataStore);
        dataStore.createIfAbsent(mZone,true);
        for(int i=1;i<maxGameLevelCount+1;i++){
            Arena a = new Arena(mZone.bucket(),mZone.oid(),i);
            if(dataStore.load(a)){
                mZone.arenas.add(a);
            }
        }
        if(mZone.arenas.size()==0){
            for(int i=1;i<defaultGameLevelCount+1;i++){
                Arena a = new Arena(mZone.bucket(),mZone.oid(),i);
                a.name("level"+i);
                a.level = i;
                a.xp = i*100;
                a.disabled(false);
                mZone.arenas.add(a);
            }
            mZone.update();
        }
        return mZone;
    }
    private JsonObject toMessage(String msg,boolean suc){
        JsonObject jms = new JsonObject();
        jms.addProperty("successful",suc);
        jms.addProperty("message",msg);
        return jms;
    }
    private User _user(String systemId){
        User u = new User();
        u.distributionKey(systemId);
        if(user.load(u)){
            return u;
        }
        return null;
    }
}
