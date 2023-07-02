package com.tarantula.platform.service;

import com.icodesoftware.OnAccess;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.HttpCaller;
import com.tarantula.platform.service.metrics.GameClusterMetrics;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

public class GameCenterAuthProvider extends AuthObject{



    public GameCenterAuthProvider(String typeId, MetricsListener metricsListener){
        super(typeId,"");
        this.applicationMetricsListener = metricsListener;
    }

    @Override
    public String name(){
        return OnAccess.GAME_CENTER;
    }

    @Override
    public void setup(ServiceContext serviceContext){
        super.setup(serviceContext);
        logger = serviceContext.logger(GameCenterAuthProvider.class);
    }
    @Override
    public boolean validate(Map<String,Object> params){
        try{
            boolean verified = verifySignature(params);
            onMetrics(GameClusterMetrics.ACCESS_GAME_CENTER_LOGIN_COUNT);
            return verified;
        }catch (Exception ex){
            logger.error("game center error ["+typeId+"]",ex);
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
        HttpCaller.ResponseData responseData = new HttpCaller.ResponseData();
        int code = serviceContext.httpClientProvider().request(client->{
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            responseData.dataAsBytes = response.body();
            return response.statusCode();
        });
        if(code!=200) return false;
        byte[] _signature_bytes = Base64.getDecoder().decode(_signature);
        byte[] _hash = Base64.getDecoder().decode(_token);
        CertificateFactory cf =   CertificateFactory.getInstance("X509");
        X509Certificate c = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(responseData.dataAsBytes));
        PublicKey key = c.getPublicKey();
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(key);
        signature.update(_hash);
        return signature.verify(_signature_bytes);
    }
}
