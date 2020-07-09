package com.tarantula.platform.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class ShardSetup {


    public static void createShard(String[] shardList) throws Exception{
        Connection connection = DriverManager.getConnection("jdbc:mysql://10.0.0.153:3306?user=tarantula&password=tarantula");
        Statement cmd = connection.createStatement();
        for(String name : shardList) {
            cmd.execute("CREATE DATABASE IF NOT EXISTS " + name);
            cmd.execute("USE " + name);
            cmd.execute("CREATE TABLE IF NOT EXISTS meta_info (data_store_name VARCHAR(25) PRIMARY KEY,node VARCHAR(15),bucket VARCHAR(15),scope INT,version INT)");
        }
        cmd.close();
        connection.close();
    }

    public static void main(String[] args) throws Exception{
        createShard(new String[]{"integration300","tarantula300"});
    }
}
