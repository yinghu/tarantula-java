package com.tarantula.platform.service.metrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Distributable;

import com.icodesoftware.Recoverable;
import com.icodesoftware.service.Metrics;

import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.statistics.StatisticsPortableRegistry;
import java.time.LocalDateTime;


public class MetricsHistory extends RecoverableObject implements Metrics.History {

    private final static int HOURLY_HISTORY_BUFFER_SIZE = 24;

    final static String LABEL_PREFIX = "history";


    private MetricsProperty[] metrics;

    private double dailyGain;
    private double weeklyGain;
    private double monthlyGain;
    private double yearlyGain;

    public int day;

    public MetricsHistory(){
        this.onEdge = true;
        this.metrics = new MetricsProperty[HOURLY_HISTORY_BUFFER_SIZE];
        for(int i=0;i<HOURLY_HISTORY_BUFFER_SIZE;i++){
            this.metrics[i]=new MetricsProperty(i,"m"+i,0d,0l);
        }
    }
    public MetricsHistory(String category,int year,int day){
        this();
        this.label = LABEL_PREFIX+"_"+category+"_"+year;
        this.day = day;
    }

    @Override
    public int scope() {
        return Distributable.LOCAL_SCOPE;
    }


    public boolean read(DataBuffer buffer){
        this.day = buffer.readInt();
        this.dailyGain = buffer.readDouble();
        this.weeklyGain = buffer.readDouble();
        this.monthlyGain = buffer.readDouble();
        this.yearlyGain = buffer.readDouble();
        for(int i=0; i<HOURLY_HISTORY_BUFFER_SIZE;i++){
            metrics[i].value = buffer.readDouble();
            metrics[i].timestamp(buffer.readLong());
        }
        return true;
    }
    public boolean write(DataBuffer buffer) {
        buffer.writeInt(day);
        buffer.writeDouble(dailyGain);
        buffer.writeDouble(weeklyGain);
        buffer.writeDouble(monthlyGain);
        buffer.writeDouble(yearlyGain);
        for(int i=0; i<HOURLY_HISTORY_BUFFER_SIZE;i++){
            buffer.writeDouble((double)metrics[i].value());
            buffer.writeLong(metrics[i].timestamp());
        }
        return true;
    }


    public Metrics.Spot[] metrics(){
        return metrics;
    }

    public Metrics.Spot[] hourlyGain(){
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


    @Override
    public int getFactoryId() {
        return StatisticsPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return StatisticsPortableRegistry.METRICS_HISTORY_CID;
    }
    //key label format history_[category]_[year]_[dayofyear]  history_httpRequestCount_2022_145

    public void archiveHourly(Metrics.Spot property){
        int hour  = TimeUtil.fromUTCMilliseconds(property.timestamp()).getHour();
        MetricsProperty archive = metrics[hour>0?(hour-1):HOURLY_HISTORY_BUFFER_SIZE-1];
        double v = archive.value;
        double d = property.value();
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

    public static String historyLabel(long metricsId,String category,LocalDateTime today){
        StringBuffer buffer = new StringBuffer().append(metricsId).append(Recoverable.PATH_SEPARATOR);
        buffer.append(Recoverable.PATH_SEPARATOR).append(MetricsHistory.LABEL_PREFIX).append("_").append(category).append("_");
        return buffer.append(today.getYear()).append("_").append(today.getDayOfYear()).toString();
    }

    @Override
    public JsonObject toJson() {
        JsonObject resp = new JsonObject();
        resp.addProperty("day",day);
        resp.addProperty("dailyGain",dailyGain);
        resp.addProperty("weeklyGain",weeklyGain);
        resp.addProperty("monthlyGain",monthlyGain);
        resp.addProperty("yearlyGain",yearlyGain);
        resp.addProperty("timestamp",timestamp);
        JsonArray history = new JsonArray();
        for(MetricsProperty m : metrics){
            history.add(m.toJson());
        }
        resp.add("data",history);
        return resp;
    }

}
