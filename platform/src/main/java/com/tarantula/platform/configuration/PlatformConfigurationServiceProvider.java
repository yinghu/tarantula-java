package com.tarantula.platform.configuration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.Configuration;
import com.icodesoftware.Descriptor;

import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.*;

import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.item.*;
import com.tarantula.platform.service.ServiceEventLog;
import com.tarantula.platform.service.cluster.ClusterFailureEvent;
import com.tarantula.platform.store.Transaction;
import com.tarantula.platform.util.SystemUtil;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PlatformConfigurationServiceProvider extends PlatformItemServiceProvider {

    public static final String NAME = "configuration";

    private final String typeId;

    private ConcurrentHashMap<String, TokenValidatorProvider.AuthVendor> registered = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String,CredentialConfiguration> vendorCredentials = new ConcurrentHashMap<>();

    private HashMap<String,JsonObject> vendors = new HashMap<>();

    private ServiceEventLogger serviceEventLogger;
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
        List<ConfigurableObject> items = applicationPreSetup.list(descriptor,new ConfigurableObjectQuery(descriptor.key(),descriptor.category()));
        items.forEach((a)-> {
            a.setup();
            if(!a.disabled()){
                try{
                    JsonObject vendor = vendors.get(a.configurationCategory());
                    String cname = vendor.get("package").getAsString()+"."+a.configurationCategory();
                    CredentialConfiguration credentialConfiguration = (CredentialConfiguration) Class.forName(cname).getConstructor(String.class,ConfigurableObject.class).newInstance(this.typeId,a);
                    if(credentialConfiguration.setup(serviceContext,dataStore)){
                        vendorCredentials.put(credentialConfiguration.name(),credentialConfiguration);
                    }
                }catch (Exception nex){
                    logger.warn("Credential Configuration Setup failed",nex);
                }
            }
        });
        vendors.forEach((k,c)->{
            JsonObject vendor = c.getAsJsonObject();
            if(!vendor.get("disabled").getAsBoolean()){
                JsonArray providers = vendor.get("providers").getAsJsonArray();
                providers.forEach(p->{
                    logger.warn("Register provider->"+p.getAsString());
                    try{
                        TokenValidatorProvider.AuthVendor authVendor = (TokenValidatorProvider.AuthVendor) Class.forName(p.getAsString()).getConstructor(PlatformGameServiceProvider.class, MetricsListener.class).newInstance(platformGameServiceProvider,serviceContext.metrics(gameServiceName));
                        serviceContext.registerAuthVendor(authVendor);
                        registered.put(authVendor.name(),authVendor);
                    } catch (Exception ex){
                        logger.warn("Provider setup failed->"+p.getAsString(),ex);
                    }
                });
            }
        });
        return null;
    }
    @Override
    public <T extends Configurable> void register(T t) {
        if(!distributionItemService.onRegisterItem(gameServiceName,name(),t.configurationTypeId(),t.distributionKey())) throw new RuntimeException("failed to register config");
        t.registered();
    }
    @Override
    public <T extends Configurable> void release(T t) {
        if(!this.distributionItemService.onReleaseItem(gameServiceName,name(),t.configurationTypeId(),t.distributionKey())) throw new RuntimeException("failed to release config");
        t.released();
    }

    public boolean onItemRegistered(String category,String itemId){
        ConfigurableObject configurableObject = new ConfigurableObject();
        configurableObject.distributionKey(itemId);
        GameCluster _gc = serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory("item");
        if(!applicationPreSetup.load(app,configurableObject)){
            return false;
        }
        JsonObject vendor = vendors.get(configurableObject.configurationCategory());
        if(vendor==null || vendor.get("disabled").getAsBoolean()){
            logger.warn(configurableObject.configurationCategory()+" is disabled");
            return false;
        }
        String cname = vendor.get("package").getAsString()+"."+configurableObject.configurationCategory();
        try {
            CredentialConfiguration credentialConfiguration = (CredentialConfiguration) Class.forName(cname).getConstructor(String.class, ConfigurableObject.class).newInstance(this.typeId,configurableObject);
            if (credentialConfiguration.setup(serviceContext, dataStore)) {
                vendorCredentials.put(credentialConfiguration.name(), credentialConfiguration);
            }
            return true;
        }catch (Exception ex){
            logger.warn("Credential configuration setup failed->"+vendor,ex);
            return false;
        }
    }
    public boolean onItemReleased(String category,String itemId){
        ConfigurableObject configurableObject = new ConfigurableObject();
        configurableObject.distributionKey(itemId);
        GameCluster _gc = serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory("item");
        if(!applicationPreSetup.load(app,configurableObject)){
            return false;
        }
        return true;
    }


    @Override
    public void setup(ServiceContext serviceContext) {
        super.setup(serviceContext);
        this.logger = JDKLogger.getLogger(PlatformConfigurationServiceProvider.class);
        gameCluster.addListener(this);
        Configuration configuration = serviceContext.configuration("game-vendor-credential-settings");
        JsonArray vlist = ((JsonElement)configuration.property("vendors")).getAsJsonArray();
        vlist.forEach(v->{
            logger.warn(v.toString());
            JsonObject jo = v.getAsJsonObject();
            vendors.put(jo.get("configuration").getAsString(),jo);
        });
        this.dataStore = applicationPreSetup.dataStore(gameCluster,NAME+"_credentials");
        this.serviceEventLogger = serviceContext.serviceEventLogger();
        this.logger.warn("Configuration service provider started on ["+gameServiceName+"]["+gameCluster.property(GameCluster.NAME)+"]");
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
        ConfigurableCategory configurableSetting = categories.configurableSetting(t.configurationCategory());
        logger.warn(configurableSetting.toString());
        logger.warn(application.distributionKey()+">>CCC"+t.distributionKey()+">>"+t.configurationVersion()+">>>"+t.configurationCategory()+">>"+t.configurationType());
    }
    public <T extends Configurable> void onUpdated(Descriptor application,T t){
        //logger.warn(application.distributionKey()+">>UUU"+t.distributionKey()+">>"+t.configurationVersion());
    }
    public <T extends Configurable> void onDeleted(Descriptor application,T t){
        logger.warn(application.distributionKey()+">>GDDD"+t.key().asString()+">>"+t.configurationVersion());
        IndexSet indexSet = new IndexSet("keys");
        indexSet.distributionKey(t.distributionKey());
        if(!dataStore.load(indexSet)) return;
        indexSet.keySet().forEach((k->{
            this.dataStore.delete(k.getBytes());
        }));
        this.dataStore.delete(indexSet.key().asString().getBytes());
    }
    public <T extends Configurable> void onCreated(GameCluster application,T t){
        //logger.warn(application.distributionKey()+">>GCCC"+t.key().asString()+">>"+t.configurationVersion());
    }
    public <T extends Configurable> void onUpdated(GameCluster application,T t){
        //logger.warn(application.distributionKey()+">>GUUU"+t.key().asString()+">>"+t.configurationVersion());
    }
    public <T extends Configurable> void onDeleted(GameCluster application,T t){
        logger.warn(application.distributionKey()+">>GDDD"+t.key().asString()+">>"+t.configurationVersion());
        IndexSet indexSet = new IndexSet("keys");
        indexSet.distributionKey(t.distributionKey());
        if(!dataStore.load(indexSet)) return;
        indexSet.keySet().forEach((k->{
            this.dataStore.delete(k.getBytes());
        }));
        this.dataStore.delete(indexSet.key().asString().getBytes());
    }

    public <T extends CredentialConfiguration> T credentialConfiguration(String vendor){
        return (T)vendorCredentials.get(vendor);
    }

}
