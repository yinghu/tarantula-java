package com.tarantula.cci.http;

import com.sun.net.httpserver.HttpExchange;
import com.tarantula.Session;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * updated by yinghu on 8/18/2019.
 */
public class RequestParser{

    public Map<String,Object> parse(HttpExchange httpExchange) throws IOException {
        HashMap<String,Object> requestMapping = new HashMap<>();
        int pz = 0;
        for(Map.Entry<String,List<String>> hv : httpExchange.getRequestHeaders().entrySet()){
            if(!hv.getValue().isEmpty()){
                requestMapping.put(hv.getKey(), hv.getValue().get(0));
                if(hv.getKey().equals(Session.TARANTULA_PAYLOAD_SIZE)){
                    pz = Integer.parseInt(hv.getValue().get(0));
                }
            }
        }

        if(httpExchange.getRequestMethod().equalsIgnoreCase("POST")){ //skip GET METHOD
            boolean onJson = requestMapping.get(Session.HTTP_CONTENT_TYPE).toString().startsWith("application/x-www-form-urlencoded");
            if(onJson){
                InputStream in = httpExchange.getRequestBody();
                byte[] buffer = new byte[pz>0?pz:in.available()];
                in.read(buffer);
                /**
                int b;
                do{
                    b = in.read();
                    if(b!=-1){
                        buffer.write(b);
                    }

                }while(b!=-1);**/
                requestMapping.put(Session.TARANTULA_PAYLOAD,buffer);
                //buffer.close();
            }
            else{
                BufferedReader bread = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody()));
                String line;
                byte[] buffer = new byte[0];
                boolean lined = false;
                do{
                    line = bread.readLine();
                    if(lined&&line!=null){
                        buffer = line.getBytes(Charset.forName("US-ASCII"));
                        lined = false;
                    }
                    else if(line!=null&&line.equals("")){
                        lined = true;
                    }
                }while(line!=null);
                requestMapping.put(Session.TARANTULA_PAYLOAD,buffer);
            }
        }
        else{
           requestMapping.put(Session.TARANTULA_PAYLOAD,new byte[0]);
        }
        return requestMapping;
    }
}
