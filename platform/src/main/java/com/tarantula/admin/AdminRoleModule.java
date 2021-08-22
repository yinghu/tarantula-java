package com.tarantula.admin;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.TimeUtil;

import com.tarantula.platform.*;
import com.tarantula.platform.item.Item;
import com.tarantula.platform.item.ItemQuery;
import com.tarantula.platform.presence.*;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.service.Metrics;
import com.tarantula.platform.util.OnAccessDeserializer;
import com.tarantula.platform.util.ResponseSerializer;
import com.tarantula.platform.util.SystemUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminRoleModule implements Module,Configurable.Listener {

    private ApplicationContext context;
    private GsonBuilder builder;
    private DataStore account;
    private DataStore user;
    private DataStore purchase;
    private DeploymentServiceProvider deploymentServiceProvider;
    private TokenValidatorProvider tokenValidatorProvider;
    private int maxGameClusterCount;
    private SubscriptionFee monthly;
    private SubscriptionFee yearly;

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        if(session.action().equals("onCheckPermission")){
            User user = _user(session.systemId());
            Account acc = new UserAccount();
            acc.distributionKey(user.primary()?session.systemId():user.owner());
            account.load(acc);
            boolean ex = this.tokenValidatorProvider.checkSubscription(user.primary()?session.systemId():user.owner());
            session.write(new PermissionContext(maxGameClusterCount,acc.gameClusterCount(0),!ex).toJson().toString().getBytes());
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
            session.write(adminContext.toJson().toString().getBytes());
        }
        else if(session.action().equals("onShoppingList")){
            session.write(new ShoppingContext(monthly,yearly).toJson().toString().getBytes());
        }
        else if(session.action().equals("onCreateAccessKey")){
            //generate access key from game cluster id
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            String key = tokenValidatorProvider.gameClusterAccessKey((String)onAccess.property("gameClusterId"));
            session.write(new PermissionContext(key).toJson().toString().getBytes());
        }
        else if(session.action().equals("onTestAccessKey")){
            //test access key
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            String key = tokenValidatorProvider.validateGameClusterAccessKey((String)onAccess.property(OnAccess.ACCESS_KEY));
            session.write(JsonUtil.toSimpleResponse(key!=null,key!=null?"key passed":"key failed").getBytes());
        }
        else if(session.action().equals("onCreateGameCluster")){
            User ua = _user(session.systemId());
            Account acc = new UserAccount();
            acc.distributionKey(ua.primary()?session.systemId():ua.owner());
            if(account.load(acc)&&acc.gameClusterCount(0)<maxGameClusterCount){
                OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
                //context.log("MODE->"+onAccess.property("playMode").toString(),OnLog.WARN);
                GameCluster gc = this.deploymentServiceProvider.createGameCluster(acc.distributionKey(),(String)onAccess.property("name"),(String) onAccess.property("playMode"),(boolean)onAccess.property("tournamentEnabled"));
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
                    acc.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
                    account.update(acc);
                    gc.message("Game cluster created successfully");
                }
                session.write(gc.toJson().toString().getBytes());
            }
            else{
                //reach max count
                session.write(this.builder.create().toJson(new ResponseHeader(session.action(),"you already have max game clusters",false)).getBytes());
            }
        }
        else if(session.action().equals("availableGameServiceList")){
            List<ExposedGameService> alist = this.deploymentServiceProvider.gameServiceList();
            session.write(toJson(alist));
        }
        else if(session.action().equals("onAddService")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            String accessId = (String) onAccess.property(OnAccess.ACCESS_ID);
            GameCluster gameCluster = deploymentServiceProvider.gameCluster(accessId);
            String name = (String)gameCluster.property(GameCluster.NAME);
            String typeId = (String)gameCluster.property(GameCluster.GAME_SERVICE);
            Lobby _lobby = this.deploymentServiceProvider.lobby(typeId);
            boolean[] _existed = {false};
            String _serviceName = onAccess.property("serviceName").toString();
            _lobby.entryList().forEach((e)->{
                if(e.tag().equals(name+"/"+_serviceName)){
                    _existed[0] = true;
                }
            });
            if(_existed[0]){
                _existed[0]=false;
            }else {
                ExposedGameService exposedGameService = this.deploymentServiceProvider.gameService(_serviceName);
                if(exposedGameService!=null){
                    DeploymentDescriptor desc = new DeploymentDescriptor();
                    desc.typeId(typeId);
                    desc.type("application");
                    desc.name(_serviceName);
                    desc.category("service");
                    desc.tag(name.toLowerCase() + "/"+_serviceName);
                    desc.moduleId(exposedGameService.property(ExposedGameService.MODULE_ID).toString());
                    desc.index(exposedGameService.property(ExposedGameService.MODULE_INDEX).toString());
                    desc.codebase(exposedGameService.property(ExposedGameService.MODULE_CODE_BASE).toString());
                    desc.moduleArtifact(exposedGameService.property(ExposedGameService.MODULE_ARTIFACT).toString());
                    desc.moduleVersion(exposedGameService.property(ExposedGameService.MODULE_VERSION).toString());
                    desc.moduleName(exposedGameService.property(ExposedGameService.MODULE_NAME).toString());
                    desc.deployPriority((Integer)exposedGameService.property(ExposedGameService.DEPLOY_PRIORITY));
                    desc.accessControl((Integer)exposedGameService.property(ExposedGameService.ACCESS_CONTROL));
                    desc.applicationClassName("com.tarantula.platform.service.deployment.SingletonModuleApplication");
                    _existed[0] = this.deploymentServiceProvider.createApplication(desc,null, null,true);
                }
            }
            session.write(JsonUtil.toSimpleResponse(_existed[0],_existed[0]?"created":"failed").getBytes());
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
                session.write(this.builder.create().toJson(new ResponseHeader(session.action(),suc?"operation successfully":"operation failed",suc)).getBytes());
            }
            else {
                session.write(this.builder.create().toJson(new ResponseHeader(session.action(), "no subscription or trial found", false)).getBytes());
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
                session.write(this.builder.create().toJson(new ResponseHeader("onCommit", "your purchase is successful", true)).getBytes());
            }
            else{
                session.write(this.builder.create().toJson(new ResponseHeader("onCommit", "failed to commit your purchase", false)).getBytes());
            }
        }
        else if(session.action().equals("onShutdownGameCluster")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            String accessId = (String) onAccess.property(OnAccess.ACCESS_ID);
            GameCluster gc = this.deploymentServiceProvider.gameCluster(accessId);
            boolean suc = this.deploymentServiceProvider.shutdownGameCluster(gc);
            session.write(this.builder.create().toJson(new ResponseHeader(session.action(),suc?"operation successfully":"operation failed",suc)).getBytes());
        }
        else{
            session.write(this.builder.create().toJson(new ResponseHeader("onError", session.action()+" operation not supported", false)).getBytes());
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        Configuration ya = this.context.configuration("yearlyAccess");
        Configuration ma = this.context.configuration("monthlyAccess");
        monthly = new SubscriptionFee("monthlyAccess",ma.property("description").toString(),ma.property("price").toString(),ma.property("currency").toString(),Integer.parseInt(ma.property("durationMonths").toString()));
        yearly = new SubscriptionFee("yearlyAccess",ya.property("description").toString(),ya.property("price").toString(),ya.property("currency").toString(),Integer.parseInt(ya.property("durationMonths").toString()));
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.account = this.context.dataStore(Account.DataStore);
        this.user = this.context.dataStore(Access.DataStore);
        this.purchase = this.context.dataStore(SubscriptionFee.DataStore);
        this.tokenValidatorProvider = this.context.serviceProvider(TokenValidatorProvider.NAME);
        this.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        ya.registerListener(this);
        ma.registerListener(this);
        this.deploymentServiceProvider.register(ya);
        this.deploymentServiceProvider.register(ma);
        this.maxGameClusterCount = Integer.parseInt(this.context.configuration("cluster").property("maxGameClusterCount").toString());
        this.context.log("Admin role module started with max game cluster count ["+maxGameClusterCount+"]", OnLog.INFO);
    }

    private User _user(String systemId){
        User u = new User();
        u.distributionKey(systemId);
        if(user.load(u)){
            return u;
        }
        return null;
    }
    private byte[] toJson(List<ExposedGameService> exposedGameServices){
        JsonObject jsonObject = new JsonObject();
        JsonArray array = new JsonArray();
        exposedGameServices.forEach((es)->{
            JsonObject jes = new JsonObject();
            jes.addProperty("name",es.name());
            jes.addProperty("description",es.property("description").toString());
            array.add(jes);
        });
        jsonObject.add("gameServiceList",array);
        return jsonObject.toString().getBytes();
    }

    public void onUpdated(Configuration configuration){

    }
}
