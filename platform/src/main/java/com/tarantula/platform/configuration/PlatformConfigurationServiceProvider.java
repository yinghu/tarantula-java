package com.tarantula.platform.configuration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.Configuration;
import com.icodesoftware.Descriptor;

import com.icodesoftware.OnAccess;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.*;

import com.icodesoftware.util.ScheduleRunner;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.OnAccessTrack;
import com.tarantula.platform.item.*;
import com.tarantula.platform.util.RecoverableQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

public class PlatformConfigurationServiceProvider extends PlatformItemServiceProvider {

    public static final String NAME = "configuration";

    private final String typeId;

    private ConcurrentHashMap<String, TokenValidatorProvider.AuthVendor> registered = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String,CredentialConfiguration> vendorCredentials = new ConcurrentHashMap<>();

    private HashMap<String,JsonObject> vendors = new HashMap<>();

    private ScheduledFuture<?> scheduledFuture;
    public PlatformConfigurationServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
        this.typeId = gameCluster.typeId();
    }

    @Override
    public void shutdown() throws Exception {
        scheduledFuture.cancel(true);
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
                        vendorCredentials.put(credentialConfiguration.distributionKey(),credentialConfiguration);
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
        exposeMetrics();
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
        GameCluster _gc = serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionId());
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
                vendorCredentials.put(credentialConfiguration.distributionKey(),credentialConfiguration);
                logger.warn(credentialConfiguration.name()+" : "+credentialConfiguration.distributionKey());

                if (credentialConfiguration.name().equals("CheatDetection")){
                    OnAccess access = new OnAccessTrack();
                    access.command("CheatDetectionConfigUpdated");
                    platformGameServiceProvider.onGameClusterEvent(access);
                }
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
        GameCluster _gc = serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionId());
        Descriptor app = _gc.serviceWithCategory("item");
        if(!applicationPreSetup.load(app,configurableObject)){
            return false;
        }
        CredentialConfiguration credentialConfiguration = vendorCredentials.remove(itemId);
        if(credentialConfiguration==null) return true;
        CredentialConfiguration cx = vendorCredentials.remove(credentialConfiguration.name());
        cx.release();
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
        this.logger.warn("Configuration service provider started on ["+gameServiceName+"]["+gameCluster.property(GameCluster.NAME)+"]");
    }

    public <T extends Configurable> void onCreated(Descriptor application,T t){

    }
    public <T extends Configurable> void onUpdated(Descriptor application,T t){
        logger.warn("APP Updated : "+t.distributionKey());
    }
    public <T extends Configurable> void onDeleted(Descriptor application,T t){
        RecoverableQuery<ConfigurationObject> query = new RecoverableQuery<>(t.key(),ConfigurationObject.LABEL, ItemPortableRegistry.CONFIGURATION_OBJECT_CID,ItemPortableRegistry.INS);
        dataStore.list(query).forEach(c->{
            dataStore.delete(c);
        });
    }
    public <T extends Configurable> void onCreated(GameCluster application,T t){

    }
    public <T extends Configurable> void onUpdated(GameCluster application,T t){

    }
    public <T extends Configurable> void onDeleted(GameCluster application,T t){

    }

    public <T extends CredentialConfiguration> T credentialConfiguration(String vendor){
        return (T)vendorCredentials.get(vendor);
    }

    private void exposeMetrics(){
        try {
            TokenValidatorProvider tokenValidatorProvider = (TokenValidatorProvider) serviceContext.serviceProvider(TokenValidatorProvider.NAME);
            TokenValidatorProvider.AuthVendor authVendor = tokenValidatorProvider.authVendor(OnAccess.JDBC_SQL);
            List<String> list = serviceContext.metricsList();
            list.forEach(m->{
                Metrics metrics = serviceContext.metrics(m);
                String query = typeId+"#metrics";
                authVendor.upload(query,metrics.statistics());
            });
        }catch (Exception ex){
            logger.error("error",ex);
        }
        scheduledFuture = serviceContext.schedule(new ScheduleRunner(5000,()->{
            exposeMetrics();
        }));
    }

    public Map<Long,MailboxCredentialConfiguration> inbox(){
        HashMap<Long,MailboxCredentialConfiguration> tem = new HashMap<>();
        vendorCredentials.forEach((k,c)->{
            if(c instanceof MailboxCredentialConfiguration){
                MailboxCredentialConfiguration m = (MailboxCredentialConfiguration)c;
                if(m.inbox() && !tem.containsKey(c.distributionId())){
                    tem.put(c.distributionId(),(MailboxCredentialConfiguration)c);
                }
            }
        });
        return tem;
    }

    public SeasonCredentialConfiguration seasonCredentialConfiguration(){
        return (SeasonCredentialConfiguration)vendorCredentials.get(OnAccess.SEASON);
    }

}
