package com.tarantula.platform.configuration;

import com.icodesoftware.Statistics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

public class StatisticsTask implements JDBCTask<Statistics>{

    private final JDBCPool pool;
    public StatisticsTask(JDBCPool jdbcPool){
        this.pool = jdbcPool;
    }
    @Override
    public boolean execute(Statistics content) {
        try(Connection connection = pool.connection();
            PreparedStatement cmd = connection.prepareStatement("INSERT INTO "+content.label()+" (category,value,node,updated) VALUES(?,?,?,?) ON CONFLICT(category,node) DO UPDATE SET value = ?, updated = ?")) {
            for(Statistics.Entry entry : content.summary()){
                cmd.setString(1,entry.name());
                cmd.setDouble(2,entry.total());
                cmd.setString(3, pool.node());
                cmd.setTimestamp(4,new Timestamp(entry.timestamp()));
                cmd.setDouble(5,entry.total());
                cmd.setTimestamp(6,new Timestamp(entry.timestamp()));
                cmd.executeUpdate();
                cmd.clearParameters();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return true;
    }
}
