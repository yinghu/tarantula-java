package com.tarantula.platform.service.cluster;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;


public class StaticMemberDiscovery implements ScopedMemberDiscovery {

    private int scope;
    public void scope(int scope){
        this.scope = scope;
    }

    @Override
    public List<InetAddress> find()  throws Exception{
        ArrayList<InetAddress> alist = new ArrayList<>();
        try{
            BufferedReader reader;
            File f = new File("/etc/tarantula/host.list");
            if(f.exists()){
                reader = new BufferedReader(new FileReader(f));
            }
            else{
                reader = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("host.list")));
            }
            String line;
            do{
                line = reader.readLine();
                if(line!=null){
                    alist.add(InetAddress.getByName(line.trim()));
                }
            }while (line!=null);
        }catch (Exception ex){
            throw new RuntimeException("not found host file in ["+scope+"]");
        }
        return alist;
    }
}
