package com.tarantula.admin;

import com.google.gson.GsonBuilder;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.game.GameLobby;
import com.tarantula.game.GameLobbyContext;
import com.tarantula.game.service.GameServiceProvider;
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
    private SubscriptionFee monthly;
    private SubscriptionFee yearly;

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
            GameLobbyContext presenceContext = new GameLobbyContext();
            presenceContext.gameLobbyList = new ArrayList<>();
            presenceContext.successful(true);
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            int page = ((Number)onAccess.property("page")).intValue();
            presenceContext.page = page;
            GameCluster gc = this.deploymentServiceProvider.gameCluster((String)onAccess.property(OnAccess.ACCESS_ID));
            Lobby lobby = this.context.lobby((String) gc.property(GameCluster.GAME_LOBBY));
            GameServiceProvider gameServiceProvider = this.context.serviceProvider((String) gc.property(GameCluster.GAME_SERVICE));
            lobby.entryList().forEach((a)->{
                GameLobby gameLobby = new GameLobby();
                gameLobby.lobby =a;
                gameLobby.zone = gameServiceProvider.zone(a.distributionKey());
                presenceContext.gameLobbyList.add(gameLobby);
            });
            //presenceContext.gameLobbyList.add(this.context.lobby((String) gc.property(GameCluster.GAME_LOBBY)));
            session.write(presenceContext.toJson().toString().getBytes(),label());
            //gameServiceProvider.zone()
        }
        else if(session.action().equals("onGameServiceList")){
            PresenceContext presenceContext = new PresenceContext();
            presenceContext.lobbyList = new ArrayList<>();
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            GameCluster gc = this.deploymentServiceProvider.gameCluster((String)onAccess.property(OnAccess.ACCESS_ID));
            presenceContext.lobbyList.add(this.context.lobby((String) gc.property(GameCluster.GAME_SERVICE)));
            session.write(this.builder.create().toJson(presenceContext).getBytes(),label());
        }
        else if(session.action().equals("onGameDataList")){
            PresenceContext presenceContext = new PresenceContext();
            presenceContext.lobbyList = new ArrayList<>();
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            GameCluster gc = this.deploymentServiceProvider.gameCluster((String)onAccess.property(OnAccess.ACCESS_ID));
            presenceContext.lobbyList.add(this.context.lobby((String) gc.property(GameCluster.GAME_DATA)));
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
                    if(acc.trial()||acc.subscribed()){
                        //launch the game
                        this.deploymentServiceProvider.launchGameCluster(gc);
                    }
                }
                session.write(this.builder.create().toJson(new ResponseHeader(session.action(),gc.message(),gc.successful())).getBytes(),label());
            }
            else{
                //reach max count
                session.write(this.builder.create().toJson(new ResponseHeader(session.action(),"you already have max game clusters",false)).getBytes(),label());
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
            //this.deploymentServiceProvider.shutdown()
        }
        else{
            session.write(this.builder.create().toJson(new ResponseHeader("onError", "operation not supported", false)).getBytes(),this.label());
        }
        /**
        else if(session.action().equals("backup")){
            DataStoreProvider dp = deploymentServiceProvider.dataStoreProvider();
            dp.backup(Distributable.DATA_SCOPE);
            dp.backup(Distributable.INTEGRATION_SCOPE);
            session.write(this.builder.create().toJson(_message("incremental backup data store")).getBytes(),label());
        }
        else if(session.action().equals("list")){
            AdminDataStoreObject dao = _message("count of data store list");
            dao.reset(this.context);
            session.write(this.builder.create().toJson(dao).getBytes(),label());
        }**/
        return session.action().equals("onLeave");
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
        this.context.log("Admin role module started with max game cluster count ["+maxGameClusterCount+"]", OnLog.INFO);
    }
    @Override
    public String label() {
        return "admin-role";
    }
    private AdminDataStoreObject _message(String message){
        return new AdminDataStoreObject(message,label());
    }
}
