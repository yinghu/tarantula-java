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

    public static void main(String[] args) throws Exception{
        //HttpCaller caller = new HttpCaller();
        //caller._init();
        //System.out.println(caller.get());
        //System.out.println(caller.get());
        //System.out.println(caller.get());
        //System.out.println(caller.get());
        double xp = 5000;
        int level = (int)xp/1000+1;
        System.out.println("LEVEL->"+level);
    }
    private HttpClient client;
    private HttpRequest request;
    public HttpCaller(){

    }
    public void _init() throws Exception{
        SSLContext sct = SSLContext.getInstance("TLS");
        sct.init(null,new TrustManager[]{new _X509TrustManager()},null);
        client = HttpClient.newBuilder().sslContext(sct).build();
        request = HttpRequest.newBuilder()
                .uri(URI.create("http://10.0.0.234:8090/admin"))
                .timeout(Duration.ofSeconds(5))
                .header("Accept", "application/json")
                .header(Session.TARANTULA_ACTION,"onAdmin")
                .header(Session.TARANTULA_ACCESS_KEY,"accessKey")
                .header(Session.TARANTULA_NAME,"root")
                .header(Session.TARANTULA_PASSWORD,"root")
                .GET()
                .build();
    }
    public String get() throws Exception{
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
            if(!chain[0].getSubjectDN().getName().equals("CN=gameenginecluster.com")){
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
