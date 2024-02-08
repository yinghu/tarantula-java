package com.tarantula.platform.bootstrap;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Batchable;
import com.icodesoftware.util.HttpCaller;
import com.icodesoftware.util.JsonUtil;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class DataBootstrap {

    public static void main(String[] args) throws Exception{
        run("dev/root","root","http://localhost:8090");
    }

    private static final int BATCH_SIZE = 1_000_000; //1mb
    public static void run(String user,String password,String host) throws Exception{

        HttpCaller httpCaller = new HttpCaller(host);
        httpCaller._init();
        String[] headers = new String[]{
                Session.TARANTULA_TAG,"index/user",
                Session.TARANTULA_MAGIC_KEY,user,
                Session.TARANTULA_ACTION,"onLogin"
        };
        JsonObject payload = new JsonObject();
        payload.addProperty("login",user);
        payload.addProperty("password",password);
        JsonObject token = JsonUtil.parse(httpCaller.post("user/action",payload.toString().getBytes(),headers));

        if(!token.get("Successful").getAsBoolean()) throw new RuntimeException(token.toString());


        DataBatch dataBatch = doBackup(httpCaller,token);
        for(int i=0;i<dataBatch.batch;i++) {
            doBatchDownload(httpCaller, host, token, dataBatch.fileName, i,BATCH_SIZE);
        }
        doBatchDownload(httpCaller,host,token,dataBatch.fileName, dataBatch.batch+1,dataBatch.remaining);
    }
    private static DataBatch doBackup(HttpCaller httpCaller,JsonObject token) throws Exception{
        String[] headers = new String[]{
                Session.TARANTULA_TOKEN,token.get("Token").getAsString(),
                Session.TARANTULA_ACTION,"onDataBackup"
        };
        JsonObject json = JsonUtil.parse(httpCaller.get("development",headers));
        String file = json.get("file").getAsString();
        int size = json.get("size").getAsInt();
        System.out.println("TOTAL : "+size);
        int offset = 1;
        do{
            size = size-BATCH_SIZE;
            if(size>BATCH_SIZE){
                offset++;
            }
        }while(size>BATCH_SIZE);
        return new DataBatch(file,offset,size);
    }
    private static void doBatchDownload(HttpCaller httpCaller,String host,JsonObject token,String fileName,int offset,int size) throws Exception{
        String[] headers = new String[]{
                Session.TARANTULA_TOKEN,token.get("Token").getAsString(),
                Session.TARANTULA_ACTION,"onDataBootstrap",
                Session.TARANTULA_NAME,fileName+"#"+offset+"#"+size
        };
        HttpRequest _request = HttpRequest.newBuilder()
                .uri(URI.create(host+"/development"))
                .timeout(Duration.ofSeconds(HttpCaller.TIME_OUT*10))
                .headers(headers)
                .GET()
                .build();
        //BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("./data."+offset+".mdb"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int code = httpCaller.request(client->{
            HttpResponse<InputStream> _response = client.send(_request, HttpResponse.BodyHandlers.ofInputStream());
            InputStream input = _response.body();
            int b;
            do {
                b = input.read();
                if(b!=-1) out.write(b);
            }while (b!=-1);
            out.close();
            return _response.statusCode();
        });
        if(code!=200) throw new RuntimeException("failed to load initial data from ["+host+"]");
        System.out.println("Read ["+offset+" : "+size+"]");
        
    }
}
