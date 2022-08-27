package com.tarantula.platform.service;

import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TokenValidatorProvider;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

public class GameCenterAuthProvider extends AuthObject implements AuthVendorRegistry{

    private HttpClient client;

    public GameCenterAuthProvider(){
        super("gameCenter","","","","","",new String[0]);
        try{
            SSLContext sct = SSLContext.getInstance("TLS");
            sct.init(null,new TrustManager[]{new GameCenterAuthProvider._X509TrustManager()},null);
            client = HttpClient.newBuilder().sslContext(sct).build();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    @Override
    public void setup(ServiceContext serviceContext){
        super.setup(serviceContext);

    }
    @Override
    public boolean validate(Map<String,Object> params){
        try{
            boolean verified = verifySignature(params);
            //metricsListener.onUpdated(VendorMetrics.GAME_CENTER,1);
            return verified;
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
    private boolean verifySignature(Map<String,Object> params) throws Exception{
        String _url = (String)params.get("publicKeyUrl");
        String _signature = (String)params.get("signature");
        String _token = (String)params.get("token");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .timeout(Duration.ofSeconds(TIMEOUT))
                .GET()
                .build();
        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if(response.statusCode()!=200) return false;
        byte[] _signature_bytes = Base64.getDecoder().decode(_signature);
        byte[] _hash = Base64.getDecoder().decode(_token);
        CertificateFactory cf =   CertificateFactory.getInstance("X509");
        X509Certificate c = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(response.body()));
        PublicKey key = c.getPublicKey();
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(key);
        signature.update(_hash);
        return signature.verify(_signature_bytes);
    }

    @Override
    public void registerAuthVendor(TokenValidatorProvider.AuthVendor authVendor) {

    }

    @Override
    public void releaseAuthVendor(TokenValidatorProvider.AuthVendor authVendor) {

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
