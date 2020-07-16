package com.tarantula.platform.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class ShardSetup {


    public static void createShard(String shard) throws Exception{
        Connection connection = DriverManager.getConnection("jdbc:mysql://10.0.0.153:3306?user=tarantula&password=tarantula");
        Statement cmd = connection.createStatement();
        cmd.execute("CREATE DATABASE IF NOT EXISTS " + shard);
        cmd.close();
        connection.close();
    }
}
