package com.tarantula.test.cluster;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonToken;
import com.tarantula.Countable;
import com.tarantula.platform.service.deployment.DynamicModuleClassLoader;

import java.io.FileReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarFile;

public class JSONTester {
    public static void main(String[] args) throws Exception{
        long dur = 360;
        long h = (dur/3600000);
        long hr = dur%3600000;
        System.out.println("hr->"+hr);
        long m = hr/60000;
        long mr = hr%60000;
        System.out.println("mr->"+mr);
        long s = mr/1000;
        long sr = mr%1000;
        System.out.println("h->"+h);
        System.out.println("m->"+m);
        System.out.println("s->"+s);
        System.out.println("sr->"+sr);
        JarURLConnection jul = (JarURLConnection) new URL("jar:file:///development/boost/target/tarantula-boost-1.1.jar!/").openConnection();
        JarFile jar = jul.getJarFile();
        jar.stream().forEach((je)->{
            if(je.getName().endsWith(".class")){
                String jn = je.getName();
                int last = jn.lastIndexOf(".");
                System.out.println(jn.substring(0,last).replaceAll("/","\\."));
            }
        });
        jar.close();
        //Thread.sleep(100000);
        //jul.disconnect();
        //DynamicModuleClassLoader classLoader = new DynamicModuleClassLoader("/development/tem1/target/tarantula-platform-2.0.jar");
        //DynamicModuleClassLoader classLoader1 = new DynamicModuleClassLoader("/development/tem1/target/tarantula-platform-2.0.jar");
        //Countable ref = (Countable) classLoader.loadClass("com.tarantula.platform.Tmd",true).getConstructor().newInstance();
        //Countable ref1 = (Countable)classLoader.loadClass("com.tarantula.platform.Tmd",true).getConstructor().newInstance();
        //System.out.println(ref.count(10));
        //System.out.println(ref1.count(100));
        //System.out.println(ref.toString()+"/"+ref1.toString()+"/"+ref.equals(ref1));
    }
    public static void cmain(String[] args) throws Exception{
        FileReader jr = new FileReader(Thread.currentThread().getContextClassLoader().getResource("test.json").getFile());
        //JsonParser jp = new JsonParser();
        long st = System.currentTimeMillis();
        //JsonElement j = jp.parse(jr);
        //.getAsJsonObject().getAsJsonObject("connection");
        //System.out.println(j.getAsJsonObject().get("connection").getAsJsonObject().get("host"));

        JsonFactory jf = new JsonFactory();
        com.fasterxml.jackson.core.JsonParser xjp = jf.createParser(jr);
        //StringBuffer sb = new StringBuffer();
        while (!xjp.isClosed()){
            JsonToken jsonToken = xjp.nextToken();
            if(JsonToken.FIELD_NAME.equals(jsonToken)){
                String fieldName = xjp.getCurrentName();
                //System.out.println(fieldName+">>");
                //System.out.println();
                if(fieldName.equals("connection")){
                    JsonToken _xt = xjp.nextToken();
                    StringBuffer sb = new StringBuffer();
                    while (!_xt.equals(JsonToken.END_OBJECT)){
                        if(_xt.equals(JsonToken.FIELD_NAME)){
                            sb.append(xjp.getCurrentName());
                        }
                        _xt = xjp.nextToken();
                        if(_xt.equals(JsonToken.VALUE_STRING)){
                            sb.append(xjp.getText());
                        }
                    }
                    System.out.println(sb.toString());
                    break;
                }
            }

        }
        //System.out.println(sb);
        long ed = System.currentTimeMillis();
        System.out.println("dur->"+(ed-st));//50 ms
    }
}
