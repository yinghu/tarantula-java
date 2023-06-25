package com.tarantula.platform.achievement;

import com.icodesoftware.*;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.inventory.PlatformInventoryServiceProvider;
import com.tarantula.platform.item.PlatformItemServiceProvider;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PlatformAchievementServiceProvider extends PlatformItemServiceProvider {

    public static final String NAME = "achievement";

    private final PlatformInventoryServiceProvider inventoryServiceProvider;

    private ConcurrentHashMap<String,Achievement> achievements;

    public PlatformAchievementServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
        this.inventoryServiceProvider = gameServiceProvider.inventoryServiceProvider();
        this.achievements = new ConcurrentHashMap<>();
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        super.setup(serviceContext);
        this.logger = serviceContext.logger(PlatformAchievementServiceProvider.class);
        this.logger.warn("Achievement service provider started on ->"+gameServiceName);
    }
    public AchievementProgress achievementProgress(Session session){
        AchievementProgress achievementProgress = new AchievementProgress();
        if(!platformGameServiceProvider.savedGameServiceProvider().load(session,achievementProgress)){
            platformGameServiceProvider.savedGameServiceProvider().save(session,achievementProgress);
        }
        if(achievementProgress.disabled()) tryNextAchievement(achievementProgress);
        return achievementProgress.disabled()?null:achievementProgress;
    }
    public AchievementProgress onProgress(Session session,double delta){
        AchievementProgress achievementProgress = new AchievementProgress();
        if(!platformGameServiceProvider.savedGameServiceProvider().load(session,achievementProgress)){
            platformGameServiceProvider.savedGameServiceProvider().save(session,achievementProgress);
        }
        if(achievementProgress.onProgress(delta)){
            Achievement achievement = achievements.get(achievementProgress.name());
            if(!tryNextAchievement(achievementProgress)){
                achievementProgress.disabled(true);
                achievementProgress.update();
            }
            return achievementProgress;
        }
        achievementProgress.update();
        return achievementProgress;
    }
    public List<Achievement> list(){
        ArrayList<Achievement> _item = new ArrayList<>();
        achievements.forEach((k,v)->_item.add(v));
        return _item;
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
        Achievement configurableObject = new Achievement();
        configurableObject.distributionKey(itemId);
        GameCluster _gc = serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
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
        List<Achievement> items = applicationPreSetup.list(descriptor,new AchievementObjectQuery("category/Achievement"));
        items.forEach((a)-> {
            a.setup();
            if(!a.disabled()) achievements.put(a.name(),a);
        });
        return null;
    }
    private boolean tryNextAchievement(AchievementProgress achievementProgress){
        String key = "tier_"+achievementProgress.tier()+"_target_"+(achievementProgress.target()+1);//target up 1
        Achievement achievement = achievements.get(key);
        if(achievement!=null){
            achievementProgress.reset(achievement.tier(),achievement.target(),achievement.objective());
            achievementProgress.update();
            return true;
        }
        key = "tier_"+(achievementProgress.tier()+1)+"_target_1"; //tier up 1
        achievement = achievements.get(key);
        if(achievement!=null){
            achievementProgress.reset(achievement.tier(),achievement.target(),achievement.objective());
            achievementProgress.update();
            return true;
        }
        return false;
    }
}
