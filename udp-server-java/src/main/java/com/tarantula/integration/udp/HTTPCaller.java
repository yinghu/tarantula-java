package com.tarantula.integration.udp;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

    static String TARANTULA_APPLICATION_ID ="Tarantula-application-id";
    static String TARANTULA_INSTANCE_ID ="Tarantula-instance-id";
    static String TARANTULA_VIEW_ID ="View-id";
    static String TARANTULA_TOKEN ="Tarantula-token";
    static String TARANTULA_PAYLOAD_SIZE ="Tarantula-payload-size";
    static String TARANTULA_ACTION ="Tarantula-action";
    static String TARANTULA_MAGIC_KEY ="Tarantula-magic-key"; //the routing key
    static String TARANTULA_TAG ="Tarantula-tag";
    static String TARANTULA_PAYLOAD = "Tarantula-payload";


    private String prefix;
    private boolean secured;
    private JsonParser jsonParser;
    private JsonElement jsonElement;
    public HTTPCaller(boolean secured, String host) {
        this.secured = secured;
        this.prefix = host;
        this.jsonParser = new JsonParser();
    }
    public void doAction(String path, String command, HashMap<String,String> headers, byte[] payload,OnResponse onResponse){
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
                headers.put(TARANTULA_PAYLOAD_SIZE,payload.length+"");
            }
            if(headers!=null){
                headers.put(TARANTULA_ACTION,command);
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
    interface OnResponse{
        void on(JsonObject jsonObject);
    }
}
