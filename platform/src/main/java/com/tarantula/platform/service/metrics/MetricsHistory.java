package com.tarantula.platform.service.metrics;

import com.google.gson.JsonObject;
import com.icodesoftware.Property;
import com.icodesoftware.Recoverable;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.statistics.StatisticsPortableRegistry;

import java.time.LocalDateTime;
import java.util.Map;

public class MetricsHistory extends RecoverableObject implements Metrics.History {

    private final static int HOURLY_HISTORY_BUFFER_SIZE = 24;

    final static String LABEL_PREFIX = "history";


    private Property[] metrics;

    private double dailyGain;
    private double weeklyGain;
    private double monthlyGain;
    private double yearlyGain;

    public MetricsHistory(){
        this.metrics = new Property[HOURLY_HISTORY_BUFFER_SIZE];
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("dailyGain",dailyGain);
        this.properties.put("weeklyGain",weeklyGain);
        this.properties.put("monthlyGain",monthlyGain);
        this.properties.put("yearlyGain",yearlyGain);
        for(int i=0;i<HOURLY_HISTORY_BUFFER_SIZE;i++){
            this.properties.put("m"+i,metrics[i].toJson().toString());
        }
        return this.properties;
    }

    @Override
    public void fromMap(Map<String,Object> properties){
        this.dailyGain = ((Number)properties.get("dailyGain")).doubleValue();
        this.weeklyGain = ((Number)properties.get("weeklyGain")).doubleValue();
        this.monthlyGain = ((Number)properties.get("monthlyGain")).doubleValue();
        this.yearlyGain = ((Number)properties.get("yearlyGain")).doubleValue();
        for(int i=0;i<HOURLY_HISTORY_BUFFER_SIZE;i++){
            Object payload = properties.get("m"+i);
            JsonObject mj = JsonUtil.parse((String)payload);
            metrics[i]=(new MetricsProperty(i, mj.get("name").getAsString(), Double.parseDouble(mj.get("value").getAsString()),mj.get("timestamp").getAsLong()));
        }
    }


    public Property[] metrics(){
        return metrics;
    }

    public Property[] hourlyGain(){
        return metrics;
    }
    public double dailyGain(){
        return dailyGain;
    }
    public double weeklyGain(){
        return weeklyGain;
    }
    public double monthlyGain(){
        return monthlyGain;
    }
    public double yearlyGain(){
        return yearlyGain;
    }

    public void distributionKey(String rkey){
        String[] idx = rkey.split(Recoverable.PATH_SEPARATOR);
        bucket = idx[0];
        oid = idx[1];
        label = idx[2];
    }
    @Override
    public int getFactoryId() {
        return StatisticsPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return StatisticsPortableRegistry.METRICS_HISTORY_CID;
    }
    //key label format history_[category]_[year]_[dayofyear]  history_httpRequestCount_2022_145
    public Key key(){
        return new AssociateKey(this.bucket,oid,label);
    }

    public void archiveHourly(Property property){
        int hour  = TimeUtil.fromUTCMilliseconds(property.timestamp()).getHour();
        MetricsProperty archive = (MetricsProperty)metrics[hour>0?(hour-1):HOURLY_HISTORY_BUFFER_SIZE-1];
        if(archive==null){
            metrics[hour>0?(hour-1):HOURLY_HISTORY_BUFFER_SIZE-1] = property;
            return;
        }
        double v = (Double)archive.value;
        double d = (Double)property.value();
        archive.value = v+d;
    }

    public void initializeHourly(LocalDateTime current){
        LocalDateTime start = current.minusHours(current.getHour());
        for(int i=0;i<HOURLY_HISTORY_BUFFER_SIZE;i++){
            LocalDateTime hour = start.plusHours(i);
            archiveHourly(new MetricsProperty(i,MetricsProperty.historyPropertyLabel(hour),0d,hour));
        }
    }

    public void archiveDaily(double dailyGain,LocalDateTime updated){
        this.dailyGain = dailyGain;
        this.timestamp = TimeUtil.toUTCMilliseconds(updated);
    }
    public void archiveWeekly(double weeklyGain,LocalDateTime updated){
        this.weeklyGain = weeklyGain;
        this.timestamp = TimeUtil.toUTCMilliseconds(updated);
    }
    public void archiveMonthly(double monthlyGain,LocalDateTime updated){
        this.monthlyGain = monthlyGain;
        this.timestamp = TimeUtil.toUTCMilliseconds(updated);
    }
    public void archiveYearly(double yearlyGain,LocalDateTime updated){
        this.yearlyGain = yearlyGain;
        this.timestamp = TimeUtil.toUTCMilliseconds(updated);
    }

    public static String historyLabel(String bucket,String oid,String category,LocalDateTime today){
        StringBuffer buffer = new StringBuffer().append(bucket).append(Recoverable.PATH_SEPARATOR).append(oid);
        buffer.append(Recoverable.PATH_SEPARATOR).append(MetricsHistory.LABEL_PREFIX).append("_").append(category).append("_");
        return buffer.append(today.getYear()).append("_").append(today.getDayOfYear()).toString();
    }

}
