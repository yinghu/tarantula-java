package com.tarantula.platform.bootstrap;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.util.HttpCaller;
import com.icodesoftware.util.JsonUtil;

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


        doBackup(httpCaller,token);
        //doBatchDownload(httpCaller,host,token);

    }
    private static void doBackup(HttpCaller httpCaller,JsonObject token) throws Exception{
        String[] headers = new String[]{
                Session.TARANTULA_TOKEN,token.get("Token").getAsString(),
                Session.TARANTULA_ACTION,"onDataBackup"
        };
        JsonObject json = JsonUtil.parse(httpCaller.get("development",headers));
        System.out.println(json.toString());
    }
    private static void doBatchDownload(HttpCaller httpCaller,String host,JsonObject token) throws Exception{
        String[] headers = new String[]{
                Session.TARANTULA_TOKEN,token.get("Token").getAsString(),
                Session.TARANTULA_ACTION,"onDataBootstrap"
        };
        HttpRequest _request = HttpRequest.newBuilder()
                .uri(URI.create(host+"/development"))
                .timeout(Duration.ofSeconds(HttpCaller.TIME_OUT*10))
                .headers(headers)
                .GET()
                .build();
        FileOutputStream out = new FileOutputStream("./data.mdb");
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
    }
}
