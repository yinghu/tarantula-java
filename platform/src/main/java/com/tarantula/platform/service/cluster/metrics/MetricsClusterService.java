package com.tarantula.platform.service.cluster.metrics;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;
import com.icodesoftware.LeaderBoard;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceProvider;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.TarantulaContext;

import com.tarantula.platform.service.metrics.MetricsSnapshotResponse;
import com.tarantula.platform.service.metrics.ServiceViewRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;


public class MetricsClusterService implements ManagedService, RemoteService {

    private static TarantulaLogger log = JDKLogger.getLogger(MetricsClusterService.class);

    private NodeEngine nodeEngine;
    private TarantulaContext tarantulaContext;

    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        this.nodeEngine = nodeEngine;
        tarantulaContext = TarantulaContext.getInstance();
        log.warn("Metrics cluster service started");
    }

    @Override
    public void reset() {

    }

    @Override
    public void shutdown(boolean b) {

    }

    @Override
    public DistributedObject createDistributedObject(String objectName) {
        return new DistributionMetricsServiceProxy(objectName,nodeEngine,this);
    }

    @Override
    public void destroyDistributedObject(String objectName) {

    }

    public String metricsPayload(String serviceName){
        ServiceViewRequest request = new ServiceViewRequest(nodeEngine.getLocalMember().getUuid());
        ServiceProvider serviceProvider = this.tarantulaContext.serviceProvider(serviceName);
        serviceProvider.updateSummary(request);
        return request.toJson().toString();
    }
    public String metricsSnapshot(String name,String category,String classifier){
        Metrics m = this.tarantulaContext.metrics(name);
        Metrics.Spot[] dat = m.snapshot(category,classifier);
        MetricsSnapshotResponse response = new MetricsSnapshotResponse(nodeEngine.getLocalMember().getUuid());
        response.snapshot(dat);
        return response.toJson().toString();
    }
    public String metricsArchive(String name, String category, String classifier, LocalDateTime end){
        Metrics m = this.tarantulaContext.metrics(name);
        MetricsSnapshotResponse response = new MetricsSnapshotResponse(nodeEngine.getLocalMember().getUuid());
        switch (classifier){
            case LeaderBoard.DAILY:
                for(int i=23;i>=0;i--){
                    LocalDateTime hday = end.minusDays(i);
                    Metrics.History history = m.archive(category,hday);
                    String tag = hday.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                    response.archive(tag,history.dailyGain());
                }
                break;
            case LeaderBoard.WEEKLY:
                for(int i=23;i>=0;i--){
                    LocalDateTime wday = TimeUtil.toLastMonday(end.minusDays(i*7));
                    Metrics.History history = m.archive(category,wday);
                    String tag = wday.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                    response.archive(tag,history.weeklyGain());
                }
                break;
            case LeaderBoard.MONTHLY:
                for(int i=12;i>=0;i--){
                    LocalDateTime mday = TimeUtil.toFirstDayOfLastMonth(end.minusMonths(i));
                    Metrics.History history = m.archive(category,mday);
                    String tag = mday.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                    response.archive(tag,history.monthlyGain());
                }
                break;
            case LeaderBoard.YEARLY:
                for(int i=2;i>=0;i--){
                    LocalDateTime yday = TimeUtil.toFirstDayOfLastYear(end.minusYears(i));
                    Metrics.History history = m.archive(category,yday);
                    String tag = yday.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                    response.archive(tag,history.yearlyGain());
                }
                break;
            case LeaderBoard.HOURLY:
            default:
                Metrics.History history = m.archive(category,end);
                response.snapshot(history.hourlyGain());
        }
        return response.toJson().toString();
    }

}
