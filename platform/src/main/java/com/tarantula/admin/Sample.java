package com.tarantula.admin;

import com.icodesoftware.util.HttpCaller;

public class Sample {
    public static void main(String[] args) throws Exception {
        HttpCaller httpCaller = new HttpCaller("http://192.168.1.7:8090");
        httpCaller._init();

        for(int i=0;i<1000;i++){
            long st = System.currentTimeMillis();
            String key = "key"+i;
            String value = "value"+i;
            httpCaller.get("tarantula/presence/set/test/"+key+"/"+value,new String[]{"a","b"});
            httpCaller.get("tarantula/presence/get/test/"+key,new String[]{"a","b"});
            System.out.println("Duration: "+(System.currentTimeMillis()-st)+" ms");
        }
    }
}
