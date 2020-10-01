package com.tarantula.licensing;

import com.icodesoftware.service.TokenValidatorProvider;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.util.SystemUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.security.MessageDigest;

public class Validator {

    private static final JDKLogger log = JDKLogger.getLogger(Validator.class);
	
    public static boolean validate(){
        try{
            File sf = new File("license.txt");
            if(sf.exists()){
                BufferedReader reader = new BufferedReader(new FileReader(sf));
                String key = reader.readLine();
                log.info("Validating tarantula license key ["+key+"]");
                return key.equals(SystemUtil.hashPassword(MessageDigest.getInstance(TokenValidatorProvider.MDA),"tarantula"));
            }
            else{
                throw new IllegalArgumentException("License key not found");
            }
        }catch (Exception ex){
            log.error("error",ex);
            return false;
        }
    }
}
