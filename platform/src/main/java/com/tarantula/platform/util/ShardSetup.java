package com.tarantula.platform.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Map;

public class ShardSetup {

    //create the database
    public static void createShard(String shard, Map<String,String> config) throws Exception{
        String url = config.get("url");
        String user = config.get("user");
        String password = config.get("password");
        Connection connection = DriverManager.getConnection(url+"?user="+user+"&password="+password);
        Statement cmd = connection.createStatement();
        cmd.execute("CREATE DATABASE IF NOT EXISTS " + shard);
        cmd.close();
        connection.close();
    }
}
