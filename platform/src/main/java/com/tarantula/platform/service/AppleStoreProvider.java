package com.tarantula.platform.service;


import com.google.gson.JsonObject;
import com.icodesoftware.Descriptor;
import com.icodesoftware.OnAccess;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.Transaction;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.service.MetricsListener;

import com.icodesoftware.util.Base64Util;
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
                params.put(OnAccess.STORE_MESSAGE,"Item associated with transaction ["+pendingTransactionId+"] already has granted");
                return false;
            }
            AppleStoreKey appleStoreKey = credentialConfiguration.appleStoreKey();
            String token = appleStoreKey.token();
            StoreResponse productionResponse = validate(true,token,pendingTransactionId);

            if(productionResponse.code==200){
                return postValidation(appleStoreKey,productionResponse.responseData.dataAsString,params);
            }
            if(productionResponse.code == 4040010){
                StoreResponse sandboxResponse = validate(false,token,pendingTransactionId);
                if(sandboxResponse.code != 200) throw new RuntimeException(sandboxResponse.responseData.dataAsString);
                return postValidation(appleStoreKey,sandboxResponse.responseData.dataAsString,params);
            }
            else{
                throw new RuntimeException(productionResponse.responseData.dataAsString);
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
        String[] parts = payload.get("signedTransactionInfo").getAsString().split("\\.");
        JsonObject transactionInfo = JsonUtil.parse(Base64Util.fromBase64String(parts[1]));
        String requestedSku = (String)params.get(OnAccess.STORE_BUNDLE_ID);
        String requestedTransactionId = (String)params.get("transactionId");
        String systemId = (String)params.get(OnAccess.SYSTEM_ID);
        String transactionId = transactionInfo.get("transactionId").getAsString();
        int quantity  = transactionInfo.get("quantity").getAsInt();
        String bundleId = transactionInfo.get("bundleId").getAsString();
        if(!transactionId.equals(requestedTransactionId)){
            logger.warn("Transaction Id not matched ["+transactionId+" : " +requestedTransactionId+ " : playerId : "+systemId);
            params.put(OnAccess.STORE_MESSAGE,"transaction not matched ["+transactionId+" : "+requestedTransactionId);
            return false;
        }
        if(!bundleId.equals(appleStoreKey.bundleId())){
            logger.warn("Bundle Id not matched ["+bundleId+" : " +appleStoreKey.bundleId()+ " : playerId : "+systemId);
            params.put(OnAccess.STORE_MESSAGE,"bundleId not matched ["+bundleId+" : "+appleStoreKey.bundleId());
            return false;
        }
        ShoppingItem shoppingItem = gameServiceProvider.storeServiceProvider().shoppingItem(requestedSku);
        if(shoppingItem==null){
            logger.warn("Shopping Item not existed  : "+requestedSku+ " : playerId : "+systemId);
            params.put(OnAccess.STORE_MESSAGE,"Sku not existed ["+requestedSku+"]");
            return false;
        }
        GameCluster gameCluster = gameServiceProvider.gameCluster();
        Transaction t = gameCluster.transaction();
        boolean granted = t.execute(ctx->{
            ApplicationPreSetup setup =(ApplicationPreSetup)ctx;
            Descriptor app = gameCluster.application(shoppingItem.configurationTypeId());
            ApplicationRedeemer redeemer = new ApplicationRedeemer(systemId,setup);
            redeemer.distributionKey(requestedSku);
            if(!setup.load(app,redeemer)) return false;
            redeemer.redeem();
            return true;
        });
        onMetrics(PaymentMetrics.PAYMENT_APPLE_STORE_AMOUNT);
        if(!granted){
            logger.warn("Item failed to grant  : "+requestedSku+ " : playerId : "+systemId);
            params.put(OnAccess.STORE_MESSAGE,"Item failed to grant  :  ["+requestedSku+"]");
            return false;
        }
        storeServiceProvider.transactionLog(new StoreTransactionLog(Long.parseLong(systemId),OnAccess.APPLE_STORE,transactionId,shoppingItem.distributionId(),true));
        boolean isSandbox = transactionInfo.get("environment").getAsString().equals("Sandbox");
        params.put(OnAccess.STORE_TRANSACTION_ID,transactionId);
        params.put(OnAccess.STORE_PRODUCT_ID,shoppingItem.skuName());
        params.put(OnAccess.STORE_QUANTITY,quantity);
        params.put(OnAccess.IS_SANDBOX,isSandbox);
        params.put(OnAccess.STORE_MESSAGE,"Sku granted  ["+shoppingItem.skuName()+":"+true+"]");
        return true;
    }

    private StoreResponse validate(boolean onProduction,String token,String pendingTransactionId){
        String validationUrl = onProduction?AppleStoreKey.production_transaction_url:AppleStoreKey.sandbox_transaction_url;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(validationUrl+pendingTransactionId))
                .timeout(Duration.ofSeconds(TIMEOUT))
                .header(AUTHORIZATION,"Bearer "+token)
                .GET()
                .build();
        HttpCaller.ResponseData responseData = new HttpCaller.ResponseData();
        for(int retry = 0; retry<3; retry++){
            try{
                int code = serviceContext.httpClientProvider().request(client->{
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    responseData.dataAsString = response.body();
                    return response.statusCode();
                });
                if(code==200) return new StoreResponse(code,responseData);
                JsonObject errorResponse = JsonUtil.parse(responseData.dataAsString);
                int errorCode = errorResponse.get("errorCode").getAsInt();
                responseData.dataAsString = errorResponse.get("errorMessage").getAsString();
                if(errorCode== 4040010) return new StoreResponse(errorCode,responseData);
                //anything else retry
                logger.warn("Apple store response ["+responseData.dataAsString+"]");
                logger.warn("Retrying ["+retry+"] on production ["+onProduction+"] pending transactionId ["+pendingTransactionId+"]");
                Thread.sleep(500);
            }catch (Exception ex){
                logger.error("Error on retrying ["+retry+"]",ex);
            }
        }
        //failed after retries
        throw new RuntimeException("["+pendingTransactionId+"] cannot be validated on apple store after 3 retries");
    }


    private static class StoreResponse{
        final HttpCaller.ResponseData responseData;
        final int code;
        public StoreResponse(int code,HttpCaller.ResponseData responseData){
            this.code = code;
            this.responseData = responseData;
        }
    }




}
