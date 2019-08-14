package com.tarantula.test.integration;

import com.tarantula.Session;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yinghu lu on 10/19/2017.
 */
public class HTTPCaller {

    private String prefix;
    private boolean secured;
    public HTTPCaller(boolean secured,String host) {
        this.secured = secured;
        this.prefix = host;
    }

    public String doAction(String command, String action, HashMap<String,String> headers, byte[] payload) throws Exception{
        //long st = System.currentTimeMillis();
        String ret = this.request(action,command,headers,payload);
        //System.out.println(action+"<-Duration->"+(System.currentTimeMillis()-st));
        return ret;
    }

    private String request(String path,String command,HashMap<String,String> headers,byte[] payload){
        StringBuffer buff = new StringBuffer();
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
                int ch;
                do{
                    ch = in.read();
                    if(ch!=-1){
                        buff.append((char)ch);
                    }
                }while(ch!=-1);
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
        return buff.toString();
    }
}
