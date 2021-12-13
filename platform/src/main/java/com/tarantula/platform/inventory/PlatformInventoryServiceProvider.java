package com.tarantula.platform.inventory;

import com.icodesoftware.Configurable;
import com.icodesoftware.Descriptor;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.Tournament;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.achievement.Achievement;
import com.tarantula.platform.item.*;
import com.tarantula.platform.presence.DailyGiveaway;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.store.ShoppingItem;
import com.tarantula.platform.util.SystemUtil;


public class PlatformInventoryServiceProvider implements ServiceProvider {
    private TarantulaLogger logger;

    private final String name;
    private GameCluster gameCluster;
    private ServiceContext serviceContext;
    private ApplicationPreSetup applicationPreSetup;
    public PlatformInventoryServiceProvider(GameCluster gameCluster){
        this.name = (String)gameCluster.property(GameCluster.GAME_SERVICE);
        this.gameCluster = gameCluster;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void start() throws Exception {
        logger.warn("Inventory service provider started");
    }

    @Override
    public void shutdown() throws Exception {

    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.applicationPreSetup = SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
        this.logger = serviceContext.logger(PlatformItemServiceProvider.class);
    }
    public Category category(){
        GameCluster _gameCluster = this.serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gameCluster.serviceWithCategory("item");
        ApplicationPreSetup preSetup = SystemUtil.applicationPreSetup((String) _gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
        Category category = new Category();
        category.distributionKey(app.distributionKey());
        preSetup.load(serviceContext,app,category);
        category.list((ci)-> ci.configurationType().equals(Configurable.COMMODITY_CONFIG_TYPE));
        return category;
    }
    public Inventory inventory(String systemId,String category){
        Inventory inventory = new Inventory(category);
        inventory.distributionKey(systemId);
        GameCluster _gc = this.serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory("store");
        if(!this.applicationPreSetup.load(serviceContext,app,inventory)) return null;
        inventory.list();
        return inventory;
    }
    public boolean redeem(String systemId, Application item){
        ApplicationRedeemer redeemer = new ApplicationRedeemer(systemId,this);
        redeemer.distributionKey(item.distributionKey());
        GameCluster _gc = this.serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory(item.configurationCategory());
        if(app==null||!applicationPreSetup.load(serviceContext,app,redeemer)) return false;
        redeemer.redeem();
        return true;
    }
    public boolean redeem(String systemId, Achievement item){
        ApplicationRedeemer redeemer = new ApplicationRedeemer(systemId,this);
        redeemer.distributionKey(item.distributionKey());
        GameCluster _gc = this.serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory(item.configurationCategory());
        if(app==null||!applicationPreSetup.load(serviceContext,app,redeemer)) return false;
        redeemer.redeem();
        return true;
    }
    public boolean redeem(String systemId, DailyGiveaway item){
        ApplicationRedeemer redeemer = new ApplicationRedeemer(systemId,this);
        redeemer.distributionKey(item.distributionKey());
        GameCluster _gc = this.serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory(item.configurationCategory());
        if(app==null||!applicationPreSetup.load(serviceContext,app,redeemer)) return false;
        redeemer.redeem();
        return true;
    }
    public boolean redeem(String systemId, ShoppingItem item){
        ApplicationRedeemer redeemer = new ApplicationRedeemer(systemId,this);
        redeemer.distributionKey(item.distributionKey());
        GameCluster _gc = this.serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory(item.configurationCategory());
        if(app==null||!applicationPreSetup.load(serviceContext,app,redeemer)) return false;
        redeemer.redeem();
        return true;
    }
    public boolean redeem(String systemId, Tournament.Prize item){
        ItemRedeemer redeemer = new ItemRedeemer(systemId,this);
        redeemer.distributionKey(item.distributionKey());
        GameCluster _gc = this.serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory(item.configurationCategory());
        if(app==null||!applicationPreSetup.load(serviceContext,app,redeemer)) return false;
        redeemer.redeem();
        return true;
    }

    public Inventory inventory(String category){
        ConfigurableTemplate template = this.serviceContext.deploymentServiceProvider().configuration(gameCluster,GameCluster.GAME_COMMODITY_CATEGORY_TEMPLATE);
        ConfigurableSetting conf = template.settings.get(category);
        //logger.warn("Inventory=>"+conf.type+"//"+ conf.name+"/"+conf.rechargeable+"/"+conf.icon);
        return new Inventory(conf.type,conf.name,conf.icon,conf.rechargeable);
    }
}
