package com.tarantula.platform.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icodesoftware.OnAccess;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.MetricsListener;

import com.icodesoftware.service.ServiceEventLogger;
import com.icodesoftware.util.HttpCaller;

import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.configuration.AppleCredentialConfiguration;
import com.tarantula.platform.configuration.AppleStoreKey;
import com.tarantula.platform.configuration.PlatformConfigurationServiceProvider;
import com.tarantula.platform.service.metrics.GameClusterMetrics;
import com.tarantula.platform.store.Transaction;

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
    private ServiceEventLogger transactionLogger;
    public AppleStoreProvider(PlatformGameServiceProvider gameServiceProvider, MetricsListener metricsListener){
        super(gameServiceProvider.gameCluster().typeId(),"");
        transactionLogger = gameServiceProvider.transactionEventLogger("apple_store");
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
            String serviceTypeId = (String)params.get(OnAccess.TYPE_ID);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(appleStoreKey.isSandbox()?SANDBOX_VERIFY_URI:PRODUCTION_VERIFY_URI))
                    .version(HttpClient.Version.HTTP_2)
                    .timeout(Duration.ofSeconds(TIMEOUT))
                    .header(ACCEPT, ACCEPT_JSON)
                    .header(CONTENT_TYPE,ACCEPT_JSON)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(toRequestPayload(appleStoreKey,serviceTypeId,receipt).toString().getBytes()))
                    .build();
            HttpCaller.ResponseData responseData = new HttpCaller.ResponseData();
            int code = serviceContext.httpClientProvider().request(client->{
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                responseData.dataAsString = response.body();
                return response.statusCode();
            });
            if(code!=200) return false;
            onMetrics(GameClusterMetrics.PAYMENT_APPLE_STORE_AMOUNT);
            return checkResponsePayload(responseData.dataAsString,params);
        }catch (Exception ex){
            logger.error("apple store error ["+typeId+"]",ex);
            return false;
        }
    }
    private boolean checkResponsePayload(String resp,Map<String,Object> params){
        String systemId = (String) params.get(OnAccess.SYSTEM_ID);
        String pendingTransactionId = (String)params.get("transactionId");
        //String serviceTypeId = (String)params.get(OnAccess.TYPE_ID);
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
        if(!validated){
            params.put(OnAccess.STORE_MESSAGE,"transaction cannot be validated");
        }
        Transaction transaction = new Transaction(systemId,(String)params.get(OnAccess.STORE_BUNDLE_ID),resp);
        transaction.distributionKey(pendingTransactionId);
        serviceEventLogger.log(transaction);
        //this.metricsListener.onUpdated(VendorMetrics.APPLE_STORE_COUNT,1);
        return validated;
    }
    private JsonObject toRequestPayload(AppleStoreKey appleStoreKey,String serviceTypeId,String receipt){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("receipt-data",receipt);
        jsonObject.addProperty("password",appleStoreKey.secureKey());
        jsonObject.addProperty("exclude-old-transactions",true);
        return jsonObject;
    }
}
