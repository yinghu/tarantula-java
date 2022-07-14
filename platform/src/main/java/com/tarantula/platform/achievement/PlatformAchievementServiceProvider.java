package com.tarantula.platform.achievement;

import com.icodesoftware.*;
import com.icodesoftware.service.ConfigurationServiceProvider;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.inventory.PlatformInventoryServiceProvider;
import com.tarantula.platform.item.DistributionItemService;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.service.ClusterConfigurationCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PlatformAchievementServiceProvider implements ConfigurationServiceProvider, ClusterConfigurationCallback {

    private TarantulaLogger logger;
    private final String gameServiceName;
    private final GameCluster gameCluster;
    private final PlatformInventoryServiceProvider inventoryServiceProvider;
    private ServiceContext serviceContext;
    private DistributionItemService distributionItemService;
    private DataStore dataStore;
    private ApplicationPreSetup applicationPreSetup;
    private ConcurrentHashMap<String,Achievement> achievements;

    public PlatformAchievementServiceProvider(GameCluster gameCluster, PlatformInventoryServiceProvider inventoryServiceProvider){
        this.gameServiceName = (String)gameCluster.property(GameCluster.GAME_SERVICE);
        this.gameCluster = gameCluster;
        this.inventoryServiceProvider = inventoryServiceProvider;
        this.achievements = new ConcurrentHashMap<>();
    }
    @Override
    public String name() {
        return "achievement";
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.applicationPreSetup = gameCluster.applicationPreSetup();//SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
        this.logger = serviceContext.logger(PlatformAchievementServiceProvider.class);
        this.dataStore = serviceContext.dataStore(gameServiceName.replace("-","_"),serviceContext.partitionNumber());
        this.distributionItemService = this.serviceContext.clusterProvider().serviceProvider(DistributionItemService.NAME);
        this.logger.warn("Achievement service provider started on ->"+gameServiceName);
    }
    public AchievementProgress achievementProgress(String gameId){
        AchievementProgress achievementProgress = new AchievementProgress();
        achievementProgress.distributionKey(gameId);
        this.dataStore.createIfAbsent(achievementProgress,true);
        achievementProgress.dataStore(this.dataStore);
        if(achievementProgress.disabled()) tryNextAchievement(achievementProgress);
        return achievementProgress.disabled()?null:achievementProgress;
    }
    public AchievementProgress onProgress(String systemId,String gameId,double delta){
        AchievementProgress achievementProgress = new AchievementProgress();
        achievementProgress.distributionKey(gameId);
        this.dataStore.createIfAbsent(achievementProgress,true);
        achievementProgress.dataStore(this.dataStore);
        if(achievementProgress.onProgress(delta)){
            Achievement achievement = achievements.get(achievementProgress.name());

            if(!tryNextAchievement(achievementProgress)){
                achievementProgress.disabled(true);
                this.dataStore.update(achievementProgress);
            }
            return achievementProgress;
        }
        this.dataStore.update(achievementProgress);
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
        distributionItemService.register(gameServiceName,name(),t.configurationTypeId(),t.distributionKey());
    }
    @Override
    public <T extends Configurable> void release(T t) {
        t.released();
        distributionItemService.release(gameServiceName,name(),t.configurationTypeId(),t.distributionKey());
    }
    public boolean onRegister(String category,String itemId){
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
    public boolean onRelease(String category,String itemId){
        String[] released = {null};
        achievements.forEach((k,v)->{
            if(v.distributionKey().equals(itemId)) released[0] = k;
        });
        if(released[0]!=null) achievements.remove(released[0]);
        return true;
    }

    @Override
    public String registerConfigurableListener(Descriptor descriptor, Configurable.Listener listener) {
        List<Achievement> items = applicationPreSetup.list(descriptor,new AchievementObjectQuery("typeId/"+descriptor.category()));
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
            this.dataStore.update(achievementProgress);
            return true;
        }
        key = "tier_"+(achievementProgress.tier()+1)+"_target_1"; //tier up 1
        achievement = achievements.get(key);
        if(achievement!=null){
            achievementProgress.reset(achievement.tier(),achievement.target(),achievement.objective());
            this.dataStore.update(achievementProgress);
            return true;
        }
        return false;
    }
}
