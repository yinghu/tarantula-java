package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.Descriptor;
import com.icodesoftware.OnAccess;
import com.icodesoftware.service.Content;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.util.HttpCaller;
import com.icodesoftware.util.JWTUtil;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.*;
import com.tarantula.platform.store.ApplicationStoreProvider;
import com.tarantula.platform.service.*;
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

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PlatformConfigurationServiceProvider extends PlatformItemServiceProvider {

    public static final String NAME = "configuration";

    private final String typeId;

    private ConcurrentHashMap<String, TokenValidatorProvider.AuthVendor> registered = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String,Application> vendorCredentials = new ConcurrentHashMap<>();

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
                else if(a.configurationCategory().equals("GoogleCredentialConfiguration")){
                    GoogleCredentialConfiguration googleCredentialConfiguration = new GoogleCredentialConfiguration(typeId,a);
                    googleCredentialConfiguration.setup(serviceContext.deploymentServiceProvider(),dataStore);
                    jwt(googleCredentialConfiguration.serviceAccount());
                    vendorCredentials.put(OnAccess.GOOGLE,googleCredentialConfiguration);
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
        GoogleOAuthTokenValidator googleOAuthTokenValidator = new GoogleOAuthTokenValidator(platformGameServiceProvider,serviceContext.metrics(gameServiceName));
        this.serviceContext.registerAuthVendor(googleOAuthTokenValidator);
        registered.put(googleOAuthTokenValidator.name(),gameCenterAuthProvider);
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
        if(configurableObject.configurationCategory().equals("GoogleCredentialConfiguration")){
            GoogleCredentialConfiguration vendorConfiguration = new GoogleCredentialConfiguration(this.typeId,configurableObject);
            vendorConfiguration.setup(serviceContext.deploymentServiceProvider(),dataStore);
            vendorCredentials.put(OnAccess.GOOGLE,vendorConfiguration);
            jwt(vendorConfiguration.serviceAccount());
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
        if(configurableObject.configurationCategory().equals("VendorConfiguration")){

            return true;
        }
        if(configurableObject.configurationCategory().equals("GoogleCredentialConfiguration")){
            vendorCredentials.remove(OnAccess.GOOGLE);
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
        this.dataStore = applicationPreSetup.dataStore(gameCluster,NAME+"_credentials");
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
            googleStorePurchaseValidator.configurationServiceProvider = platformGameServiceProvider.configurationServiceProvider();
            return googleStorePurchaseValidator;
        }
        //else if(configurableObject.configurationCategory().equals("GooglePlayConfiguration")){
            //GoogleOAuthTokenValidator googleOAuthTokenValidator = new GoogleOAuthTokenValidator(new GooglePlayConfiguration(typeId,configurableObject),serviceContext.metrics(gameServiceName));
            //googleOAuthTokenValidator.registerMetricsLister(serviceContext.metrics(gameServiceName));
            //return googleOAuthTokenValidator;
        //}
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
        ConfigurationObject configurationObject = new ConfigurationObject();
        configurationObject.distributionKey(t.distributionKey());
        this.dataStore.delete(configurationObject.key().asString().getBytes());
    }
    public <T extends Configurable> void onDeleted(Descriptor application,T t){
        logger.warn(application.distributionKey()+">>DDD"+t.distributionKey()+">>"+t.configurationVersion());
    }
    public <T extends Configurable> void onCreated(GameCluster application,T t){
        //logger.warn(application.distributionKey()+">>GCCC"+t.key().asString()+">>"+t.configurationVersion());
    }
    public <T extends Configurable> void onUpdated(GameCluster application,T t){
        //logger.warn(application.distributionKey()+">>GUUU"+t.key().asString()+">>"+t.configurationVersion());
    }
    public <T extends Configurable> void onDeleted(GameCluster application,T t){
        logger.warn(application.distributionKey()+">>GDDD"+t.key().asString()+">>"+t.configurationVersion());
        ConfigurationObject configurationObject = new ConfigurationObject();
        configurationObject.distributionKey(t.distributionKey());
        this.dataStore.delete(configurationObject.key().asString().getBytes());
    }

    public GoogleCredentialConfiguration googleCredentialConfiguration(){
        return (GoogleCredentialConfiguration)vendorCredentials.get(OnAccess.GOOGLE);
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
