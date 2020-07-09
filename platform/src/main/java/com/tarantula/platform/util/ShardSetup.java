package com.tarantula.platform.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class ShardSetup {



    public static void main(String[] args) throws Exception{


        Connection connection = DriverManager.getConnection("jdbc:mysql://10.0.0.153:3306?user=tarantula&password=tarantula");
        Statement cmd = connection.createStatement();
        cmd.execute("CREATE DATABASE IF NOT EXISTS integration10");
        //cmd.execute("CREATE DATABASE IF NOT EXISTS integration1");
        //cmd.execute("CREATE DATABASE IF NOT EXISTS integration");
        cmd.execute("CREATE DATABASE IF NOT EXISTS tarantula10");
        cmd.execute("USE integration10");
        cmd.execute("CREATE TABLE IF NOT EXISTS meta_info (data_store_name VARCHAR(25) PRIMARY KEY,node VARCHAR(15),bucket VARCHAR(15),scope INT,version INT)");
        //cmd.execute("USE integration1");
        //cmd.execute("CREATE TABLE IF NOT EXISTS meta_info (data_store_name VARCHAR(25) PRIMARY KEY,node VARCHAR(15),bucket VARCHAR(15),scope INT,version INT)");
        //cmd.execute("USE integration2");
        //cmd.execute("CREATE TABLE IF NOT EXISTS meta_info (data_store_name VARCHAR(25) PRIMARY KEY,node VARCHAR(15),bucket VARCHAR(15),scope INT,version INT)");
        cmd.execute("USE tarantula10");
        cmd.execute("CREATE TABLE IF NOT EXISTS meta_info (data_store_name VARCHAR(25) PRIMARY KEY,node VARCHAR(15),bucket VARCHAR(15),scope INT,version INT)");

        cmd.close();
        connection.close();
    }
}
