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
import com.tarantula.platform.configuration.GoogleCredentialConfiguration;
import com.tarantula.platform.configuration.GoogleServiceAccount;
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



public class GoogleStorePurchaseValidator extends AuthObject {

    private static final TarantulaLogger logger = JDKLogger.getLogger(GoogleStorePurchaseValidator.class);
    private final static String VALIDATION_URI = "https://androidpublisher.googleapis.com/androidpublisher/v3/applications/";

    //"acknowledge_uri": "https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{packageName}/purchases/products/{productId}/tokens/{token}:acknowledge"


    private PlatformConfigurationServiceProvider configurationServiceProvider;
    private PlatformGameServiceProvider gameServiceProvider;
    private PlatformStoreServiceProvider storeServiceProvider;

    public GoogleStorePurchaseValidator(PlatformGameServiceProvider gameServiceProvider, MetricsListener metricsListener) {
        super(gameServiceProvider.gameCluster().typeId(), "");
        this.gameServiceProvider = gameServiceProvider;
        this.configurationServiceProvider = gameServiceProvider.configurationServiceProvider();
        this.storeServiceProvider = gameServiceProvider.storeServiceProvider();
        this.applicationMetricsListener = metricsListener;
    }

    @Override
    public String name() {
        return OnAccess.GOOGLE_STORE;
    }


    public boolean validate(Map<String, Object> params) {
        try {
            GoogleCredentialConfiguration googleCredentialConfiguration = configurationServiceProvider.credentialConfiguration(OnAccess.GOOGLE);
            if (googleCredentialConfiguration == null) {
                logger.warn("No credential available [" + typeId + "]");
                return false;
            }
            String pendingTransactionId = (String)params.get("transactionId");
            StoreTransactionLog logged = storeServiceProvider.transactionLog(pendingTransactionId);
            if(logged != null){
                logger.warn(logged.toJson().toString());
                return false;
            }
            GoogleServiceAccount serviceAccount = googleCredentialConfiguration.serviceAccount();
            String _tk = serviceAccount.token(serviceContext);
            String sku = (String) params.get(OnAccess.STORE_PRODUCT_ID);
            String token = (String) params.get(OnAccess.STORE_RECEIPT);

            String query = new StringBuffer(VALIDATION_URI).append(googleCredentialConfiguration.packageName()).append("/purchases/products/").append(sku)
                    .append("/tokens/").append(token).toString();

            HttpRequest _request = HttpRequest.newBuilder()
                    .uri(URI.create(query))
                    .timeout(Duration.ofSeconds(TIMEOUT))
                    .header(AUTHORIZATION, "Bearer " + _tk)
                    .header(ACCEPT, ACCEPT_JSON)
                    .GET()
                    .build();
            HttpCaller.ResponseData responseData = new HttpCaller.ResponseData();

            int code = this.serviceContext.httpClientProvider().request(client -> {
                HttpResponse<String> _response = client.send(_request, HttpResponse.BodyHandlers.ofString());
                responseData.dataAsString = _response.body();
                return _response.statusCode();
            });

            if (code != 200) throw new RuntimeException("Google store gateway none 200 response");
            onMetrics(PaymentMetrics.PAYMENT_GOOGLE_STORE_AMOUNT);

            if (!checkResponsePayload(responseData.dataAsString, params)) return false;

            consumePurchase(query, _tk);

            String bundleId = (String) params.get(OnAccess.STORE_BUNDLE_ID);
            long systemId = (long) params.get(OnAccess.SYSTEM_ID);
            ShoppingItem shoppingItem = gameServiceProvider.storeServiceProvider().shoppingItem(bundleId);

            if (shoppingItem == null) {
                logger.warn("Shopping Item not existed  : " + bundleId);
                throw new RuntimeException("Shopping not existed [" + bundleId + "]");
            }


            GameCluster gameCluster = gameServiceProvider.gameCluster();
            Transaction t = gameCluster.transaction();

            boolean suc = t.execute(ctx -> {
                ApplicationPreSetup setup = (ApplicationPreSetup) ctx;
                Descriptor app = gameCluster.application(shoppingItem.configurationTypeId());
                ApplicationRedeemer redeemer = new ApplicationRedeemer(systemId, setup);
                redeemer.distributionKey(bundleId);
                if (!setup.load(app, redeemer)) return false;
                redeemer.redeem();
                return true;
            });
            storeServiceProvider.transactionLog(new StoreTransactionLog((systemId),OnAccess.GOOGLE_STORE,pendingTransactionId,shoppingItem.distributionId(),suc));

            if (suc) return true;

            logger.warn("Item : " + bundleId + " cannot be redeemed");

            throw new RuntimeException("Item : " + bundleId + " cannot be redeemed");
        } catch (Exception ex) {
            logger.error("Error on google pay", ex);
            return false;
        }
    }

    private boolean checkResponsePayload(String resp, Map<String, Object> params) {
        String pendingTransactionId = (String) params.get("transactionId");
        JsonObject receipt = JsonParser.parseString(resp).getAsJsonObject();
        int status = receipt.get("purchaseState").getAsInt();
        int consumptionState = receipt.get("consumptionState").getAsInt();
        boolean validated = false;
        if (status == 0 & consumptionState == 0) {
            String transactionId = receipt.get("orderId").getAsString();
            if (transactionId.equals(pendingTransactionId)) {
                String sku = (String) params.get("storeProductId");
                params.put(OnAccess.STORE_TRANSACTION_ID, transactionId);
                params.put(OnAccess.STORE_PRODUCT_ID, sku);
                params.put(OnAccess.STORE_QUANTITY, 1);

                if(receipt.has("purchaseType") && receipt.get("purchaseType").getAsInt() == 0){
                    params.put(OnAccess.IS_SANDBOX, true);
                }
                else{
                    params.put(OnAccess.IS_SANDBOX, false);
                }

                validated = true;
            }
        } else {
            logger.warn("failure Google store response [" + status + "]");
            logger.warn(receipt.toString());
        }
        if (!validated) {
            params.put(OnAccess.STORE_MESSAGE, "transaction cannot be validated");
        }
        return validated;
    }

    private boolean consumePurchase(String query, String _tk) throws Exception{
        String consumeQuery = query + ":consume";

        HttpRequest _request = HttpRequest.newBuilder()
                .uri(URI.create(consumeQuery))
                .timeout(Duration.ofSeconds(TIMEOUT))
                .header(AUTHORIZATION,"Bearer "+_tk)
                .POST(HttpRequest.BodyPublishers.noBody())

                .build();

        HttpCaller.ResponseData responseData = new HttpCaller.ResponseData();

        int code = serviceContext.httpClientProvider().request(client->{
            HttpResponse<String> _response = client.send(_request, HttpResponse.BodyHandlers.ofString());
            return _response.statusCode();
        });

        if(code!=200) logger.warn("Status of Google consume not 200: " + code);

        logger.warn("Item consumed!");

        return true;
    }
}
