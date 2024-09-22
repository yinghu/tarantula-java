package com.tarantula.platform.presence.achievement;

import com.google.gson.JsonArray;
import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.PlatformItemServiceProvider;
import com.tarantula.platform.presence.saves.CurrentSaveIndex;
import com.tarantula.platform.util.JsonSerializableContext;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PlatformAchievementServiceProvider extends PlatformItemServiceProvider {

    public static final String NAME = "achievement";

    private ConcurrentHashMap<String, AchievementItem> achievements;

    public PlatformAchievementServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
        this.achievements = new ConcurrentHashMap<>();
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        super.setup(serviceContext);
        this.dataStore = applicationPreSetup.dataStore(gameCluster,NAME);
        this.logger = JDKLogger.getLogger(PlatformAchievementServiceProvider.class);
        this.logger.warn("Achievement service provider started on ->"+gameServiceName);
    }

    @Override
    public void start() throws Exception{
        if(serviceContext.node().homingAgent().enabled()){
            String config = serviceContext.node().homingAgent().onConfiguration(gameCluster.distributionId(),"Achievement");
            logger.warn(config);
            JsonArray list = JsonUtil.parse(config).get("list").getAsJsonArray();
            list.forEach(e->{
                AchievementItem achievementItem = new AchievementItem(e.getAsJsonObject());
                registerAchievement(achievementItem);
            });
        }
    }

    public Achievement achievement(Session session){
        CurrentSaveIndex currentSaveIndex = platformGameServiceProvider.savedGameServiceProvider().currentSaveIndex(session);
        AchievementProgress achievementProgress = AchievementProgress.lookup(currentSaveIndex.saveId,dataStore);
        return new AchievementProxy(achievementProgress,delta ->onProgress(session,achievementProgress,delta));
    }

    private void onProgress(Session session,AchievementProgress achievementProgress,double delta){
            logger.warn("STATUS : "+achievementProgress.disabled()+" : "+delta+" : "+achievementProgress.progress());
            if(achievementProgress.disabled()) tryNextAchievement(achievementProgress);
            if(achievementProgress.disabled()) return;
            if(achievementProgress.onProgress(delta)){
                AchievementItem achievement = achievements.get(achievementProgress.name());
                if(achievement==null) {
                    achievementProgress.disabled(true);
                    achievementProgress.update();
                    return;
                }
                logger.warn("Achieved : "+achievement.configurationTypeId());
                platformGameServiceProvider.inventoryServiceProvider().redeem(session.systemId(),achievement);
                logger.warn("Redeemed : "+achievement.configurationTypeId());
                if(!tryNextAchievement(achievementProgress)){
                    achievementProgress.disabled(true);
                    achievementProgress.update();
                }
            }
    }
    public List<AchievementItem> list(){
        ArrayList<AchievementItem> _item = new ArrayList<>();
        achievements.forEach((k,v)->_item.add(v));
        return _item;
    }

    public JsonSerializable listAsJson(){
        return new JsonSerializableContext<>(list());
    }

    @Override
    public <T extends Configurable> void register(T t) {
        t.registered();
        distributionItemService.onRegisterItem(gameServiceName,name(),t.configurationTypeId(),t.distributionKey());
    }
    @Override
    public <T extends Configurable> void release(T t) {
        t.released();
        distributionItemService.onReleaseItem(gameServiceName,name(),t.configurationTypeId(),t.distributionKey());
    }
    public boolean onItemRegistered(String category,String itemId){
        AchievementItem configurableObject = new AchievementItem();
        configurableObject.distributionKey(itemId);
        GameCluster _gc = serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionId());
        Descriptor app = _gc.serviceWithCategory(category);
        if(!applicationPreSetup.load(app,configurableObject)){
            return false;
        }
        configurableObject.setup();
        achievements.put(configurableObject.name(),configurableObject);
        return true;
    }
    public boolean onItemReleased(String category,String itemId){
        String[] released = {null};
        achievements.forEach((k,v)->{
            if(v.distributionKey().equals(itemId)) released[0] = k;
        });
        if(released[0]!=null) achievements.remove(released[0]);
        return true;
    }

    @Override
    public String registerConfigurableListener(Descriptor descriptor, Configurable.Listener listener) {
        List<AchievementItem> items = applicationPreSetup.list(descriptor,new AchievementObjectQuery(descriptor.key()));
        items.forEach((a)-> {
            logger.warn("<><><>"+a.name()+"<><>"+a.objective());
            a.configurableSetting(gameCluster.configurableCategories(Configurable.APPLICATION_CONFIG_TYPE));
            a.setup();
            if(!a.disabled()) achievements.put(a.name(),a);
        });
        return null;
    }
    private boolean tryNextAchievement(AchievementProgress achievementProgress){
        String key = "tier_"+achievementProgress.tier()+"_target_"+(achievementProgress.target()+1);//target up 1
        AchievementItem achievement = achievements.get(key);
        if(achievement!=null){
            achievementProgress.reset(achievement.tier(),achievement.target(),achievement.objective());
            return true;
        }
        key = "tier_"+(achievementProgress.tier()+1)+"_target_1"; //tier up 1
        achievement = achievements.get(key);
        if(achievement!=null){
            achievementProgress.reset(achievement.tier(),achievement.target(),achievement.objective());
            return true;
        }
        return false;
    }

    @Override
    public boolean onItemRegistered(int publishId,int configurationId){
        String config = serviceContext.node().homingAgent().onConfigurationRegistered(publishId);
        logger.warn(config);
        AchievementItem achievementItem = new AchievementItem(JsonUtil.parse(config));
        registerAchievement(achievementItem);
        return true;
    }
    @Override
    public boolean onItemReleased(int publishId,int configurationId){
        logger.warn("release local resource with ["+publishId+"]");
        AchievementItem removed = achievements.remove(Integer.toString(configurationId));
        if(removed==null) return false;
        achievements.remove(removed.name());
        return true;
    }

    private void registerAchievement(AchievementItem achievementItem){
        achievementItem.commodityList().forEach(commodity -> {
            gameCluster.registerConfigurableCategory(commodity.configurableCategory());
        });
        this.achievements.put(achievementItem.name(),achievementItem);
        this.achievements.put(achievementItem.configurationKey(),achievementItem);
    }
}
