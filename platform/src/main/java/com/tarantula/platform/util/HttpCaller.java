package com.tarantula.platform.util;

import com.tarantula.Session;

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

public class HttpCaller {

    private HttpClient client;
    private String host;
    public HttpCaller(String host){
        this.host = host;
    }
    public void _init() throws Exception{
        SSLContext sct = SSLContext.getInstance("TLS");
        sct.init(null,new TrustManager[]{new _X509TrustManager()},null);
        client = HttpClient.newBuilder().sslContext(sct).build();
        /**
        request = HttpRequest.newBuilder()
                .uri(URI.create("http://10.0.0.234:8090/admin"))
                .timeout(Duration.ofSeconds(5))
                .header("Accept", "application/json")
                .header(Session.TARANTULA_ACTION,"onAdmin")
                .header(Session.TARANTULA_ACCESS_KEY,"accessKey")
                .header(Session.TARANTULA_NAME,"root")
                .header(Session.TARANTULA_PASSWORD,"root")
                .GET()
                .build();**/

    }
    public String get(String path,String tag,String command) throws Exception{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(host+"/"+path))
                .timeout(Duration.ofSeconds(5))
                .header("Accept", "application/json")
                .header(Session.TARANTULA_TAG,tag)
                .header(Session.TARANTULA_ACTION,command)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return (response.body());
    }

    private class _X509TrustManager implements X509TrustManager{
        private X509Certificate[] certificate;
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            //run on server
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            //run on client to check if certificate is valid
            if(!chain[0].getSubjectDN().getName().equals("CN=gameclustering.com")){
                throw new CertificateException("Invalid certificate");
            }
            certificate = chain;
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return this.certificate;
        }

    }
}
