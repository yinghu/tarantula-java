package com.tarantula.platform.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.gson.JsonObject;
import com.icodesoftware.OnAccess;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.configuration.GoogleStoreConfiguration;
import com.tarantula.platform.service.metrics.GameClusterMetrics;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Map;


public class GoogleStorePurchaseValidator extends AuthObject {

    private final static String VALIDATION_URI = "https://androidpublisher.googleapis.com/androidpublisher/v3/applications/";

    //"acknowledge_uri": "https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{packageName}/purchases/products/{productId}/tokens/{token}:acknowledge"

    private HttpClient client;
    private String accessKey;
    private String packageName;

    public GoogleStorePurchaseValidator(GoogleStoreConfiguration googleStoreConfiguration, MetricsListener metricsListener){
        this(googleStoreConfiguration.typeId(),googleStoreConfiguration.packageName(),googleStoreConfiguration.secretKey());
        this.applicationMetricsListener = metricsListener;
    }

    public GoogleStorePurchaseValidator(String typeId, String packageName, String accessKey) {
        super(typeId,"");
        this.packageName = packageName;
        this.accessKey = accessKey;
        try{
            SSLContext sct = SSLContext.getInstance("TLS");
            sct.init(null,new TrustManager[]{new GoogleStorePurchaseValidator._X509TrustManager()},null);
            client = HttpClient.newBuilder().sslContext(sct).build();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String name(){
        return OnAccess.GOOGLE_STORE;
    }

    @Override
    public void setup(ServiceContext serviceContext){
        super.setup(serviceContext);
    }


    public boolean validate(Map<String,Object> params){
        try{
            String sku = (String) params.get(OnAccess.STORE_PRODUCT_ID);
            String token = (String)params.get(OnAccess.STORE_RECEIPT);
            String orderId = (String)params.get(OnAccess.STORE_TRANSACTION_ID);
            //{packageName}/purchases/products/{productId}/tokens/{token}",
            String query = new StringBuffer(VALIDATION_URI).append(packageName).append("/purchases/products/").append(sku)
                    .append("/tokens/").append(token).append("?key=").append(accessKey).toString();
            HttpRequest _request = HttpRequest.newBuilder()
                    .uri(URI.create(query))
                    .timeout(Duration.ofSeconds(TIMEOUT))
                    //.header(AUTHORIZATION,"Bearer "+ accessToken)
                    .header(ACCEPT, ACCEPT_JSON)
                    .GET()
                    .build();
            HttpResponse<String> _response = client.send(_request, HttpResponse.BodyHandlers.ofString());
            JsonObject payload = JsonUtil.parse(_response.body());
            onMetrics(GameClusterMetrics.PAYMENT_GOOGLE_STORE_COUNT);
            return (payload.has("orderId")&&payload.get("orderId").getAsString().equals(orderId));
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    private class _X509TrustManager implements X509TrustManager {
        private X509Certificate[] certificate;
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            //run on server
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            //run on client to check if certificate is valid
            //if(!chain[0].getSubjectDN().getName().equals("CN=gameclustering.com")){
            //throw new CertificateException("Invalid certificate");
            //}
            certificate = chain;
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return this.certificate;
        }

    }

}
