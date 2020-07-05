package com.tarantula.platform.util;

import com.google.gson.JsonObject;
import com.tarantula.Session;
import org.apache.commons.dbcp2.BasicDataSource;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class SampleLoad {
    private final String host;
    private final String prefix;
    private final int size;
    private HttpCaller httpCaller;
    private ExecutorService pool;

    public SampleLoad(String host,String prefix,int size){
        this.host = host;
        this.prefix = prefix!=null?prefix: UUID.randomUUID().toString();
        this.size = size;
    }
    public void _init() throws Exception{
        httpCaller = new HttpCaller(host);
        httpCaller._init();
        pool = Executors.newFixedThreadPool(100);
    }
    public void device() throws Exception{
        CountDownLatch batch = new CountDownLatch(size);
        AtomicLong timeRun = new AtomicLong(0);
        long start = System.currentTimeMillis();
        for(int i=0;i<size;i++){
            try{
                //register
                String _login = prefix+"-"+i;
                String[] headers = new String[]{
                        Session.TARANTULA_TAG,"index/user",
                        Session.TARANTULA_ACTION,"onDevice",
                        Session.TARANTULA_MAGIC_KEY,_login
                };
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("deviceId",_login);
                //jsonObject.addProperty("password","password");
                pool.execute(()->{
                    try{
                        long st = System.currentTimeMillis();
                        httpCaller.index();
                        httpCaller.post("user/action",jsonObject.toString().getBytes(),headers);
                        long ed = System.currentTimeMillis()-st;
                        timeRun.addAndGet(ed);
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                    finally {
                        batch.countDown();
                    }
                });
            }catch (Exception ex){
                //ignore
                ex.printStackTrace();
            }
        }
        batch.await();
        pool.shutdownNow();
        System.out.println("Average duration ["+(timeRun.get()/size)+"] with total time ["+((System.currentTimeMillis()-start)/1000)+"]");
    }
    public void register() throws Exception{
        CountDownLatch batch = new CountDownLatch(size);
        AtomicLong timeRun = new AtomicLong(0);
        long start = System.currentTimeMillis();
        for(int i=0;i<size;i++){
            try{
                //register
                String _login = prefix+"-"+i;
                String[] headers = new String[]{
                        Session.TARANTULA_TAG,"index/user",
                        Session.TARANTULA_ACTION,"onRegister",
                        Session.TARANTULA_MAGIC_KEY,_login
                };
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("login",_login);
                jsonObject.addProperty("password","password");
                pool.execute(()->{
                    try{
                        long st = System.currentTimeMillis();
                        httpCaller.index();
                        httpCaller.post("user/action",jsonObject.toString().getBytes(),headers);
                        long ed = System.currentTimeMillis()-st;
                        timeRun.addAndGet(ed);
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                    finally {
                        batch.countDown();
                    }
                });
            }catch (Exception ex){
                //ignore
                ex.printStackTrace();
            }
        }
        batch.await();
        pool.shutdownNow();
        System.out.println("Average duration ["+(timeRun.get()/size)+"]");
    }
    public static void main(String[] args) throws Exception{
        //SampleLoad sampleLoad = new SampleLoad("http://10.0.0.153:8090",null,5000);
        //sampleLoad._init();
        //sampleLoad.batch();
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://10.0.0.153:3306/");
        dataSource.setUsername("tarantula");
        dataSource.setPassword("tarantula");

        // Connection pooling properties
        dataSource.setInitialSize(0);
        dataSource.setMaxIdle(5);
        dataSource.setMaxTotal(5);
        dataSource.setMinIdle(0);
        Connection connection = dataSource.getConnection();
        Statement cmd = connection.createStatement();
        cmd.execute("CREATE DATABASE IF NOT EXISTS tarantula1");
        cmd.execute("USE tarantula1");
        cmd.execute("CREATE TABLE IF NOT EXISTS shard (partition_no INT NOT NULL,version INT,node VARCHAR(10),bucket VARCHAR(10),start_time VARCHAR(100),PRIMARY KEY(partition_no))");
        for(int i=0;i<127;i++){
            cmd.addBatch("INSERT INTO shard(partition_no) VALUES("+i+")");
            cmd.addBatch("CREATE TABLE IF NOT EXISTS partition_"+i+" (k VARCHAR(100) NOT NULL,v BLOB, PRIMARY KEY(k))");
        }
        cmd.executeBatch();
        //PreparedStatement cmd = connection.prepareStatement("update shard_version set version = version+1 where name=?");
        //cmd.execute("update shard_version set version = version+1 where name=",)
        //PreparedStatement cmd = connection.prepareStatement("select v from kvb where k =?");
        //cmd.setString(1,"key02");
        /**
        PreparedStatement cmd = connection.prepareStatement("insert into kvb(k,v) values(?,?)");
        JsonObject v = new JsonObject();
        v.addProperty("name","test");
        v.addProperty("age",15);
        cmd.setString(1,"key02");
        cmd.setBytes(2,v.toString().getBytes());
        **/
        //ResultSet rs = cmd.executeQuery();
        //if(rs.next()){
            //System.out.println(new String(rs.getBytes("v")));
        //}
        //cmd.close();
        //cmd.setString(1,"pd01");
        //cmd.execute();
        dataSource.close();
    }
}

