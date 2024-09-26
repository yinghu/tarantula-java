package com.tarantula.licensing;


import com.icodesoftware.logging.JDKLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Validator {

    private static final JDKLogger log = JDKLogger.getLogger(Validator.class);
	
    public static boolean validate(){
        try{
            File sf = new File("LICENSE");
            if(sf.exists()){
                log.warn("License file is included");
                return true;
            }
            else{
                throw new IllegalArgumentException("License not found");
            }
        }catch (Exception ex){
            log.error("error",ex);
            return false;
        }
    }

    public static String version(){
        try{
            File sf = new File("version.txt");
            if(!sf.exists()) throw new IllegalArgumentException("no version file found");
            BufferedReader reader = new BufferedReader(new FileReader(sf));
            String version = reader.readLine();
            log.info("Release version ["+version+"]");
            return version;
        }catch (Exception ex){
            log.error("error",ex);
            throw new RuntimeException(ex);
        }
    }
}
