package com.tarantula.cci;

import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.*;
import com.tarantula.platform.event.ResponsiveEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class DevelopmentEventHandler implements RequestHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(DevelopmentEventHandler.class);
    private TokenValidatorProvider tokenValidatorProvider;
    private String homeDir;

    public String name(){
        return "/development";
    }

    public void onRequest(OnExchange exchange){
        try{
            String accessKey = exchange.header(Session.TARANTULA_ACCESS_KEY);
            String typeId = this.tokenValidatorProvider.validateAccessKey(accessKey);
            if(typeId==null){
                throw new RuntimeException("Illegal access");
            }
            String _file = exchange.path().replaceFirst("/development","");
            log.warn("Downloading from developer->"+typeId);
            InputStream inputStream = new FileInputStream(new File(homeDir+_file));
            byte[] _payload = inputStream.readAllBytes();
            inputStream.close();
            ResponsiveEvent responsiveEvent = new ResponsiveEvent("","",_payload,"start",true);
            exchange.onEvent(responsiveEvent);

        }catch (Exception ex){
            ex.printStackTrace();
            exchange.onError(ex,ex.getMessage());
        }
    }

    @Override
    public void start() throws Exception {
        String _homeDir = System.getProperty("user.home");
        int wc = _homeDir.indexOf(":");
        if(wc>0){
            homeDir = _homeDir.substring(wc+1).replace('\\','/')+"/.m2/repository";
        }
        else{
            homeDir = _homeDir+"/.m2/repository";
        }
        log.info("Development event handler started->"+homeDir);
    }

    @Override
    public void shutdown() throws Exception {

    }

    public void setup(ServiceContext tcx){
        this.tokenValidatorProvider = (TokenValidatorProvider) tcx.serviceProvider(TokenValidatorProvider.NAME);
    }
    public  boolean onEvent(Event event){

        return true;
    }
    public void onCheck(){
        //log.warn("Total active session ["+_hex.size()+"] on ["+name()+"]");
    }
}
