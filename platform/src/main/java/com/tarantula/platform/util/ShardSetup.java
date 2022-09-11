package com.tarantula.platform.util;

import com.icodesoftware.LeaderBoard;
import com.icodesoftware.Property;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.service.metrics.AccessMetrics;
import com.tarantula.platform.service.metrics.MetricsHistory;
import com.tarantula.platform.service.metrics.MetricsProperty;
import com.tarantula.platform.service.metrics.MetricsSnapshot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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

    public static void main(String[] args){
        LocalDateTime _cur = LocalDateTime.now();
        MetricsSnapshot metricsSnapshot = new MetricsSnapshot(12,AccessMetrics.ACCOUNT_USER_CREATION_COUNT, LeaderBoard.HOURLY);
        LocalDateTime hf = _cur.minusMinutes(_cur.getMinute()).plusHours(1);
        for(int i=0;i<12;i++){
            LocalDateTime xhf = hf.plusHours(11-i);
            String xh = xhf.format(DateTimeFormatter.ofPattern("hh:mm a"));
            metricsSnapshot.initialize(new MetricsProperty(i,xh,0,xhf),_cur);
        }
        for(Property p : metricsSnapshot.metrics()){
            System.out.println(p.name()+">>>>>>"+TimeUtil.fromUTCMilliseconds(p.timestamp()));
        }
        metricsSnapshot.update(100);
        if(!metricsSnapshot.validate(_cur.plusHours(1))){
            System.out.println("Need to reset");

        }
        //MetricsHistory history = new MetricsHistory(24);
        //history.initializeHourly(_cur);
        //Property[] mts = history.metrics();
        //for(int i=0;i<24;i++){
            //System.out.println(TimeUtil.fromUTCMilliseconds(mts[i].timestamp()));
        //}
        //metricsSnapshot.initialize();
        //long hs = TimeUtil.durationUTCInHours(_cur.minusYears(1).minusDays(0).minusHours(0).minusMinutes(59),_cur);
        //System.out.println("Day passed->"+hs);
    }
}
