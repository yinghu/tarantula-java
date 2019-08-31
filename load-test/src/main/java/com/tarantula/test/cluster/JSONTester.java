package com.tarantula.test.cluster;

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
}
