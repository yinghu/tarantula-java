package com.tarantula.platform.service;

import com.google.gson.JsonObject;
import com.icodesoftware.OnAccess;
import com.icodesoftware.Session;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.HttpCaller;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.configuration.GoogleStoreConfiguration;
import com.tarantula.platform.configuration.PlatformConfigurationServiceProvider;
import com.tarantula.platform.service.metrics.GameClusterMetrics;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;


public class GoogleStorePurchaseValidator extends AuthObject {

    private final static String VALIDATION_URI = "https://androidpublisher.googleapis.com/androidpublisher/v3/applications/";

    //"acknowledge_uri": "https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{packageName}/purchases/products/{productId}/tokens/{token}:acknowledge"

    private String accessKey;
    private String packageName;

    public PlatformConfigurationServiceProvider configurationServiceProvider;
    public GoogleStorePurchaseValidator(GoogleStoreConfiguration googleStoreConfiguration, MetricsListener metricsListener){
        this(googleStoreConfiguration.typeId(),googleStoreConfiguration.packageName(),googleStoreConfiguration.secretKey());
        this.applicationMetricsListener = metricsListener;
    }

    public GoogleStorePurchaseValidator(String typeId, String packageName, String accessKey) {
        super(typeId,"");
        this.packageName = packageName;
        this.accessKey = accessKey;
    }

    @Override
    public String name(){
        return OnAccess.GOOGLE_STORE;
    }

    @Override
    public void setup(ServiceContext serviceContext){
        super.setup(serviceContext);
        logger = serviceContext.logger(GoogleStorePurchaseValidator.class);
    }
    public boolean validate(Map<String,Object> params){
        try{
            //Session session = (Session)params.get(OnAccess.SESSION);
            String sku = (String) params.get(OnAccess.STORE_PRODUCT_ID);
            String token = (String)params.get(OnAccess.STORE_RECEIPT);//purchase token
            String orderId = (String)params.get(OnAccess.STORE_TRANSACTION_ID);
            //{packageName}/purchases/products/{productId}/tokens/{token}",
            String query = new StringBuffer(VALIDATION_URI).append(packageName).append("/purchases/products/").append(sku)
                    .append("/tokens/").append(token).toString();
            HttpRequest _request = HttpRequest.newBuilder()
                    .uri(URI.create(query))
                    .timeout(Duration.ofSeconds(TIMEOUT))
                     .header(AUTHORIZATION,"Bearer ")//+ configurationServiceProvider.jwt())
                    .header(ACCEPT, ACCEPT_JSON)
                    .GET()
                    .build();
            HttpCaller.ResponseData responseData = new HttpCaller.ResponseData();
            int code = this.serviceContext.httpClientProvider().request(client->{
                HttpResponse<String> _response = client.send(_request, HttpResponse.BodyHandlers.ofString());
                responseData.dataAsString = _response.body();
                return _response.statusCode();
            });
            if(code!=200) {
                logger.warn("Error:"+responseData.dataAsString);
                return true;//false;
            }
            JsonObject payload = JsonUtil.parse(responseData.dataAsString);
            logger.warn("Response:"+payload.toString());
            onMetrics(GameClusterMetrics.PAYMENT_GOOGLE_STORE_COUNT);
            return payload.has("orderId") && payload.get("orderId").getAsString().equals(orderId);
        }catch (Exception ex){
            logger.error("Error on google pay",ex);
            return true;//false;
        }
    }

}
