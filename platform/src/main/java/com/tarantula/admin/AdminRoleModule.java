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
import com.tarantula.platform.presence.*;
import com.tarantula.platform.util.OnAccessDeserializer;
import com.tarantula.platform.util.ResponseSerializer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminRoleModule implements Module{

    private ApplicationContext context;
    private GsonBuilder builder;
    private DataStore account;
    private DataStore accountIndex;
    private DataStore user;

    private DeploymentServiceProvider deploymentServiceProvider;
    private TokenValidatorProvider tokenValidatorProvider;
    private int maxGameClusterCount;

    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
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
            if(accountIndex.load(idx)){
                idx.keySet().forEach((k)->{
                    GameCluster g = this.deploymentServiceProvider.gameCluster(k);
                    if(g!=null){
                        adminContext.gameClusterList.add(g);
                    }
                });
            }
            session.write(adminContext.toJson().toString().getBytes());
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
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            String pendingName = (String)onAccess.property("name");
            if(!checkGameClusterName(pendingName)){
                session.write(JsonUtil.toSimpleResponse(false,"letter and number only with 4 chars at least").getBytes());
            }
            else{
                User ua = _user(session.systemId());
                Account acc = new UserAccount();
                acc.distributionKey(ua.primary()?session.systemId():ua.owner());
                if(account.load(acc)&&acc.gameClusterCount(0)<maxGameClusterCount){
                    //context.log("MODE->"+onAccess.property("playMode").toString(),OnLog.WARN);
                    GameCluster gc = this.deploymentServiceProvider.createGameCluster(acc.distributionKey(),pendingName,(String) onAccess.property("playMode"),(boolean)onAccess.property("tournamentEnabled"));
                    if(gc.successful()){
                        IndexSet idx = new IndexSet();
                        idx.distributionKey(acc.distributionKey());
                        idx.label(Account.GameClusterLabel);
                        idx.addKey(gc.distributionKey());
                        if(!accountIndex.createIfAbsent(idx,true)){
                            idx.addKey(gc.distributionKey());//update on existing
                            accountIndex.update(idx);
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
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.account = this.context.dataStore(Account.DataStore);
        this.accountIndex = this.context.dataStore(Account.IndexDataStore);
        this.user = this.context.dataStore(Access.DataStore);
        this.tokenValidatorProvider = this.context.serviceProvider(TokenValidatorProvider.NAME);
        this.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        this.maxGameClusterCount = ((Number)this.context.configuration("cluster").property("maxGameClusterCount")).intValue();
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
    private boolean checkGameClusterName(String name){
        return name!=null&&name.length()>3&&name.chars().allMatch(Character::isLetterOrDigit);
    }
}
