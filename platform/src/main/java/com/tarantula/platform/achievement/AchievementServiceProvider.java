package com.tarantula.platform.achievement;

import com.icodesoftware.*;
import com.icodesoftware.service.ConfigurationServiceProvider;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.inventory.InventoryServiceProvider;
import com.tarantula.platform.item.DistributionItemService;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.service.ClusterConfigurationCallback;
import com.tarantula.platform.util.SystemUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AchievementServiceProvider implements ConfigurationServiceProvider, ClusterConfigurationCallback {

    private TarantulaLogger logger;
    private final String name;
    private final GameCluster gameCluster;
    private final InventoryServiceProvider inventoryServiceProvider;
    private ServiceContext serviceContext;
    private DistributionItemService distributionItemService;
    private DataStore dataStore;
    private ApplicationPreSetup applicationPreSetup;
    private ConcurrentHashMap<String,Achievement> achievements;

    public AchievementServiceProvider(GameCluster gameCluster, InventoryServiceProvider inventoryServiceProvider){
        this.name = (String)gameCluster.property(GameCluster.GAME_SERVICE);
        this.gameCluster = gameCluster;
        this.inventoryServiceProvider = inventoryServiceProvider;
        this.achievements = new ConcurrentHashMap<>();
    }
    @Override
    public String name() {
        return "AchievementService";
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
        this.applicationPreSetup = SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
        this.logger = serviceContext.logger(AchievementServiceProvider.class);
        this.dataStore = serviceContext.dataStore(name.replace("-","_"),serviceContext.partitionNumber());
        this.distributionItemService = this.serviceContext.clusterProvider(Distributable.DATA_SCOPE).serviceProvider(DistributionItemService.NAME);
    }

    public AchievementProgress onProgress(String systemId,String goal,double delta){
        Achievement achievement = achievements.get(goal);
        AchievementProgress achievementProgress = new AchievementProgress(achievement);
        achievementProgress.distributionKey(systemId);
        this.dataStore.createIfAbsent(achievementProgress,true);
        if(achievementProgress.onProgress(delta)){
            //achievement looting
            achievementProgress.disabled(true);
            inventoryServiceProvider.redeem(systemId,achievement);
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
        distributionItemService.register(name,name(),t.configurationCategory(),t.distributionKey());
    }
    @Override
    public <T extends Configurable> void release(T t) {
        t.released();
        distributionItemService.release(name,name(),t.configurationCategory(),t.distributionKey());
    }
    public boolean onRegister(String category,String itemId){
        Achievement configurableObject = new Achievement();
        configurableObject.distributionKey(itemId);
        GameCluster _gc = serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory(category);
        if(!applicationPreSetup.load(serviceContext,app,configurableObject)){
            return false;
        }
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
        List<Achievement> items = applicationPreSetup.list(serviceContext,descriptor,new AchievementObjectQuery("category/"+descriptor.category()));
        items.forEach((a)-> {
            if(!a.disabled()) achievements.put(a.name(),a);
        });
        return null;
    }
}
