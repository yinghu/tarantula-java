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

import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.ScheduleRunner;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
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

    private HashMap<String,Vendor> vendors = new HashMap<>();

    private ScheduledFuture<?> scheduledFuture;

    public PlatformConfigurationServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
        this.typeId = gameCluster.typeId();
    }

    @Override
    public void start() throws Exception{
        if(!serviceContext.node().homingAgent().enabled()) return;
        vendors.forEach((k,v)->{
            if(!v.disabled()){
                logger.warn("Loading data from ["+k+"] : ["+v.configuration()+"]");
                String configs = serviceContext.node().homingAgent().onConfiguration(gameCluster.distributionId(),v.configuration());
                logger.warn(configs);
                JsonObject  list = JsonUtil.parse(configs);
                list.get("list").getAsJsonArray().forEach(e->{
                    CredentialConfiguration credentialConfiguration = v.credentialConfiguration(typeId,e.getAsJsonObject());
                    if(credentialConfiguration!=null && credentialConfiguration.setup(serviceContext)){
                        vendorCredentials.put(credentialConfiguration.name(), credentialConfiguration);
                        vendorCredentials.put(Integer.toString(credentialConfiguration.publishId()),credentialConfiguration);
                        logger.warn(credentialConfiguration.name()+" : "+credentialConfiguration.publishId());
                    }
                });
            }
        });

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
                Vendor vendor = vendors.get(a.configurationCategory());
                CredentialConfiguration credentialConfiguration = vendor.credentialConfiguration(typeId,a);
                if(credentialConfiguration!=null && credentialConfiguration.setup(serviceContext)){
                    vendorCredentials.put(credentialConfiguration.name(),credentialConfiguration);
                    vendorCredentials.put(credentialConfiguration.distributionKey(),credentialConfiguration);
                    logger.warn(credentialConfiguration.name()+" : "+credentialConfiguration.distributionKey());
                }
            }
        });
        vendors.forEach((k,c)->{
            if(!c.disabled()){
                List<String> providers = c.providers();
                providers.forEach(p->{
                    logger.warn("Register provider->"+p);
                    try{
                        TokenValidatorProvider.AuthVendor authVendor = (TokenValidatorProvider.AuthVendor) Class.forName(p).getConstructor(PlatformGameServiceProvider.class, MetricsListener.class).newInstance(platformGameServiceProvider,serviceContext.metrics(gameServiceName));
                        serviceContext.registerAuthVendor(authVendor);
                        registered.put(authVendor.name(),authVendor);
                    } catch (Exception ex){
                        logger.warn("Provider setup failed->"+p,ex);
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
        Vendor vendor = vendors.get(configurableObject.configurationCategory());
        if(vendor==null || vendor.disabled()){
            logger.warn(configurableObject.configurationCategory()+" is disabled");
            return false;
        }
        CredentialConfiguration credentialConfiguration = vendor.credentialConfiguration(this.typeId,configurableObject);
        if (credentialConfiguration != null && credentialConfiguration.setup(serviceContext)) {
            vendorCredentials.put(credentialConfiguration.name(), credentialConfiguration);
            vendorCredentials.put(credentialConfiguration.distributionKey(),credentialConfiguration);
            logger.warn(credentialConfiguration.name()+" : "+credentialConfiguration.distributionKey());
            return true;
        }
        return false;
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
            vendors.put(jo.get("configuration").getAsString(),new Vendor(jo));
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


    @Override
    public void register(int publishId) {
        logger.warn("register : "+publishId);
        distributionItemService.onRegisterItem(gameServiceName,name(),publishId);
    }

    @Override
    public void release(int publishId) {
        logger.warn("release : "+publishId);
        distributionItemService.onReleaseItem(gameServiceName,name(),publishId);
    }

    public boolean onItemRegistered(int publishId){
        String config = serviceContext.node().homingAgent().onConfigurationRegistered(publishId);
        logger.warn(config);
        JsonObject load = JsonUtil.parse(config);
        String category = load.get("ConfigurationCategory").getAsString();
        Vendor vendor = vendors.get(category);
        if(vendor==null || vendor.disabled()){
            logger.warn(category+" is disabled");
            return false;
        }
        CredentialConfiguration credentialConfiguration = vendor.credentialConfiguration(this.typeId,load);
        if(credentialConfiguration==null) return false;
        if(!credentialConfiguration.setup(serviceContext)) return false;
        vendorCredentials.put(credentialConfiguration.name(), credentialConfiguration);
        vendorCredentials.put(Integer.toString(credentialConfiguration.publishId()),credentialConfiguration);
        logger.warn(credentialConfiguration.name()+" : "+credentialConfiguration.publishId());
        return true;

    }
    public boolean onItemReleased(int publishId) {
        logger.warn("release local resource with [" + publishId + "]");
        CredentialConfiguration removed = vendorCredentials.remove(Integer.toString(publishId));
        if(removed==null) return false;
        logger.warn("removed credential configuration : "+removed.name());
        vendorCredentials.remove(removed.name());
        return true;
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



}
