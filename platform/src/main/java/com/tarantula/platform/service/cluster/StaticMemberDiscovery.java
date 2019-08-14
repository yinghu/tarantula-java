package com.tarantula.platform.service.cluster;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yinghu lu on 8/10/2018.
 */
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
            File f = new File("/etc/tarantula/host.list."+scope);
            if(f.exists()){
                reader = new BufferedReader(new FileReader(f));
            }
            else{
                reader = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("host.list."+scope)));
            }
            String line;
            do{
                line = reader.readLine();
                if(line!=null){
                    alist.add(InetAddress.getByName(line.trim()));
                }
            }while (line!=null);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return alist;
    }
}
