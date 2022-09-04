package com.tarantula.platform.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icodesoftware.DataStore;
import com.icodesoftware.OnAccess;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.ValidationUtil;
import com.tarantula.platform.configuration.AppleStoreConfiguration;
import com.tarantula.platform.service.metrics.GameClusterMetrics;
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

    private final static String  SANDBOX_VERIFY_URI = "https://sandbox.itunes.apple.com/verifyReceipt";
    private final static String  PRODUCTION_VERIFY_URI = "https://buy.itunes.apple.com/verifyReceipt";


    private HttpClient client;
    private JsonParser jsonParser;
    private DataStore dataStore;

    private String secureKey;
    private boolean isSandbox;

    public AppleStoreProvider(AppleStoreConfiguration appleStoreConfiguration, MetricsListener metricsListener){
        this(appleStoreConfiguration.typeId(),appleStoreConfiguration.secureKey(),appleStoreConfiguration.isSandbox());
        this.applicationMetricsListener = metricsListener;
    }

    public AppleStoreProvider(String typeId,String key,boolean isSandbox){
        super(typeId,"");
        this.secureKey = key;
        this.isSandbox = isSandbox;
        try{
            SSLContext sct = SSLContext.getInstance("TLS");
            sct.init(null,new TrustManager[]{new AppleStoreProvider._X509TrustManager()},null);
            client = HttpClient.newBuilder().sslContext(sct).build();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    @Override
    public void setup(ServiceContext serviceContext){
        super.setup(serviceContext);
        jsonParser = new JsonParser();
        String ds = typeId.replaceAll("-","_")+"_apple_store_transaction";
        dataStore = serviceContext.dataStore(ds,serviceContext.partitionNumber());

    }
    @Override
    public boolean validate(Map<String,Object> params){
        try{
            if(checkTransactionExisted(params)) return false;
            String receipt = (String)params.get("receipt");
            String serviceTypeId = (String)params.get(OnAccess.TYPE_ID);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(isSandbox?SANDBOX_VERIFY_URI:PRODUCTION_VERIFY_URI))
                    .version(HttpClient.Version.HTTP_2)
                    .timeout(Duration.ofSeconds(TIMEOUT))
                    .header(ACCEPT, ACCEPT_JSON)
                    .header(CONTENT_TYPE,ACCEPT_JSON)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(toRequestPayload(serviceTypeId,receipt).toString().getBytes()))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            onMetrics(GameClusterMetrics.PAYMENT_APPLE_STORE_AMOUNT);
            return checkResponsePayload(response.body(),params);
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
    private boolean checkTransactionExisted(Map<String,Object> params){
        String systemId = (String) params.get(OnAccess.SYSTEM_ID);
        String transactionId = (String) params.get("transactionId");
        //String serviceTypeId = (String)params.get(OnAccess.TYPE_ID);
        Transaction transaction = new Transaction();
        transaction.index(transactionId);
        transaction.owner(systemId);
        if(dataStore.load(transaction)){
            params.put(OnAccess.STORE_MESSAGE,"duplicated transaction");
            return true;
        }
        return false;
    }
    private boolean checkResponsePayload(String resp,Map<String,Object> params){
        String systemId = (String) params.get(OnAccess.SYSTEM_ID);
        String pendingTransactionId = (String)params.get("transactionId");
        //String serviceTypeId = (String)params.get(OnAccess.TYPE_ID);
        JsonObject receipt = jsonParser.parse(resp).getAsJsonObject();
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
        Transaction transaction = new Transaction();
        transaction.index(pendingTransactionId);
        transaction.owner(systemId);
        transaction.originalPayload = resp;
        this.dataStore.create(transaction);
        //this.metricsListener.onUpdated(VendorMetrics.APPLE_STORE_COUNT,1);
        return validated;
    }
    private JsonObject toRequestPayload(String serviceTypeId,String receipt){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("receipt-data",receipt);
        jsonObject.addProperty("password",secureKey);
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
