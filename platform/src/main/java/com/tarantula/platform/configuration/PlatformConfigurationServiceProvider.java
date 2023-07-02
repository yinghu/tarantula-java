package com.tarantula.platform.configuration;

import com.icodesoftware.Configurable;
import com.icodesoftware.Descriptor;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TokenValidatorProvider;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.*;
import com.tarantula.platform.store.ApplicationStoreProvider;
import com.tarantula.platform.service.*;
import com.tarantula.platform.service.persistence.mysql.MysqlBackupProvider;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PlatformConfigurationServiceProvider extends PlatformItemServiceProvider {

    public static final String NAME = "configuration";

    private final String typeId;

    private ConcurrentHashMap<String, TokenValidatorProvider.AuthVendor> registered = new ConcurrentHashMap<>();

    public PlatformConfigurationServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
        this.typeId = gameCluster.typeId();
    }

    @Override
    public void shutdown() throws Exception {
        registered.forEach((k,v)->{
            serviceContext.unregisterAuthVendor(v);
        });
    }
    @Override
    public String registerConfigurableListener(Descriptor descriptor, Configurable.Listener listener) {
        List<ConfigurableObject> items = applicationPreSetup.list(descriptor,new ConfigurableObjectQuery("typeId/"+descriptor.category()));
        items.forEach((a)-> {
            a.setup();
            if(!a.disabled()){
                if(a.configurationCategory().equals("MySQLConfiguration")){
                    MysqlBackupProvider mysqlBackupProvider = new MysqlBackupProvider(new MySQLConfiguration(typeId,a));
                    this.serviceContext.registerBackupProvider(mysqlBackupProvider);
                }
                else{
                    TokenValidatorProvider.AuthVendor vendor = toAuthVendor(a);
                    if(vendor!=null){
                        serviceContext.registerAuthVendor(vendor);
                        registered.put(vendor.name(),vendor);
                    }
                }
            }
        });
        GameCenterAuthProvider gameCenterAuthProvider = new GameCenterAuthProvider(typeId,serviceContext.metrics(gameServiceName));
        serviceContext.registerAuthVendor(gameCenterAuthProvider);
        registered.put(gameCenterAuthProvider.name(),gameCenterAuthProvider);
        ApplicationStoreProvider applicationStoreProvider = new ApplicationStoreProvider(typeId,this.platformGameServiceProvider);
        registered.put(applicationStoreProvider.name(),applicationStoreProvider);
        serviceContext.registerAuthVendor(applicationStoreProvider);
        DeveloperStoreProvider developerStoreProvider = new DeveloperStoreProvider(typeId);
        this.serviceContext.registerAuthVendor(developerStoreProvider);
        registered.put(developerStoreProvider.name(),developerStoreProvider);
        return null;
    }
    @Override
    public <T extends Configurable> void register(T t) {
        t.registered();
        distributionItemService.onRegisterItem(gameServiceName,name(),t.configurationTypeId(),t.distributionKey());
    }
    @Override
    public <T extends Configurable> void release(T t) {
        t.released();
        this.distributionItemService.onReleaseItem(gameServiceName,name(),t.configurationTypeId(),t.distributionKey());
    }

    public boolean onItemRegistered(String category,String itemId){
        ConfigurableObject configurableObject = new ConfigurableObject();
        configurableObject.distributionKey(itemId);
        GameCluster _gc = serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory("item");
        if(!applicationPreSetup.load(app,configurableObject)){
            return false;
        }
        if(configurableObject.configurationCategory().equals("MySQLConfiguration")){
            MysqlBackupProvider mysqlBackupProvider = new MysqlBackupProvider(new MySQLConfiguration(typeId,configurableObject));
            this.serviceContext.registerBackupProvider(mysqlBackupProvider);
            return true;
        }
        TokenValidatorProvider.AuthVendor authVendor = toAuthVendor(configurableObject);
        this.serviceContext.registerAuthVendor(authVendor);
        registered.put(authVendor.name(),authVendor);
        return true;
    }
    public boolean onItemReleased(String category,String itemId){
        ConfigurableObject configurableObject = new ConfigurableObject();
        configurableObject.distributionKey(itemId);
        GameCluster _gc = serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory("item");
        if(!applicationPreSetup.load(app,configurableObject)){
            return false;
        }
        if(configurableObject.configurationCategory().equals("MySQLConfiguration")){
            MysqlBackupProvider mysqlBackupProvider = new MysqlBackupProvider(new MySQLConfiguration(typeId,configurableObject));
            this.serviceContext.unregisterBackupProvider(mysqlBackupProvider);
            return true;
        }
        TokenValidatorProvider.AuthVendor authVendor = toAuthVendor(configurableObject);
        this.serviceContext.unregisterAuthVendor(authVendor);
        this.registered.remove(authVendor.name());
        return true;
    }


    @Override
    public void setup(ServiceContext serviceContext) {
        super.setup(serviceContext);
        gameCluster.addListener(this);
        this.logger = serviceContext.logger(PlatformConfigurationServiceProvider.class);
        this.logger.warn("Configuration service provider started on ["+gameServiceName+"]["+gameCluster.property(GameCluster.NAME)+"]");
    }
    private TokenValidatorProvider.AuthVendor toAuthVendor(ConfigurableObject configurableObject){
        if(configurableObject.configurationCategory().equals("AwsS3Configuration")){
            AmazonAWSProvider amazonAWSProvider = new AmazonAWSProvider(new AwsS3Configuration(typeId,configurableObject),serviceContext.metrics(gameServiceName));
            return amazonAWSProvider;
        }
        else if(configurableObject.configurationCategory().equals("AppleStoreConfiguration")){
            AppleStoreProvider appleStoreProvider = new AppleStoreProvider(new AppleStoreConfiguration(typeId,configurableObject),serviceContext.metrics(gameServiceName));
            appleStoreProvider.registerMetricsLister(serviceContext.metrics(gameServiceName));
            return appleStoreProvider;
        }
        else if(configurableObject.configurationCategory().equals("FacebookConfiguration")){
            FacebookAuthProvider facebookAuthProvider = new FacebookAuthProvider(new FacebookConfiguration(typeId,configurableObject),serviceContext.metrics(gameServiceName));
            facebookAuthProvider.registerMetricsLister(serviceContext.metrics(gameServiceName));
            return facebookAuthProvider;
        }
        else if(configurableObject.configurationCategory().equals("GoogleStoreConfiguration")){
            GoogleStorePurchaseValidator googleStorePurchaseValidator = new GoogleStorePurchaseValidator(new GoogleStoreConfiguration(typeId,configurableObject),serviceContext.metrics(gameServiceName));
            googleStorePurchaseValidator.registerMetricsLister(serviceContext.metrics(gameServiceName));
            return googleStorePurchaseValidator;
        }
        else if(configurableObject.configurationCategory().equals("GooglePlayConfiguration")){
            GoogleOAuthTokenValidator googleOAuthTokenValidator = new GoogleOAuthTokenValidator(new GooglePlayConfiguration(typeId,configurableObject),serviceContext.metrics(gameServiceName));
            googleOAuthTokenValidator.registerMetricsLister(serviceContext.metrics(gameServiceName));
            return googleOAuthTokenValidator;
        }
        return null;
    }

    public <T extends Configurable> void onCreated(Descriptor application,T t){
        int index = t.configurationType().indexOf(".");
        String scope = index>0?t.configurationType().substring(0,index):t.configurationType();
        ConfigurableCategories categories = new ConfigurableCategories();
        categories.name(scope);
        if(!applicationPreSetup.load(gameCluster,categories)){
            logger.warn("Categories missed from ["+ scope+"]");
            return;
        }
        ConfigurableSetting configurableSetting = categories.configurableSetting(t.configurationCategory());
        logger.warn(configurableSetting.toString());
        logger.warn(application.distributionKey()+">>CCC"+t.distributionKey()+">>"+t.configurationVersion()+">>>"+t.configurationCategory()+">>"+t.configurationType());
    }
    public <T extends Configurable> void onUpdated(Descriptor application,T t){
        //logger.warn(application.distributionKey()+">>UUU"+t.distributionKey()+">>"+t.configurationVersion());
    }
    public <T extends Configurable> void onDeleted(Descriptor application,T t){
        //logger.warn(application.distributionKey()+">>DDD"+t.distributionKey()+">>"+t.configurationVersion());
    }
    public <T extends Configurable> void onCreated(GameCluster application,T t){
        //logger.warn(application.distributionKey()+">>GCCC"+t.key().asString()+">>"+t.configurationVersion());
    }
    public <T extends Configurable> void onUpdated(GameCluster application,T t){
        //logger.warn(application.distributionKey()+">>GUUU"+t.key().asString()+">>"+t.configurationVersion());
    }
    public <T extends Configurable> void onDeleted(GameCluster application,T t){
        //logger.warn(application.distributionKey()+">>GDDD"+t.key().asString()+">>"+t.configurationVersion());
    }

}
