package com.tarantula.test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.tarantula.Session;
import com.tarantula.test.integration.OnPayload;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * Updated by yinghu lu on 8/31/2019.
 */
public class HTTPCaller {

    private String prefix;
    private boolean secured;
    private JsonParser jsonParser;
    private JsonElement jsonElement;
    public HTTPCaller(boolean secured,String host) {
        this.secured = secured;
        this.prefix = host;
        this.jsonParser = new JsonParser();
    }
    public void doAction(String path, String command, HashMap<String,String> headers, byte[] payload, OnPayload onResponse){
        this.requestOnJson(path,command,headers,payload);
        onResponse.on(jsonElement.getAsJsonObject());
    }
    private void requestOnJson(String path,String command,HashMap<String,String> headers,byte[] payload){
        HttpURLConnection http = null;
        try{
            URL h = new URL(secured?("https://"+prefix+"/"+path):("http://"+prefix+"/"+path));
            if(secured){
                HttpsURLConnection _https=(HttpsURLConnection)h.openConnection();
                _https.setRequestMethod(payload!=null?"POST":"GET");
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, null, new SecureRandom());
                _https.setSSLSocketFactory(context.getSocketFactory());
                http = _https;
            }
            else{
                http = (HttpURLConnection)h.openConnection();
                http.setRequestMethod(payload!=null?"POST":"GET");
            }
            if(payload!=null){
                headers.put(Session.TARANTULA_PAYLOAD_SIZE,payload.length+"");
            }
            if(headers!=null){
                headers.put(Session.TARANTULA_ACTION,command);
                for(Map.Entry<String,String> e:headers.entrySet()){
                    http.setRequestProperty(e.getKey(),e.getValue());
                }
            }
            if(payload!=null) {
                http.setDoOutput(true);
                http.getOutputStream().write(payload);
            }
            if(http.getResponseCode()==200){//blocked on response
                InputStream in = http.getInputStream();
                jsonElement = jsonParser.parse(new InputStreamReader(in));
                in.close();
            }else{
                throw new RuntimeException("failed on request");
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
            throw new RuntimeException("failed on ["+command+"]");
        }
        finally {
            if(http!=null){
                http.disconnect();
            }
        }
    }
}
