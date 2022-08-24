package com.tarantula.platform.configuration;

import com.icodesoftware.Configurable;
import com.icodesoftware.Descriptor;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.ConfigurationServiceProvider;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TokenValidatorProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.ConfigurableObjectQuery;
import com.tarantula.platform.item.DistributionItemService;
import com.tarantula.platform.presence.PlatformPresenceServiceProvider;
import com.tarantula.platform.service.AmazonAWSProvider;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.service.AuthObject;
import com.tarantula.platform.service.ClusterConfigurationCallback;

import java.util.List;

public class PlatformConfigurationServiceProvider implements ConfigurationServiceProvider, ClusterConfigurationCallback {


    private GameCluster gameCluster;
    private TarantulaLogger logger;
    private final String gameServiceName;

    private ApplicationPreSetup applicationPreSetup;

    private DistributionItemService distributionItemService;

    private ServiceContext serviceContext;

    public PlatformConfigurationServiceProvider(GameCluster gameCluster){
        this.gameServiceName = (String)gameCluster.property(GameCluster.GAME_SERVICE);
        this.gameCluster = gameCluster;
    }

    @Override
    public String name() {
        return "configuration";
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
    @Override
    public String registerConfigurableListener(Descriptor descriptor, Configurable.Listener listener) {
        List<ConfigurableObject> items = applicationPreSetup.list(descriptor,new ConfigurableObjectQuery("typeId/"+descriptor.category()));
        items.forEach((a)-> {
            a.setup();
            if(!a.disabled()){
                TokenValidatorProvider.AuthVendor vendor = toAuthVendor(a);
                if(a!=null){
                    logger.warn(vendor.name());
                    //serviceContext.registerAuthVendor(vendor);
                }
            }
        });
        return null;
    }
    @Override
    public <T extends Configurable> void register(T t) {
        t.registered();
        distributionItemService.register(gameServiceName,name(),t.configurationTypeId(),t.distributionKey());
    }
    @Override
    public <T extends Configurable> void release(T t) {
        t.released();
        this.distributionItemService.release(gameServiceName,name(),t.configurationTypeId(),t.distributionKey());
    }

    public boolean onRegister(String category,String itemId){
        ConfigurableObject configurableObject = new ConfigurableObject();
        configurableObject.distributionKey(itemId);
        GameCluster _gc = serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory("item");
        if(!applicationPreSetup.load(app,configurableObject)){
            return false;
        }
        TokenValidatorProvider.AuthVendor authVendor = toAuthVendor(configurableObject);
        this.serviceContext.registerAuthVendor(authVendor);
        return true;
    }
    public boolean onRelease(String category,String itemId){
        logger.warn("CAT->"+category+">>>"+itemId);
        return false;
    }


    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.applicationPreSetup = gameCluster.applicationPreSetup();
        this.distributionItemService = this.serviceContext.clusterProvider().serviceProvider(DistributionItemService.NAME);
        this.logger = serviceContext.logger(PlatformPresenceServiceProvider.class);
        this.logger.warn("Configuration service provider started on ->"+gameServiceName);
    }
    @Override
    public void waitForData(){

    }
    private TokenValidatorProvider.AuthVendor toAuthVendor(ConfigurableObject configurableObject){
        if(configurableObject.configurationCategory().equals("AwsS3Configuration")){
            AmazonAWSProvider amazonAWSProvider = new AmazonAWSProvider(new AwsS3Configuration(gameServiceName.replace("-service",""),configurableObject));
            amazonAWSProvider.registerMetricsLister(gameCluster);
            return amazonAWSProvider;
        }
        return null;
    }

}
