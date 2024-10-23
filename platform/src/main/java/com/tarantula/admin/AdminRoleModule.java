package com.tarantula.admin;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.lmdb.LMDBDataStoreProvider;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.*;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.SnowflakeKey;
import com.icodesoftware.util.TimeUtil;

import com.perfectday.games.earth8.inbox.PlayerActionQuery;
import com.tarantula.platform.*;
import com.tarantula.platform.inbox.*;
import com.tarantula.platform.presence.*;
import com.tarantula.platform.util.OnAccessDeserializer;
import com.tarantula.platform.util.ResponseSerializer;
import com.tarantula.platform.util.SystemUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private AccessIndexService accessIndexService;

    private int maxGameClusterCount;
    private Configuration gameClusterConfiguration;

    private ConcurrentHashMap<String,Descriptor> pendingGameServices;

    private TarantulaLogger logger = JDKLogger.getLogger(LMDBDataStoreProvider.class);


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
        else if(session.action().equals("onItemGrantEvent")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(Long.parseLong(session.name()));

            DataStore dataStore = gameCluster.applicationPreSetup().onDataStore("player_inventory_grant");

            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            String playerID = (String)onAccess.property("playerID");
            String amount = (String)onAccess.property("itemAmount");
            String itemID = (String)onAccess.property("itemID");
            String itemName = (String)onAccess.property("itemName");

            PlatformServerEvent serverGrantEvent = new PlatformServerEvent("ItemGrant--" + itemID + "--" + amount,false);
            serverGrantEvent.ownerKey(SnowflakeKey.from(Long.parseLong(playerID)));

            if(dataStore.create(serverGrantEvent)){
                session.write(JsonUtil.toSimpleResponse(true, amount + " " + itemName + " Granted to Player " + playerID).getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false, "Failed To Create Grant Event For Player " + playerID).getBytes());
            }

        }
        else if(session.action().equals("onDeletePlayerData")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            String playerID = (String)onAccess.property("playerID");

            Access user = userService.loadUser(Long.parseLong(playerID));

            if(user.login() != null) {

                AccessIndex acc = accessIndexService.get(user.login());
                if (acc != null) {

                    boolean accessIndexDelete = accessIndexService.delete(user.login());

                    boolean userServiceDelete = userService.deleteUser(Long.parseLong(playerID));

                    session.write(JsonUtil.toSimpleResponse(true, "Access Index Delete: " + accessIndexDelete + " | User Service Delete: " + userServiceDelete).getBytes());
                    return true;
                }

            }

            session.write(JsonUtil.toSimpleResponse(false, "No Data for Player " + playerID + " Deleted").getBytes());
        }
        else if(session.action().equals("onGetGlobalGrantEvents")) {
            long gameclusterID = Long.parseLong(session.name());

            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(gameclusterID);
            DataStore dataStore = gameCluster.applicationPreSetup().dataStore(gameCluster, "global_item_grants");
            List<GlobalItemGrantEvent> eventList = new ArrayList<>();

            dataStore.list(new GlobalItemGrantEventQuery(gameclusterID)).forEach(globalGrantEvent -> {
                if(!globalGrantEvent.completed){
                    eventList.add(globalGrantEvent);
                }
            });

            GlobalItemGrantList globalItemGrantList = new GlobalItemGrantList(eventList);

            session.write(globalItemGrantList.toJson().toString().getBytes());
        }
        else if(session.action().equals("onDeleteGlobalGrantEvent")) {
            long gameclusterID = Long.parseLong(session.name());

            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            String dateCreatedString = (String)onAccess.property("dateCreated");

            LocalDateTime dateCreated = LocalDateTime.parse(dateCreatedString);

            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(gameclusterID);
            DataStore dataStore = gameCluster.applicationPreSetup().dataStore(gameCluster, "global_item_grants");

           dataStore.list(new GlobalItemGrantEventQuery(gameclusterID)).forEach(globalGrantEvent -> {
                if(globalGrantEvent.dateCreated.equals(dateCreated)){
                    globalGrantEvent.completed = true;
                    dataStore.update(globalGrantEvent);

                    session.write(JsonUtil.toSimpleResponse(true, "Global Grant Event Ended").getBytes());
                }
            });

           session.write(JsonUtil.toSimpleResponse(true, "No Global Grant Event Found").getBytes());

        }
        else if(session.action().equals("onCreateGlobalEvent")) {
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);

            String amount = (String)onAccess.property("itemAmount");
            String itemID = (String)onAccess.property("itemID");
            String itemName = (String)onAccess.property("itemName");

            String minPlayerLevelFilterString =  (String)onAccess.property("minPlayerLevel");
            String maxPlayerLevelFilterString = (String)onAccess.property("maxPlayerLevel");

            String minInstallDateFilterString = (String)onAccess.property("minPlayerInstallDate");
            String maxInstallDateFilterString = (String)onAccess.property("maxPlayerInstallDate");

            String tournamentIDString = (String)onAccess.property("tournamentID");

            long gameclusterID = Long.parseLong(session.name());
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(gameclusterID);
            DataStore dataStore = gameCluster.applicationPreSetup().dataStore(gameCluster, "global_item_grants");

            LocalDateTime dateCreated = LocalDateTime.now();

            GlobalItemGrantEvent globalItemGrantEvent = new GlobalItemGrantEvent(itemName, itemID, Long.parseLong(amount), dateCreated);
            globalItemGrantEvent.ownerKey(SnowflakeKey.from(gameclusterID));

            if(!minPlayerLevelFilterString.isEmpty() && !maxPlayerLevelFilterString.isEmpty()){
                int minPlayerLevelFilter = Integer.parseInt(minPlayerLevelFilterString);
                int maxPlayerLevelFilter = Integer.parseInt(maxPlayerLevelFilterString);

                globalItemGrantEvent.setPlayerLevelFilter(minPlayerLevelFilter, maxPlayerLevelFilter);
            }

            if(!minInstallDateFilterString.isEmpty() && !maxInstallDateFilterString.isEmpty()){
                LocalDate minInstallDateFilter = LocalDate.parse(minInstallDateFilterString);
                LocalDate maxInstallDateFilter = LocalDate.parse(maxInstallDateFilterString);

                globalItemGrantEvent.setInstallDateFilter(minInstallDateFilter, maxInstallDateFilter);
            }

            if(!tournamentIDString.isEmpty()){
                long tournamentID = Long.parseLong(tournamentIDString);

                globalItemGrantEvent.setTournamentIdFilter(tournamentID);
            }

            if(dataStore.create(globalItemGrantEvent)){
                session.write(JsonUtil.toSimpleResponse(true, "Global Grant Event Created").getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(true, "Error Creating Global Grant Event").getBytes());
            }
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
        this.accessIndexService = this.context.clusterProvider().accessIndexService();
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
