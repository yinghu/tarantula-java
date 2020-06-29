package com.tarantula.platform.util;

public class SampleLoad {
    private final String host;
    private final String prefix;
    private final int size;
    private HttpCaller httpCaller;
    public SampleLoad(String host,String prefix,int size){
        this.host = host;
        this.prefix = prefix;
        this.size = size;
    }
    public void _init() throws Exception{
        httpCaller = new HttpCaller(host);
        httpCaller._init();
    }
    public void batch(){
        for(int i=0;i<size;i++){
            try{
                //register
                System.out.println(httpCaller.get("user/action","index/user","onIndex"));

            }catch (Exception ex){
                //ignore
                ex.printStackTrace();
            }
        }
    }
    public static void main(String[] args) throws Exception{
        SampleLoad sampleLoad = new SampleLoad("http://10.0.0.6:8090","",1);
        sampleLoad._init();
        sampleLoad.batch();
    }
}

