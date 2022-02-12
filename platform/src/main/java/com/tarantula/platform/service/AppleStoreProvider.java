package com.tarantula.platform.service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.http.HttpClient;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

public class AppleStoreProvider extends AuthObject{


    private HttpClient client;

    public AppleStoreProvider(String clientId, String secureKey, String authUri, String tokenUri, String certUri, String[] origins) {
        super("applestore", clientId, secureKey, authUri, tokenUri, certUri, origins);
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
    public boolean validate(Map<String,Object> params){
        try{

            metricsListener.onUpdated(Metrics.APPLE_STORE_COUNT,1);
            return true;
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
