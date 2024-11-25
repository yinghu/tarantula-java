package com.tarantula.platform.service;


import com.google.gson.JsonObject;
import com.icodesoftware.Descriptor;
import com.icodesoftware.OnAccess;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.Transaction;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.service.MetricsListener;

import com.icodesoftware.util.HttpCaller;

import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.configuration.AppleCredentialConfiguration;
import com.tarantula.platform.configuration.AppleStoreKey;
import com.tarantula.platform.configuration.PlatformConfigurationServiceProvider;
import com.tarantula.platform.inventory.ApplicationRedeemer;
import com.tarantula.platform.service.metrics.PaymentMetrics;
import com.tarantula.platform.store.PlatformStoreServiceProvider;
import com.tarantula.platform.store.ShoppingItem;
import com.tarantula.platform.store.StoreTransactionLog;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class AppleStoreProvider extends AuthObject{

    private static final TarantulaLogger logger = JDKLogger.getLogger(AppleStoreProvider.class);

    private PlatformConfigurationServiceProvider configurationServiceProvider;
    private PlatformGameServiceProvider gameServiceProvider;
    private PlatformStoreServiceProvider storeServiceProvider;

    public AppleStoreProvider(PlatformGameServiceProvider gameServiceProvider, MetricsListener metricsListener){
        super(gameServiceProvider.gameCluster().typeId(),"");
        this.gameServiceProvider = gameServiceProvider;
        this.configurationServiceProvider = gameServiceProvider.configurationServiceProvider();
        this.storeServiceProvider = gameServiceProvider.storeServiceProvider();
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
            params.put(OnAccess.STORE_MESSAGE,"No apple store credential available ["+typeId+"]");
            return false;
        }
        try{
            String pendingTransactionId = (String)params.get("transactionId");
            StoreTransactionLog logged = storeServiceProvider.transactionLog(pendingTransactionId);
            if(logged != null){
                logger.warn(logged.toJson().toString());
                params.put(OnAccess.STORE_MESSAGE,logged.toJson().toString());
                return false;
            }
            AppleStoreKey appleStoreKey = credentialConfiguration.appleStoreKey();
            String token = appleStoreKey.token();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AppleStoreKey.production_transaction_url+pendingTransactionId))
                    .timeout(Duration.ofSeconds(TIMEOUT))
                    .header(AUTHORIZATION,"Bearer "+token)
                    .GET()
                    .build();
            HttpCaller.ResponseData responseData = new HttpCaller.ResponseData();
            int code = serviceContext.httpClientProvider().request(client->{
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                responseData.dataAsString = response.body();
                return response.statusCode();
            });
            if(code==200){
                return postValidation(appleStoreKey,responseData.dataAsString,params);
            }
            if(code == 4040010){
                HttpRequest sandBoxRequest = HttpRequest.newBuilder()
                        .uri(URI.create(AppleStoreKey.sandbox_transaction_url+pendingTransactionId))
                        .timeout(Duration.ofSeconds(TIMEOUT))
                        .header(AUTHORIZATION,"Bearer "+token)
                        .GET()
                        .build();
                HttpCaller.ResponseData sandBoxResponseData = new HttpCaller.ResponseData();
                if(serviceContext.httpClientProvider().request(client->{
                    HttpResponse<String> response = client.send(sandBoxRequest, HttpResponse.BodyHandlers.ofString());
                    sandBoxResponseData.dataAsString = response.body();
                    return response.statusCode();
                }) != 200){
                    throw new RuntimeException(sandBoxResponseData.dataAsString);
                }
                return postValidation(appleStoreKey,sandBoxResponseData.dataAsString,params);
            }
            else{
                throw new RuntimeException(responseData.dataAsString);
            }
        }catch (Exception ex){
            logger.error("apple store error ["+typeId+"]",ex);
            params.put(OnAccess.STORE_MESSAGE,"Error on validation ["+ex.getMessage()+"]");
            return false;
        }
    }

    //{"transactionId":"2000000736584713",
    // "originalTransactionId":"2000000736584713",
    // "bundleId":"com.nicegang.eighthera",
    // "productId":"gems001",
    // "purchaseDate":1728414177000,"originalPurchaseDate":1728414177000,
    // "quantity":1,"type":"Consumable","inAppOwnershipType":"PURCHASED",
    // "signedDate":1732567844005,"environment":"Sandbox",
    // "transactionReason":"PURCHASE",
    // "storefront":"USA",
    // "storefrontId":"143441",
    // "price":1990,"currency":"USD"}
    private boolean postValidation(AppleStoreKey appleStoreKey,String response,Map<String,Object> params){
        JsonObject payload = JsonUtil.parse(response);
        String requestedSku = (String)params.get(OnAccess.STORE_BUNDLE_ID);
        String requestedTransactionId = (String)params.get("transactionId");
        String systemId = (String)params.get(OnAccess.SYSTEM_ID);
        String transactionId = payload.get("transactionId").getAsString();
        String sku = payload.get("productId").getAsString();
        int quantity  = payload.get("quantity").getAsInt();
        String bundleId = payload.get("bundleId").getAsString();
        if(!transactionId.equals(requestedTransactionId)){
            logger.warn("Transaction Id not matched ["+transactionId+" : " +requestedTransactionId+ " : playerId : "+systemId);
            params.put(OnAccess.STORE_MESSAGE,"transaction not matched ["+transactionId+" : "+requestedTransactionId);
            return false;
        }
        if(!sku.equals(requestedSku)){
            logger.warn("Sku/ProductId not matched ["+sku+" : " +requestedSku+ " : playerId : "+systemId);
            params.put(OnAccess.STORE_MESSAGE,"sku/ProductID not matched ["+sku+" : "+requestedSku);
            return false;
        }
        if(!bundleId.equals(appleStoreKey.bundleId())){
            logger.warn("Bundle Id not matched ["+bundleId+" : " +appleStoreKey.bundleId()+ " : playerId : "+systemId);
            params.put(OnAccess.STORE_MESSAGE,"bundleId not matched ["+bundleId+" : "+appleStoreKey.bundleId());
            return false;
        }
        ShoppingItem shoppingItem = gameServiceProvider.storeServiceProvider().shoppingItem(sku);
        if(shoppingItem==null){
            logger.warn("Shopping Item not existed  : "+sku+ " : playerId : "+systemId);
            params.put(OnAccess.STORE_MESSAGE,"Sku not existed ["+sku+" : "+requestedSku);
            return false;
        }
        GameCluster gameCluster = gameServiceProvider.gameCluster();
        Transaction t = gameCluster.transaction();
        boolean granted = t.execute(ctx->{
            ApplicationPreSetup setup =(ApplicationPreSetup)ctx;
            Descriptor app = gameCluster.application(shoppingItem.configurationTypeId());
            ApplicationRedeemer redeemer = new ApplicationRedeemer(systemId,setup);
            redeemer.distributionKey(bundleId);
            if(!setup.load(app,redeemer)) return false;
            redeemer.redeem();
            return true;
        });
        storeServiceProvider.transactionLog(new StoreTransactionLog(Long.parseLong(systemId),OnAccess.APPLE_STORE,transactionId,shoppingItem.distributionId(),granted));
        onMetrics(PaymentMetrics.PAYMENT_APPLE_STORE_AMOUNT);
        boolean isSandbox = payload.get("environment").getAsString().equals("Sandbox");
        params.put(OnAccess.STORE_TRANSACTION_ID,transactionId);
        params.put(OnAccess.STORE_PRODUCT_ID,sku);
        params.put(OnAccess.STORE_QUANTITY,quantity);
        params.put(OnAccess.IS_SANDBOX,isSandbox);
        params.put(OnAccess.STORE_MESSAGE,"Sku granted  ["+sku+":"+granted+"]");
        return true;
    }


}
