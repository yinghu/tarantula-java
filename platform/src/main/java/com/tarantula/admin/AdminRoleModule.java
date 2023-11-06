package com.tarantula.admin;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.service.UserService;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.TimeUtil;

import com.tarantula.platform.*;
import com.tarantula.platform.presence.*;
import com.tarantula.platform.util.OnAccessDeserializer;
import com.tarantula.platform.util.ResponseSerializer;
import com.tarantula.platform.util.SystemUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AdminRoleModule implements Module{

    private ApplicationContext context;
    private GsonBuilder builder;

    private DeploymentServiceProvider deploymentServiceProvider;
    private TokenValidatorProvider tokenValidatorProvider;

    private UserService userService;
    private int maxGameClusterCount;
    private Configuration gameClusterConfiguration;

    private ConcurrentHashMap<String,Descriptor> pendingGameServices;

    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        if(session.action().equals("onCheckPermission")){
            Access user = _user(session.distributionId());
            Account acc = this.userService.loadAccount(user);
            boolean ex = this.tokenValidatorProvider.checkSubscription(user.primary()?session.distributionId():user.primaryId());
            session.write(new PermissionContext(maxGameClusterCount,acc.gameClusterCount(0),!ex).toJson().toString().getBytes());
        }
        else if(session.action().equals("onGameClusterList")){
            //OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            Access user = _user(session.distributionId());
            int index = Integer.parseInt(session.name());
            GameClusterContext adminContext = new GameClusterContext();
            adminContext.gameClusterList = this.deploymentServiceProvider.gameClusterList(user);
            adminContext.index = index;
            session.write(adminContext.toJson().toString().getBytes());
        }
        else if(session.action().equals("onLoadGameCluster")){
            GameCluster g = this.deploymentServiceProvider.gameCluster(Long.parseLong(session.name()));
            g.successful(true);
            session.write(g.toJson().toString().getBytes());
        }
        else if(session.action().equals("onEditGameCluster")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            GameCluster g = this.deploymentServiceProvider.updateGameCluster(Long.parseLong(session.name()),onAccess);
            session.write(g.toJson().toString().getBytes());
        }
        else if(session.action().equals("onCreateAccessKey")){
            //generate access key from game cluster id
            String key = tokenValidatorProvider.createGameClusterAccessKey(Long.parseLong(session.name()));
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
            List<String> keys = tokenValidatorProvider.gameClusterAccessKeyList(Long.parseLong(query[0]));
            session.write(new PermissionContext(keys).toJson().toString().getBytes());
        }
        else if(session.action().equals("onCreateDeveloperKey")){
            //generate access key from game cluster id
            String key = tokenValidatorProvider.createGameClusterAccessKey(Long.parseLong(session.name()));
            session.write(new PermissionContext(key).toJson().toString().getBytes());
        }
        else if(session.action().equals("onDeveloperKeyList")){
            //list access key from game cluster id
            List<String> keys = tokenValidatorProvider.gameClusterAccessKeyList(Long.parseLong(session.name()));
            session.write(new PermissionContext(keys).toJson().toString().getBytes());
        }
        else if(session.action().equals("onCreateGameCluster")){
            //this.context.log(new String(payload),OnLog.WARN);
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            String pendingName = (String)onAccess.property("name");
            if(!checkGameClusterName(pendingName)){
                session.write(JsonUtil.toSimpleResponse(false,"letter and number only with 4 chars at least").getBytes());
            }
            else{
                Access ua = _user(session.distributionId());
                Account acc = userService.loadAccount(ua);
                if(acc.gameClusterCount(0)<maxGameClusterCount){
                    onAccess.property(OnAccess.GAME_CLUSTER_CONFIG,this.gameClusterConfiguration);
                    GameCluster gc = this.deploymentServiceProvider.createGameCluster(acc,pendingName,onAccess);
                    if(gc.successful()){
                        this.context.log(gc.distributionId()+"",OnLog.WARN);
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
            GameCluster gameCluster = deploymentServiceProvider.gameCluster(Long.parseLong(session.name()));
            Map<String,Descriptor> lmap = new HashMap<>();
            gameCluster.serviceLobby.entryList().forEach(a->lmap.put(a.name(),a));
            List<Descriptor> alist = this.deploymentServiceProvider.gameServiceList();
            session.write(toJson(lmap,alist));
        }
        else if(session.action().equals("onAddService")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = deploymentServiceProvider.gameCluster(Long.parseLong(query[0]));
            Descriptor pendingService = pendingGameServices.get(query[1]);
            String name = gameCluster.name();//.property(GameCluster.NAME);
            String typeId = gameCluster.typeId();//(GameCluster.GAME_SERVICE);
            if(pendingService!=null){
                Descriptor desc = pendingService.copy();
                desc.typeId(typeId);//replaced with named type id
                desc.moduleId(typeId);
                desc.tag(desc.tag().replaceFirst("game",name.toLowerCase()));
                boolean suc = this.deploymentServiceProvider.createApplication(desc,null,true);
                session.write(JsonUtil.toSimpleResponse(suc,suc?"service ["+desc.name()+"] Added":"service ["+desc.name()+"] Not Added").getBytes());
            }else{
                session.write(JsonUtil.toSimpleResponse(false,"game service not existed").getBytes());
            }
        }
        else if(session.action().equals("onUpdateService")){
            String[] query = session.name().split("#");
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            GameCluster gameCluster = deploymentServiceProvider.gameCluster(Long.parseLong(query[0]));
            boolean[] suc ={false};
            gameCluster.serviceLobby.entryList().forEach(descriptor -> {
                if(descriptor.tag().equals(query[1])){
                    suc[0]=this.deploymentServiceProvider.updateApplication(descriptor,onAccess);
                    if(suc[0]){
                        this.deploymentServiceProvider.disableApplication(descriptor.distributionId());
                        this.deploymentServiceProvider.enableApplication(descriptor.distributionId());
                    }
                }
            });
            session.write(JsonUtil.toSimpleResponse(suc[0],session.name()).getBytes());
        }
        else if(session.action().equals("onLaunchGameCluster")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(Long.parseLong(session.name()));
            Access _u = _user(session.distributionId());
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
            GameCluster gc = this.deploymentServiceProvider.gameCluster(Long.parseLong(session.name()));
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
        this.gameClusterConfiguration = this.context.configuration("cluster");
        this.maxGameClusterCount = ((Number)this.gameClusterConfiguration.property("maxGameClusterCount")).intValue();
        this.pendingGameServices = new ConcurrentHashMap<>();
        this.context.log("Admin role module started with max game cluster count ["+maxGameClusterCount+"]", OnLog.INFO);
    }

    private Access _user(long systemId){
        return this.userService.loadUser(systemId);
    }
    private byte[] toJson(Map<String,Descriptor> existed,List<Descriptor> exposedGameServices){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",true);
        JsonArray pending = new JsonArray();
        JsonArray listing = new JsonArray();
        exposedGameServices.forEach((a)->{
            if(!existed.containsKey(a.name())){
                JsonObject js = a.toJson();
                String applicationId = SystemUtil.oid();
                js.addProperty("applicationId", applicationId);
                pendingGameServices.put(applicationId,a);
                pending.add(js);
            }
        });
        existed.forEach((k,v)->listing.add(v.toJson()));
        jsonObject.add("runningList",listing);
        jsonObject.add("pendingList",pending);
        return jsonObject.toString().getBytes();
    }
    private boolean checkGameClusterName(String name){
        return name!=null&&name.length()>3&&name.chars().allMatch(Character::isLetterOrDigit);
    }
}
