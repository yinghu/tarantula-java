package com.tarantula.platform.configuration;

import com.icodesoftware.Statistics;

import java.sql.Connection;
import java.sql.Statement;

public class StatisticsTask implements JDBCTask<Statistics>{

    private final JDBCPool pool;
    public StatisticsTask(JDBCPool jdbcPool){
        this.pool = jdbcPool;
    }
    @Override
    public boolean execute(Statistics content) {
        try(Connection connection = pool.connection();Statement cmd = connection.createStatement()) {
            System.out.println(" : "+connection.isClosed()+" : "+content.label());
            cmd.execute("CREATE TABLE IF NOT EXISTS "+content.label()+" (cat varchar(50) NOT NULL,val DOUBLE PRECISION, updated TIMESTAMP NOT NULL, PRIMARY KEY(cat))");
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return true;
    }
}
