package com.tarantula.platform.achievement;

import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.inventory.ApplicationRedeemer;
import com.tarantula.platform.item.PlatformItemServiceProvider;
import com.tarantula.platform.presence.saves.CurrentSaveIndex;


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
        this.logger = JDKLogger.getLogger(PlatformAchievementServiceProvider.class);
        this.logger.info("Achievement service provider started on ->"+gameServiceName);
    }

    public Achievement achievement(Session session){
        CurrentSaveIndex currentSaveIndex = platformGameServiceProvider.savedGameServiceProvider().currentSaveIndex(session);
        AchievementProgress achievementProgress = new AchievementProgress();
        achievementProgress.distributionId(currentSaveIndex.saveId);
        return new AchievementProxy(achievementProgress,delta ->onProgress(session,achievementProgress,delta));
    }

    private void onProgress(Session session,AchievementProgress achievementProgress,double delta){
        Transaction transaction = gameCluster.transaction();
        transaction.execute(ctx->{
            ApplicationPreSetup preSetup = (ApplicationPreSetup)ctx;
            DataStore ds = preSetup.onDataStore(NAME);
            ds.createIfAbsent(achievementProgress,true);
            achievementProgress.dataStore(ds);
            logger.info("STATUS : "+achievementProgress.disabled()+" : "+delta+" : "+achievementProgress.progress());
            if(achievementProgress.disabled()) tryNextAchievement(achievementProgress);
            if(achievementProgress.disabled()) return true;
            if(achievementProgress.onProgress(delta)){
                AchievementItem achievement = achievements.get(achievementProgress.name());
                if(achievement==null) {
                    achievementProgress.disabled(true);
                    achievementProgress.update();
                    return true;
                }
                logger.info("Achieved : "+achievement.configurationTypeId());
                Descriptor app = gameCluster.application(achievement.configurationTypeId());
                ApplicationRedeemer redeemer = new ApplicationRedeemer(session.systemId(),preSetup);
                redeemer.distributionKey(achievement.distributionKey());
                if(!preSetup.load(app,redeemer)) return false;
                redeemer.redeem();
                logger.info("Redeemed : "+achievement.configurationTypeId());
                if(!tryNextAchievement(achievementProgress)){
                    achievementProgress.disabled(true);
                    achievementProgress.update();
                }
            }
            return true;
        });
    }
    public List<AchievementItem> list(){
        ArrayList<AchievementItem> _item = new ArrayList<>();
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
        List<AchievementItem> items = applicationPreSetup.list(descriptor,new AchievementObjectQuery(descriptor.key(),"Achievement"));
        items.forEach((a)-> {
            logger.info("<><><>"+a.name()+"<><>"+a.objective());
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
}
