package com.tarantula.platform.util;

import com.icodesoftware.Distributable;
import com.icodesoftware.LeaderBoard;
import com.icodesoftware.Property;
import com.icodesoftware.Subscription;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.game.Arena;
import com.tarantula.platform.presence.Membership;
import com.tarantula.platform.presence.User;
import com.tarantula.platform.service.metrics.AccessMetrics;
import com.tarantula.platform.service.metrics.MetricsHistory;
import com.tarantula.platform.service.metrics.MetricsProperty;
import com.tarantula.platform.service.metrics.MetricsSnapshot;
import com.tarantula.platform.service.persistence.RecoverableMetadata;
import com.tarantula.platform.service.persistence.web.WebHookBackupProvider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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
        WebHookBackupProvider webHookBackupProvider = new WebHookBackupProvider();
        HashMap<String,Object> config = new HashMap<>();
        config.put("host","http://localhost:8090");
        config.put("accessKey","BDS/592690c1e54440e2a11cfc2bccb6adee-D3A35F6940405D3D05036321510929CF28BC437F-35D3AA273137E822F9A8CD1AB0831BDA");
        config.put("path","backup");
        webHookBackupProvider.configure(config);
        Metadata metadata = new RecoverableMetadata("tarantula_user",1, Distributable.DATA_SCOPE);
        Subscription subscription = new Membership();
        subscription.distributionKey("BDS/"+SystemUtil.oid());
        subscription.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        subscription.count(1);
        webHookBackupProvider.update(metadata,subscription.distributionKey(),subscription);
    }
    public static void _main(String[] args){
        LocalDateTime cur = LocalDateTime.now();
        LocalDateTime hourly = cur.plusHours(1);//.minusHours(11);
        for(int i=0;i<12;i++){
            System.out.println(MetricsSnapshot.hourlyLabel(hourly.minusHours(11-i)));
        }

        LocalDateTime daily = cur.plusDays(1);//.minusHours(11);
        for(int i=0;i<12;i++){
            System.out.println(MetricsSnapshot.dailyLabel(daily.minusDays(11-i)));
        }

        LocalDateTime weekly = cur.plusDays(8-cur.getDayOfWeek().getValue());//.minusHours(11);
        for(int i=0;i<12;i++){
            System.out.println(MetricsSnapshot.weeklyLabel(weekly.minusWeeks(11-i)));
        }

        MetricsSnapshot metricsSnapshot = new MetricsSnapshot(12,AccessMetrics.ACCOUNT_USER_CREATION_COUNT, LeaderBoard.HOURLY);
        //LocalDateTime hf = cur.minusMinutes(cur.getMinute()).plusHours(1);
        LocalDateTime xhf = cur.plusHours(1);
        for(int i=0;i<12;i++){

            String xh = MetricsSnapshot.hourlyLabel(xhf.minusHours(11-i));
            metricsSnapshot.initialize(new MetricsProperty(i,xh,0,cur),cur);
        }
        System.out.println(TimeUtil.fromUTCMilliseconds(metricsSnapshot.timestamp()));
        //System.out.println(">>"+metricsSnapshot.validateHourly(cur.plusDays(1)));
        for(Property p : metricsSnapshot.metrics()){
            System.out.println(p.name()+">>>>>>"+TimeUtil.fromUTCMilliseconds(p.timestamp()));
        }
        System.out.println(TimeUtil.midnight());

        //LocalDateTime cur = LocalDateTime.now();
        int hour = cur.getHour();
        LocalDateTime to50 = cur.toLocalDate().atTime(23,50,0,0).plusHours(1);
        System.out.println(to50.getHour());
        System.out.println(TimeUtil.midnight(to50.toLocalDate()));
        //return TimeUtil.durationUTCInSeconds(cur,to50);
        /**
        metricsSnapshot.update(100);
        if(!metricsSnapshot.validate(_cur.plusHours(1))){
            System.out.println("Need to reset");

        }**/
        //MetricsHistory history = new MetricsHistory(24);
        //history.initializeHourly(_cur);
        //Property[] mts = history.metrics();
        //for(int i=0;i<24;i++){
            //System.out.println(TimeUtil.fromUTCMilliseconds(mts[i].timestamp()));
        //}
        //metricsSnapshot.initialize();
        //long hs = TimeUtil.durationUTCInHours(_cur.minusYears(1).minusDays(0).minusHours(0).minusMinutes(59),_cur);
        String[] sf = "asset.a.".split("\\.");
        System.out.println("Day passed->"+sf[0]+">>>"+"asset.a".endsWith("."));
        System.out.println("Day passed->"+sf[1]);
    }
}
