package com.tarantula.platform.achievement;

import com.icodesoftware.*;
import com.icodesoftware.service.ConfigurationServiceProvider;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.ConfigurableObjectQuery;
import com.tarantula.platform.item.DistributionItemService;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.service.deployment.TypedListener;
import com.tarantula.platform.util.SystemUtil;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AchievementServiceProvider implements ConfigurationServiceProvider {

    private TarantulaLogger logger;
    private final String name;
    private final GameCluster gameCluster;

    private ServiceContext serviceContext;
    private DistributionAchievementService distributionAchievementService;
    private DataStore dataStore;
    private ApplicationPreSetup applicationPreSetup;
    private ConcurrentHashMap<String,Achievement> achievements;
    private ConcurrentHashMap<String,Configurable.Listener<Achievement>> rListeners = new ConcurrentHashMap<>();

    public AchievementServiceProvider(GameCluster gameCluster){
        this.name = (String)gameCluster.property(GameCluster.GAME_SERVICE);
        this.gameCluster = gameCluster;
        this.achievements = new ConcurrentHashMap<>();
    }
    @Override
    public String name() {
        return name;
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
        this.distributionAchievementService = this.serviceContext.clusterProvider(Distributable.DATA_SCOPE).serviceProvider(DistributionAchievementService.NAME);
    }

    public AchievementProgress onProgress(String systemId,String goal,double delta){
        Achievement achievement = achievements.get(goal);
        AchievementProgress achievementProgress = new AchievementProgress(achievement);
        achievementProgress.distributionKey(systemId);
        this.dataStore.createIfAbsent(achievementProgress,true);
        if(achievementProgress.onProgress(delta)){
            //achievement looting
        }
        this.dataStore.update(achievementProgress);
        return achievementProgress;
    }

    @Override
    public <T extends Configurable> void register(T t) {
        //this.rListeners.forEach((k,l)->l.onCreated((Achievement)t));
        distributionAchievementService.register(name,t.configurationCategory(),t.distributionKey());
    }
    public boolean onRegister(String category,String itemId){
        return true;
    }
    @Override
    public <T extends Configurable> void release(T t) {

    }

    @Override
    public void configure(String s) {

    }

    @Override
    public <T extends Configuration> T configuration(String s) {
        return null;
    }

    @Override
    public String registerConfigurableListener(Descriptor descriptor, Configurable.Listener listener) {
        String rid = UUID.randomUUID().toString();
        List<Achievement> items = applicationPreSetup.list(serviceContext,descriptor,new AchievementObjectQuery("category/"+descriptor.category()));
        items.forEach((a)-> {
            listener.onCreated(a);
            achievements.put(a.name(),a);
        });
        this.rListeners.put(rid,listener);
        logger.warn("Listener registered with ->"+descriptor.category());
        return rid;
    }

    @Override
    public String registerConfigurableListener(String s, Configurable.Listener listener) {
        throw new UnsupportedOperationException("using descriptor");
    }

    @Override
    public void unregisterConfigurableListener(String registerKey) {
        this.rListeners.remove(registerKey);
    }
}
