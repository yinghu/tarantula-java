package com.tarantula.admin;

import com.google.gson.GsonBuilder;
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
import com.tarantula.platform.util.PresenceContextSerializer;
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
    private DeploymentServiceProvider deploymentServiceProvider;
    private TokenValidatorProvider tokenValidatorProvider;
    private int maxGameClusterCount;
    private int defaultGameLevelCount;
    private int maxGameLevelCount;
    private SubscriptionFee monthly;
    private SubscriptionFee yearly;

    private ConcurrentHashMap<String,GameLobbyContext> pendingLobby;

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        //this.context.log(session.action(),OnLog.INFO);
        if(session.action().equals("onCheckPermission")){
            Account acc = new UserAccount();
            acc.distributionKey(session.systemId());
            account.load(acc);
            session.write(new PermissionContext(maxGameClusterCount,acc.gameClusterCount(0)).toJson().toString().getBytes(),label());
        }
        else if(session.action().equals("onGameClusterList")){
            this.context.log(new String(payload),OnLog.WARN);
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            int index = ((Number)onAccess.property("index")).intValue();
            AdminContext adminContext = new AdminContext();
            adminContext.gameClusterList = new ArrayList<>();
            IndexSet idx = new IndexSet();
            idx.distributionKey(session.systemId());
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
            GameLobbyContext lobbyContext = pendingLobby.computeIfAbsent(accessId,(k)-> {
                GameLobbyContext presenceContext = new GameLobbyContext();
                presenceContext.gameLobbyList = new ArrayList<>();
                presenceContext.successful(true);
                GameCluster gc = this.deploymentServiceProvider.gameCluster((String) onAccess.property(OnAccess.ACCESS_ID));
                //query from master node to make sure data available always
                Lobby lobby = this.deploymentServiceProvider.lobby((String) gc.property(GameCluster.GAME_LOBBY));
                lobby.entryList().forEach((a) -> {
                    GameLobby gameLobby = new GameLobby();
                    gameLobby.lobby = a;
                    gameLobby.zone = _zone(gc, a);
                    presenceContext.gameLobbyList.add(gameLobby);
                });
                return presenceContext;
            });
            lobbyContext.page = page;
            session.write(lobbyContext.toJson().toString().getBytes(),label());
        }
        else if(session.action().equals("onGameServiceList")){
            PresenceContext presenceContext = new PresenceContext();
            presenceContext.lobbyList = new ArrayList<>();
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            GameCluster gc = this.deploymentServiceProvider.gameCluster((String)onAccess.property(OnAccess.ACCESS_ID));
            presenceContext.lobbyList.add(this.deploymentServiceProvider.lobby((String) gc.property(GameCluster.GAME_SERVICE)));
            session.write(this.builder.create().toJson(presenceContext).getBytes(),label());
        }
        else if(session.action().equals("onGameDataList")){
            PresenceContext presenceContext = new PresenceContext();
            presenceContext.lobbyList = new ArrayList<>();
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            GameCluster gc = this.deploymentServiceProvider.gameCluster((String)onAccess.property(OnAccess.ACCESS_ID));
            presenceContext.lobbyList.add(this.deploymentServiceProvider.lobby((String) gc.property(GameCluster.GAME_DATA)));
            session.write(this.builder.create().toJson(presenceContext).getBytes(),label());
            //this.deploymentServiceProvider.shutdown((String) gc.property(GameCluster.GAME_SERVICE));
            //this.deploymentServiceProvider.shutdown((String) gc.property(GameCluster.GAME_DATA));
            //this.deploymentServiceProvider.shutdown((String) gc.property(GameCluster.GAME_LOBBY));

            //GameServiceProvider gsp = this.context.serviceProvider((String) gc.property(GameCluster.GAME_SERVICE));
            //this.deploymentServiceProvider.release(gsp);
            //this.context.log(gsp.name(),OnLog.WARN);
        }
        else if(session.action().equals("onShoppingList")){
            session.write(new ShoppingContext(monthly,yearly).toJson().toString().getBytes(),this.label());
        }
        else if(session.action().equals("onMetrics")){
            Metrics  metrics = this.deploymentServiceProvider.metrics();
            AdminContext adminContext = new AdminContext();
            adminContext.metrics = metrics;
            session.write(adminContext.toJson().toString().getBytes(),label());
        }
        else if(session.action().equals("onCreateAccessKey")){
            //generate access key from game cluster id
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            String key = tokenValidatorProvider.accessKey((String)onAccess.property("gameClusterId"));
            //this.context.log("TEST->"+tokenValidatorProvider.validateAccessKey(key),OnLog.WARN);
            session.write(new PermissionContext(key).toJson().toString().getBytes(),label());
        }
        else if(session.action().equals("onCreateGameCluster")){
            Account acc = new UserAccount();
            acc.distributionKey(session.systemId());
            if(account.load(acc)&&acc.gameClusterCount(0)<maxGameClusterCount){
                OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
                GameCluster gc = this.deploymentServiceProvider.createGameCluster(session.systemId(),(String)onAccess.property("name"),"basic");
                if(gc.successful()){
                    IndexSet idx = new IndexSet();
                    idx.distributionKey(acc.distributionKey());
                    idx.label(Account.GameClusterLabel);
                    idx.keySet.add(gc.distributionKey());
                    if(!account.createIfAbsent(idx,true)){
                        idx.keySet.add(gc.distributionKey());//update on existing
                        account.update(idx);
                    }
                    idx.keySet.forEach((k)->{
                       this.context.log("KEY->"+k,OnLog.WARN);
                    });
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
            this.context.log(new String(payload),OnLog.WARN);
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            String accessId = (String) onAccess.property(OnAccess.ACCESS_ID);
            GameLobbyContext pending = pendingLobby.get(accessId);
            GameLobby gameLobby = pending.gameLobbyList.get(pending.page);
            Zone zone = gameLobby.zone;
            zone.rank = ((Number)onAccess.property("rank")).intValue();
            zone.capacity = ((Number)onAccess.property("capacity")).intValue();
            zone.roundDuration = ((Number)onAccess.property("duration")).intValue()*60000;
            zone.playMode = ((Number)onAccess.property("playMode")).intValue();
            zone.update();
            session.write(pending.toJson().toString().getBytes(),label());
        }
        else if(session.action().equals("onUpdateGameLevel")){
            this.context.log(new String(payload),OnLog.WARN);
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            String accessId = (String) onAccess.property(OnAccess.ACCESS_ID);
            int index = ((Number)onAccess.property("index")).intValue();
            GameLobbyContext pending = pendingLobby.get(accessId);
            GameLobby gameLobby = pending.gameLobbyList.get(pending.page);
            Zone zone = gameLobby.zone;
            zone.arenas[index].name(onAccess.name());
            zone.arenas[index].xp = ((Number)onAccess.property("xp")).doubleValue();
            zone.arenas[index].level = ((Number)onAccess.property("level")).intValue();
            zone.arenas[index].disabled((Boolean)onAccess.property("disabled"));
            zone.update();
            session.write(pending.toJson().toString().getBytes(),label());
        }
        else if(session.action().equals("onLaunchGameCluster")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            String accessId = (String) onAccess.property(OnAccess.ACCESS_ID);
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(accessId);
            Account acc = new UserAccount();
            acc.distributionKey(session.systemId());
            account.load(acc);
            if(acc.trial()||acc.subscribed()){
                this.deploymentServiceProvider.launchGameCluster(gameCluster);
                gameCluster.property(GameCluster.DISABLED,false);
                gameCluster.update();
                session.write(this.builder.create().toJson(new ResponseHeader(session.action(), "launch event submitted", true)).getBytes(), label());
            }
            else {
                session.write(this.builder.create().toJson(new ResponseHeader(session.action(), "no subscription or trial found", false)).getBytes(), label());
            }
        }
        else if(session.action().equals("onCommit")){
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

                //charge successfully
                //OnBalanceTrack onBalanceTrack = new OnBalanceTrack(session.systemId(),co.credits);
                //this.context.publish(this.context.routingKey(session.systemId(),"presence"),onBalanceTrack);
                //suc = true;
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
            this.deploymentServiceProvider.shutdownGameCluster(gc);
            gc.property(GameCluster.DISABLED,true);
            gc.update();
            session.write(this.builder.create().toJson(new ResponseHeader(session.action(),"updated",true)).getBytes(),label());
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
        monthly = new SubscriptionFee("monthlyAccess",ma.property("description"),ma.property("price"),ma.property("currency"));
        yearly = new SubscriptionFee("yearlyAccess",ya.property("description"),ya.property("price"),ya.property("currency"));
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.builder.registerTypeAdapter(PresenceContext.class,new PresenceContextSerializer());
        this.account = this.context.dataStore(Account.DataStore);
        this.tokenValidatorProvider = this.context.serviceProvider(TokenValidatorProvider.NAME);
        this.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        this.maxGameClusterCount = Integer.parseInt(this.context.configuration("setup").property("maxGameClusterCount"));
        this.defaultGameLevelCount = Integer.parseInt(this.context.configuration("setup").property("defaultGameLevelCount"));
        this.maxGameLevelCount = Integer.parseInt(this.context.configuration("setup").property("maxGameLevelCount"));
        this.pendingLobby = new ConcurrentHashMap<>();
        this.context.log("Admin role module started with max game cluster count ["+maxGameClusterCount+"]", OnLog.INFO);
    }
    @Override
    public String label() {
        return "admin-role";
    }
    private Zone _zone(GameCluster gameCluster,Descriptor descriptor){
        DataStore dataStore = this.context.dataStore((String) gameCluster.property(GameCluster.GAME_SERVICE));
        Zone mZone = new Zone();
        mZone.distributionKey(descriptor.distributionKey());
        mZone.rank = descriptor.accessRank();
        mZone.capacity=1;
        mZone.roundDuration = 60*1000;
        mZone.overtime = 5000;
        mZone.playMode = Room.OFF_LINE_MODE;
        mZone.arenas = new Arena[maxGameLevelCount];
        for(int i=1;i<maxGameLevelCount+1;i++){
            mZone.arenas[i-1]=new Arena(i,i*100,"Level "+i,i>defaultGameLevelCount);
        }
        mZone.dataStore(dataStore);
        dataStore.createIfAbsent(mZone,true);
        return mZone;
    }
}
