package com.icodesoftware.util;

import com.icodesoftware.Connection;
import com.icodesoftware.Session;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;

public class HttpCaller {

    private static int TIME_OUT = 5;
    private static String ACCEPT = "Accept";
    private static String ACCEPT_JSON = "application/json";
    private static String CONTENT_TYPE = "Content-type";
    private static String CONTENT_FORM = "application/x-www-form-urlencoded";
    private HttpClient client;
    private String host;


    public HttpCaller(String host){
        this.host = host;
    }
    public void _init() throws Exception{
        SSLContext sct = SSLContext.getInstance("TLS");
        sct.init(null,new TrustManager[]{new _X509TrustManager()},null);
        client = HttpClient.newBuilder().sslContext(sct).build();
    }
    public String index() throws Exception{
        String[] headers = new String[]{
                Session.TARANTULA_TAG,"index/user",Session.TARANTULA_ACTION,"onIndex"
        };
        return get("user/action",headers);
    }
    public String get(String path,String[] headers) throws Exception{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(host+"/"+path))
                .timeout(Duration.ofSeconds(TIME_OUT))
                .header(ACCEPT, ACCEPT_JSON)
                .headers(headers)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return (response.body());
    }
    public String post(String path,byte[] payload, String[] headers) throws Exception{
        HttpRequest request = HttpRequest.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .uri(URI.create(host+"/"+path))
                .timeout(Duration.ofSeconds(TIME_OUT))
                .header(ACCEPT, ACCEPT_JSON)
                .header(CONTENT_TYPE, CONTENT_FORM)
                .header(Session.TARANTULA_PAYLOAD_SIZE,payload.length+"")
                .headers(headers)
                .POST(HttpRequest.BodyPublishers.ofByteArray(payload))
                .build();
        HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
    public WebSocket connect(Connection serverConnection,WebSocket.Listener listener) throws Exception{
        //String ticket = tokenValidatorProvider.tokenValidator().ticket(serverLogin.distributionKey(),presence.count(0));
        String protocol = serverConnection.secured() ? "wss://" : "ws://";
        //StringBuffer query = new StringBuffer();
        //query.append("connectionId="+serverConnection.connectionId());
        //query.append("&accessKey="+ticket);
        //query.append("&stub="+presence.count(0));
        //query.append("&systemId="+serverLogin.owner());
        URI uri = new URI(protocol + serverConnection.host() + ":" + serverConnection.port()+"/"+serverConnection.path()+"?"+ URLEncoder.encode(serverConnection.index(),"UTF-8"));
        return client.newWebSocketBuilder().header("origin",serverConnection.host()).subprotocols("tarantula-service").buildAsync(uri, listener).join();
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
