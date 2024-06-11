package com.tarantula.platform.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icodesoftware.Descriptor;
import com.icodesoftware.OnAccess;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.Transaction;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.service.MetricsListener;

import com.icodesoftware.util.HttpCaller;

import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.configuration.AppleCredentialConfiguration;
import com.tarantula.platform.configuration.AppleStoreKey;
import com.tarantula.platform.configuration.PlatformConfigurationServiceProvider;
import com.tarantula.platform.inventory.ApplicationRedeemer;
import com.tarantula.platform.service.metrics.PaymentMetrics;
import com.tarantula.platform.store.ShoppingItem;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class AppleStoreProvider extends AuthObject{

    private static final TarantulaLogger logger = JDKLogger.getLogger(AppleStoreProvider.class);
    private final static String  SANDBOX_VERIFY_URI = "https://sandbox.itunes.apple.com/verifyReceipt";
    private final static String  PRODUCTION_VERIFY_URI = "https://buy.itunes.apple.com/verifyReceipt";

    private PlatformConfigurationServiceProvider configurationServiceProvider;
    private PlatformGameServiceProvider gameServiceProvider;

    public AppleStoreProvider(PlatformGameServiceProvider gameServiceProvider, MetricsListener metricsListener){
        super(gameServiceProvider.gameCluster().typeId(),"");
        this.gameServiceProvider = gameServiceProvider;
        this.configurationServiceProvider = gameServiceProvider.configurationServiceProvider();
        this.applicationMetricsListener = metricsListener;
    }

    @Override
    public String name(){
        return OnAccess.APPLE_STORE;
    }

    @Override
    public boolean validate(Map<String,Object> params){
        AppleCredentialConfiguration credentialConfiguration = configurationServiceProvider.credentialConfiguration(OnAccess.APPLE);
        if(credentialConfiguration==null){
            logger.warn("no apple credential available ["+typeId+"]");
            return false;
        }
        try{
            AppleStoreKey appleStoreKey = credentialConfiguration.appleStoreKey();
            String receipt = (String)params.get("receipt");
            //String serviceTypeId = (String)params.get(OnAccess.TYPE_ID);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(appleStoreKey.isSandbox()?SANDBOX_VERIFY_URI:PRODUCTION_VERIFY_URI))
                    .version(HttpClient.Version.HTTP_2)
                    .timeout(Duration.ofSeconds(TIMEOUT))
                    .header(ACCEPT, ACCEPT_JSON)
                    .header(CONTENT_TYPE,ACCEPT_JSON)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(toRequestPayload(appleStoreKey,receipt).toString().getBytes()))
                    .build();
            HttpCaller.ResponseData responseData = new HttpCaller.ResponseData();
            int code = serviceContext.httpClientProvider().request(client->{
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                responseData.dataAsString = response.body();
                return response.statusCode();
            });
            if(code!=200) throw new RuntimeException("apple store gateway none 200 response");
            onMetrics(PaymentMetrics.PAYMENT_APPLE_STORE_AMOUNT);
            if(!checkResponsePayload(responseData.dataAsString,params)) return false;
            String bundleId = (String)params.get(OnAccess.STORE_BUNDLE_ID);
            long systemId = (long)params.get(OnAccess.SYSTEM_ID);
            ShoppingItem shoppingItem = gameServiceProvider.storeServiceProvider().shoppingItem(bundleId);
            if(shoppingItem==null){
                logger.warn("Shopping Item not existed  : "+bundleId);
                throw new RuntimeException("Shopping not existed ["+bundleId+"]");
            }
            GameCluster gameCluster = gameServiceProvider.gameCluster();
            boolean[] suc ={false};
            try(Transaction t = gameCluster.transaction()){
                suc[0] = t.execute(ctx->{
                    ApplicationPreSetup setup =(ApplicationPreSetup)ctx;
                    Descriptor app = gameCluster.application(shoppingItem.configurationTypeId());
                    ApplicationRedeemer redeemer = new ApplicationRedeemer(systemId,setup);
                    redeemer.distributionKey(bundleId);
                    if(!setup.load(app,redeemer)) return false;
                    redeemer.redeem();
                    return true;
                });
            }
            if(suc[0]) return true;
            logger.warn("Item : "+bundleId+" cannot be redeemed");
            throw new RuntimeException("Item : "+bundleId+" cannot be redeemed");
        }catch (Exception ex){
            logger.error("apple store error ["+typeId+"]",ex);
            return false;
        }
    }
    private boolean checkResponsePayload(String resp,Map<String,Object> params){
        String pendingTransactionId = (String)params.get("transactionId");
        JsonObject receipt = JsonParser.parseString(resp).getAsJsonObject();
        int status = receipt.get("status").getAsInt();
        //in_app array
        boolean validated = false;
        if(status==0){
            JsonArray inApps = receipt.get("receipt").getAsJsonObject().get("in_app").getAsJsonArray();
            for(JsonElement inApp : inApps){
                JsonObject transaction = inApp.getAsJsonObject();
                String transactionId = transaction.get("transaction_id").getAsString();
                if(transactionId.equals(pendingTransactionId)){
                    String sku = transaction.get("product_id").getAsString();
                    int quantity  = transaction.get("quantity").getAsInt();
                    params.put(OnAccess.STORE_TRANSACTION_ID,transactionId);
                    params.put(OnAccess.STORE_PRODUCT_ID,sku);
                    params.put(OnAccess.STORE_QUANTITY,quantity);
                    validated = true;
                    break;
                }
            }
        }
        else{
            logger.warn("failure apple store response ["+status+"]");
            logger.warn(receipt.toString());
            //to do send out a system message
        }
        if(!validated){
            params.put(OnAccess.STORE_MESSAGE,"transaction cannot be validated");
        }
        return validated;
    }
    private JsonObject toRequestPayload(AppleStoreKey appleStoreKey,String receipt){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("receipt-data",receipt);
        jsonObject.addProperty("password",appleStoreKey.secureKey());
        jsonObject.addProperty("exclude-old-transactions",true);
        return jsonObject;
    }
}
