package com.tarantula.platform.configuration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.Configuration;
import com.icodesoftware.Descriptor;
import com.icodesoftware.OnAccess;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.util.HttpCaller;
import com.icodesoftware.util.JWTUtil;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.item.*;
import com.tarantula.platform.service.persistence.mysql.MysqlBackupProvider;
import com.tarantula.platform.util.SystemUtil;


import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyFactory;
import java.security.PrivateKey;

import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PlatformConfigurationServiceProvider extends PlatformItemServiceProvider {

    public static final String NAME = "configuration";

    private final String typeId;

    private ConcurrentHashMap<String, TokenValidatorProvider.AuthVendor> registered = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String,CredentialConfiguration> vendorCredentials = new ConcurrentHashMap<>();

    private HashMap<String,JsonObject> vendors = new HashMap<>();
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
        JsonObject vendor = vendors.get(t.configurationCategory());
        if(vendor==null|| vendor.get("disabled").getAsBoolean()) throw new RuntimeException(t.configurationCategory()+" disabled");
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
        this.logger = serviceContext.logger(PlatformConfigurationServiceProvider.class);
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
    private TokenValidatorProvider.AuthVendor toAuthVendor(ConfigurableObject configurableObject){


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

    public String jwt(GoogleServiceAccount serviceAccount){
        try{
            //JsonObject credential = JsonUtil.parse(configurationObject.value());
            byte[] key = SystemUtil.fromPemString(serviceAccount.privateKey());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key);
            PrivateKey pkey = keyFactory.generatePrivate(keySpec);
            JWTUtil.JWT jwt = JWTUtil.init(pkey);
            String token = jwt.token((h,p)->{
                h.addProperty("kid",serviceAccount.privateKeyId());
                p.addProperty("aud",serviceAccount.tokenUri());
                //Instant cc = Instant.now();
                //LocalDateTime localDateTime = LocalDateTime.now();
                long x = Instant.now().getEpochSecond();//TimeUtil.toUTCSeconds(localDateTime);
                p.addProperty("iat",x);
                long y = x+3600;//TimeUtil.toUTCSeconds(localDateTime.plusMinutes(60));
                logger.warn("IAT->"+(x));
                logger.warn("EXP->"+(y));
                p.addProperty("exp",y);
                p.addProperty("iss",serviceAccount.clientEmail());
                p.addProperty("scope","https://www.googleapis.com/auth/androidpublisher");
                p.addProperty("sub",serviceAccount.clientEmail());
                logger.warn(h.toString());
                logger.warn(p.toString());
                return true;
            });
            StringBuffer query = new StringBuffer(serviceAccount.tokenUri());
            query.append("?");
            query.append("grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer&assertion=");
            query.append(token);
            String ACCEPT = "Accept";
            String ACCEPT_JSON = "application/json";
            String CONTENT_TYPE = "Content-type";
            String CONTENT_FORM = "application/x-www-form-urlencoded";
            HttpRequest _request = HttpRequest.newBuilder()
                    .uri(URI.create(query.toString()))
                    .timeout(Duration.ofSeconds(10))
                    .header(ACCEPT, ACCEPT_JSON)
                    .header(CONTENT_TYPE, CONTENT_FORM)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpCaller.ResponseData responseData = new HttpCaller.ResponseData();
            int code = this.serviceContext.httpClientProvider().request(client->{
                HttpResponse<String> _response = client.send(_request, HttpResponse.BodyHandlers.ofString());
                responseData.dataAsString = _response.body();
                return _response.statusCode();
            });
            logger.warn(token);
            logger.warn("code->"+code);
            logger.warn("resp->"+responseData.dataAsString);
            JsonObject resp = JsonUtil.parse(responseData.dataAsString);
            return resp.get("access_token").getAsString();
        }catch (Exception ex){
            logger.error("err",ex);
            throw new RuntimeException(ex);
        }
    }

}
