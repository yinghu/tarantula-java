package com.tarantula.test.datastore;

import com.sleepycat.je.*;
import com.tarantula.Access;
import com.tarantula.DataStore;
import com.tarantula.platform.presence.AccessTrack;
import com.tarantula.platform.service.persistence.ReplicatedDataStore;
import com.tarantula.platform.service.persistence.berkeley.BerkeleyJEProvider;
import com.tarantula.platform.util.SystemUtil;

import java.io.File;
import java.util.UUID;

/**
 * Created by yinghu lu on 4/6/2019.
 */
public class BerkeleyDBTest {
    //static Database database;
    static int count;

    public static void main(String[] args) throws Exception {
        BerkeleyJEProvider je = new BerkeleyJEProvider();
        je.configure(DataStoreConfig._cfg);
        je.start();
        ReplicatedDataStore ds = (ReplicatedDataStore) je.create("test",7);
        //System.out.println(ds.pause());
        new Thread(()->{
            //try{Thread.sleep(1000);ds.restart();}catch (Exception ex){}
        }).start();
        ds.traverse((d,p,k,v)->{
            System.out.println(new String(k));
            return true;
        });
        Access acc = new AccessTrack();
        acc.bucket(ds.bucket());
        acc.oid(SystemUtil.oid());
        acc.login("login5");
        acc.password("password");
        acc.active(true);
        acc.onEdge(true);
        acc.owner("loop");
        acc.label("Access");
        System.out.println(ds.create(acc));
        System.out.println(ds.createIfAbsent(acc,false));
        acc.login("");
        System.out.println(ds.load(acc));
        System.out.println(acc.login());
        AccessQuery accessQuery = new AccessQuery("loop");
        ds.list(accessQuery,(t)->{
            System.out.println(t.distributionKey()+"/"+t.login());
            return true;
        });
        je.shutdown();
    }

    public static void mainm(String[] args) throws Exception {
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
        Environment env = new Environment(new File("/backup"), envConfig);
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        dbConfig.setDeferredWrite(true);

        Database d0 = env.openDatabase(null, "tarantula100",dbConfig);
        DiskOrderedCursor cursor = d0.openCursor(null);
        DatabaseEntry pk = new DatabaseEntry();
        DatabaseEntry pv = new DatabaseEntry();
        int ct = 0;
        do{
            OperationStatus os = cursor.getNext(pk,pv,null);
            if(os==OperationStatus.SUCCESS){
                //System.out.println(new String(pk.getData()));
                ct++;
            }
            else{
                break;
            }
        }while (true);
        cursor.close();
        System.out.println(d0.count()+"////"+ct);
        Database d1 = env.openDatabase(null,"ta11",dbConfig);
        System.out.println(d1.count());
        long st = System.currentTimeMillis();
        for(int i=0;i<100;i++){
            byte[] k = UUID.randomUUID().toString().getBytes("UTF-8");
            byte[] v = "1234".getBytes("UTF-8");
            set(d0,k,v);
            get(d0,k);
            set(d1,k,v);
            get(d1,k);
        }
        System.out.println(count+"//DT->"+((System.currentTimeMillis()-st)/1000));
        //set(k,v);
        //get(k);
        //System.out.println(new String(get(k)));
        d1.close();
        d0.close();
    }
    static void set(Database database,byte[] key,byte[] value){
        DatabaseEntry ke = new DatabaseEntry(key);
        DatabaseEntry ve = new DatabaseEntry(value);
        OperationStatus status = database.put(null,ke,ve);
        if(status==OperationStatus.SUCCESS){
            count++;
        }
        //System.out.println(status);
    }
    static byte[] get(Database database,byte[] key){
        DatabaseEntry ke = new DatabaseEntry(key);
        DatabaseEntry ve = new DatabaseEntry();
        OperationStatus status = database.get(null,ke,ve,null);
        //System.out.println(status);
        if(status==OperationStatus.SUCCESS){
            count++;
            return ve.getData();
        }
        else{
            return null;
        }
    }
}
