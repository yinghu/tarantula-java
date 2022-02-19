package com.tarantula.platform.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icodesoftware.DataStore;
import com.icodesoftware.OnAccess;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.store.Transaction;

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

public class AppleStoreProvider extends AuthObject{


    private HttpClient client;
    private DataStore dataStore;
    private JsonParser jsonParser;
    public AppleStoreProvider(String clientId, String secureKey, String authUri, String tokenUri, String certUri, String[] origins) {
        super("appleStore", clientId, secureKey, authUri, tokenUri, certUri, origins);
        try{
            SSLContext sct = SSLContext.getInstance("TLS");
            sct.init(null,new TrustManager[]{new AppleStoreProvider._X509TrustManager()},null);
            client = HttpClient.newBuilder().sslContext(sct).build();
            //if(!serverToken()) throw new RuntimeException("invalid token");
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    @Override
    public void setup(ServiceContext serviceContext){
        super.setup(serviceContext);
        jsonParser = new JsonParser();
        this.dataStore = serviceContext.dataStore("apple_store_transaction",serviceContext.partitionNumber());
    }
    @Override
    public boolean validate(Map<String,Object> params){
        try{
            String receipt = (String)params.get("receipt");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(certUri()))
                    .version(HttpClient.Version.HTTP_2)
                    .timeout(Duration.ofSeconds(TIMEOUT))
                    .header(ACCEPT, ACCEPT_JSON)
                    .header(CONTENT_TYPE,ACCEPT_JSON)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(toRequestPayload(receipt).toString().getBytes()))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return checkResponsePayload(response.body(),params);
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
    private boolean checkResponsePayload(String resp,Map<String,Object> params){
        JsonObject receipt = jsonParser.parse(resp).getAsJsonObject();
        int status = receipt.get("status").getAsInt();
        //in_app array
        boolean validated = false;
        if(status==0){
            JsonArray inApps = receipt.get("receipt").getAsJsonObject().get("in_app").getAsJsonArray();
            String pendingTransactionId = (String)params.get("transactionId");
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
        Transaction transaction = new Transaction();
        transaction.originalPayload = resp;
        this.dataStore.create(transaction);
        this.metricsListener.onUpdated(Metrics.APPLE_STORE_COUNT,1);
        return validated;
    }
    private JsonObject toRequestPayload(String receipt){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("receipt-data",receipt);
        jsonObject.addProperty("password",secureKey());
        jsonObject.addProperty("exclude-old-transactions",true);
        return jsonObject;
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
