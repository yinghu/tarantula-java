package com.tarantula.platform.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class ShardSetup {


    public static void createShard(String shard) throws Exception{
        Connection connection = DriverManager.getConnection("jdbc:mysql://10.0.0.153:3306?user=tarantula&password=tarantula");
        Statement cmd = connection.createStatement();
        cmd.execute("CREATE DATABASE IF NOT EXISTS " + shard);
        //cmd.execute("USE " + shard);
        //cmd.execute("CREATE TABLE IF NOT EXISTS meta_info (p INT NOT NULL PRIMARY KEY,n VARCHAR(15),v INT)");
        cmd.close();
        //PreparedStatement pstm = connection.prepareStatement("INSERT INTO meta_info VALUES(?,?,?)");
        //for(int i=0;i<partitions;i++){
            //pstm.setInt(1,i);
            //pstm.setString(2,"pending");
            //pstm.setInt(3,0);
            //pstm.execute();
            //pstm.clearParameters();
        //}
        //pstm.close();
        connection.close();
    }

    public static void main(String[] args) throws Exception{
        createShard("integration_data_tx");
        createShard("tarantula_data_tx");
    }
}
