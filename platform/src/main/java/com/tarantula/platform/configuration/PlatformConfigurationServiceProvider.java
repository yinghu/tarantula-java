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
import com.tarantula.platform.service.*;
import com.tarantula.platform.service.metrics.GameClusterMetrics;

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
                    serviceContext.registerAuthVendor(vendor);
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
        ConfigurableObject configurableObject = new ConfigurableObject();
        configurableObject.distributionKey(itemId);
        GameCluster _gc = serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory("item");
        if(!applicationPreSetup.load(app,configurableObject)){
            return false;
        }
        TokenValidatorProvider.AuthVendor authVendor = toAuthVendor(configurableObject);
        this.serviceContext.unregisterAuthVendor(authVendor);
        return true;
    }


    @Override
    public void setup(ServiceContext serviceContext) {
        GameClusterMetrics gameClusterMetrics = new GameClusterMetrics(gameServiceName);
        gameClusterMetrics.setup(serviceContext);
        serviceContext.registerMetrics(gameClusterMetrics);
        this.serviceContext = serviceContext;
        this.applicationPreSetup = gameCluster.applicationPreSetup();
        this.distributionItemService = this.serviceContext.clusterProvider().serviceProvider(DistributionItemService.NAME);
        this.logger = serviceContext.logger(PlatformPresenceServiceProvider.class);
        this.logger.warn("Configuration service provider started on ->"+gameServiceName+">>>"+gameCluster.property(GameCluster.NAME));
    }
    @Override
    public void waitForData(){

    }
    private TokenValidatorProvider.AuthVendor toAuthVendor(ConfigurableObject configurableObject){
        String typeId = gameServiceName.replace("-service","");
        if(configurableObject.configurationCategory().equals("AwsS3Configuration")){
            AmazonAWSProvider amazonAWSProvider = new AmazonAWSProvider(new AwsS3Configuration(typeId,configurableObject));
            //amazonAWSProvider.registerMetricsLister(gameCluster);
            return amazonAWSProvider;
        }
        else if(configurableObject.configurationCategory().equals("AppleStoreConfiguration")){
            AppleStoreProvider appleStoreProvider = new AppleStoreProvider(new AppleStoreConfiguration(typeId,configurableObject));
            //appleStoreProvider.registerMetricsLister(gameCluster);
            return appleStoreProvider;
        }
        else if(configurableObject.configurationCategory().equals("FacebookConfiguration")){
            FacebookAuthProvider facebookAuthProvider = new FacebookAuthProvider(new FacebookConfiguration(typeId,configurableObject));
            //facebookAuthProvider.registerMetricsLister(gameCluster);
            return facebookAuthProvider;
        }
        else if(configurableObject.configurationCategory().equals("GoogleStoreConfiguration")){
            GoogleStorePurchaseValidator googleStorePurchaseValidator = new GoogleStorePurchaseValidator(new GoogleStoreConfiguration(typeId,configurableObject));
            //googleStorePurchaseValidator.registerMetricsLister(gameCluster);
            return googleStorePurchaseValidator;
        }
        else if(configurableObject.configurationCategory().equals("GooglePlayConfiguration")){
            GoogleOAuthTokenValidator googleOAuthTokenValidator = new GoogleOAuthTokenValidator(new GooglePlayConfiguration(typeId,configurableObject));
            //googleOAuthTokenValidator.registerMetricsLister(gameCluster);
            return googleOAuthTokenValidator;
        }
        return null;
    }

}
