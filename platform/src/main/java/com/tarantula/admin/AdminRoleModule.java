package com.tarantula.admin;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.service.UserService;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.TimeUtil;

import com.tarantula.platform.*;
import com.tarantula.platform.presence.*;
import com.tarantula.platform.service.metrics.StatisticsSerializer;
import com.tarantula.platform.util.OnAccessDeserializer;
import com.tarantula.platform.util.ResponseSerializer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminRoleModule implements Module{

    private ApplicationContext context;
    private GsonBuilder builder;

    private DeploymentServiceProvider deploymentServiceProvider;
    private TokenValidatorProvider tokenValidatorProvider;

    private UserService userService;
    private int maxGameClusterCount;

    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        if(session.action().equals("onCheckPermission")){
            Access user = _user(session.systemId());
            Account acc = this.userService.loadAccount(user);
            boolean ex = this.tokenValidatorProvider.checkSubscription(user.primary()?session.systemId():user.owner());
            session.write(new PermissionContext(maxGameClusterCount,acc.gameClusterCount(0),!ex).toJson().toString().getBytes());
        }
        else if(session.action().equals("onGameClusterMetricsCategory")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            Metrics metrics = context.metrics((String) gameCluster.property(GameCluster.GAME_SERVICE));
            List<String> categories = metrics.categories();
            JsonObject m = new JsonObject();
            JsonArray ms = new JsonArray();
            categories.forEach(category->ms.add(category));
            m.add("categories",ms);
            session.write(m.toString().getBytes());
        }
        else if(session.action().equals("onGameClusterMetrics")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            Metrics metrics = context.metrics((String) gameCluster.property(GameCluster.GAME_SERVICE));
            MetricsContext metricsContext = new MetricsContext();
            metricsContext.metrics = metrics;
            session.write(metricsContext.toJson().toString().getBytes());
        }
        else if(session.action().equals("onGameClusterList")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            Access user = _user(session.systemId());
            int index = ((Number)onAccess.property("index")).intValue();
            GameClusterContext adminContext = new GameClusterContext();
            adminContext.gameClusterList = new ArrayList<>();
            adminContext.index = index;
            IndexSet idx = this.userService.loadGameClusterIndex(user);
            if(idx!=null){
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
            String key = tokenValidatorProvider.createGameClusterAccessKey(session.name());
            session.write(new PermissionContext(key).toJson().toString().getBytes());
        }
        else if(session.action().equals("onTestAccessKey")){
            //test access key
            GameCluster key = tokenValidatorProvider.validateGameClusterAccessKey(session.name());
            //this.context.log(key.property(GameCluster.GAME_LOBBY).toString(),OnLog.WARN);
            session.write(JsonUtil.toSimpleResponse(key!=null,key!=null?"key passed":"key failed").getBytes());
        }
        else if(session.action().equals("onRevokeAccessKey")){
            //revoke access key
            String[] query = session.name().split("#");
            tokenValidatorProvider.revokeAccessKey(query[1]);
            List<String> keys = tokenValidatorProvider.gameClusterAccessKeyList(query[0]);
            session.write(new PermissionContext(keys).toJson().toString().getBytes());
        }
        else if(session.action().equals("onCreateDeveloperKey")){
            //generate access key from game cluster id
            String key = tokenValidatorProvider.createGameClusterAccessKey(session.name());
            session.write(new PermissionContext(key).toJson().toString().getBytes());
        }
        else if(session.action().equals("onDeveloperKeyList")){
            //list access key from game cluster id
            List<String> keys = tokenValidatorProvider.gameClusterAccessKeyList(session.name());
            session.write(new PermissionContext(keys).toJson().toString().getBytes());
        }
        else if(session.action().equals("onCreateGameCluster")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            String pendingName = (String)onAccess.property("name");
            if(!checkGameClusterName(pendingName)){
                session.write(JsonUtil.toSimpleResponse(false,"letter and number only with 4 chars at least").getBytes());
            }
            else{
                Access ua = _user(session.systemId());
                Account acc = userService.loadAccount(ua);
                if(acc.gameClusterCount(0)<maxGameClusterCount){
                    GameCluster gc = this.deploymentServiceProvider.createGameCluster(acc.distributionKey(),pendingName,(String) onAccess.property("playMode"),(boolean)onAccess.property("tournamentEnabled"));
                    if(gc.successful()){
                        IndexSet idx = this.userService.loadGameClusterIndex(ua);
                        idx.addKey(gc.distributionKey());
                        idx.update();
                        acc.gameClusterCount(1);
                        acc.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
                        acc.update();
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
            List<Descriptor> alist = this.deploymentServiceProvider.gameServiceList();
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
                Descriptor exposedGameService = this.deploymentServiceProvider.gameService(_serviceName);
                if(exposedGameService!=null){
                    DeploymentDescriptor desc = new DeploymentDescriptor();
                    desc.typeId(typeId);
                    desc.type("application");
                    desc.name(_serviceName);
                    desc.category("service");
                    desc.tag(name.toLowerCase() + "/"+_serviceName);
                    //desc.moduleId(exposedGameService.property(ExposedGameService.MODULE_ID).toString());
                    //desc.index(exposedGameService.property(ExposedGameService.MODULE_INDEX).toString());
                    //desc.codebase(exposedGameService.property(ExposedGameService.MODULE_CODE_BASE).toString());
                    //desc.moduleArtifact(exposedGameService.property(ExposedGameService.MODULE_ARTIFACT).toString());
                    //desc.moduleVersion(exposedGameService.property(ExposedGameService.MODULE_VERSION).toString());
                    //desc.moduleName(exposedGameService.property(ExposedGameService.MODULE_NAME).toString());
                    //desc.deployPriority((Integer)exposedGameService.property(ExposedGameService.DEPLOY_PRIORITY));
                    //desc.accessControl((Integer)exposedGameService.property(ExposedGameService.ACCESS_CONTROL));
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
            Access _u = _user(session.systemId());
            Account acc = userService.loadAccount(_u);
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
        this.tokenValidatorProvider = this.context.serviceProvider(TokenValidatorProvider.NAME);
        this.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        this.userService = this.context.serviceProvider(UserService.NAME);
        this.maxGameClusterCount = ((Number)this.context.configuration("cluster").property("maxGameClusterCount")).intValue();
        this.context.log("Admin role module started with max game cluster count ["+maxGameClusterCount+"]", OnLog.INFO);
    }

    private Access _user(String systemId){
        return this.userService.loadUser(systemId);
    }
    private byte[] toJson(List<Descriptor> exposedGameServices){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",true);
        JsonArray array = new JsonArray();
        exposedGameServices.forEach((es)->{
            array.add(es.toJson());
        });
        jsonObject.add("gameServiceList",array);
        return jsonObject.toString().getBytes();
    }
    private boolean checkGameClusterName(String name){
        return name!=null&&name.length()>3&&name.chars().allMatch(Character::isLetterOrDigit);
    }
}
