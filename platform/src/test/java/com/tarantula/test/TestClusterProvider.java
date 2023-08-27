package com.tarantula.test;

import com.icodesoftware.Event;
import com.icodesoftware.EventListener;
import com.icodesoftware.RoutingKey;
import com.icodesoftware.service.*;
import com.tarantula.platform.TarantulaApplicationHeader;


public class TestClusterProvider extends TarantulaApplicationHeader implements ClusterProvider {
    @Override
    public String name() {
        return null;
    }

    @Override
    public int scope() {
        return 0;
    }
    public int maxSize(){
        return 10;
    }
    public int maxReplicationNumber(){
        return 10;
    }
    @Override
    public String bucket() {
        return null;
    }

    @Override
    public EventService publisher() {
        return new EventService() {
            @Override
            public void publish(Event out) {

            }

            @Override
            public void retry(String retryKey) {

            }

            @Override
            public void registerEventListener(String topic, EventListener callback) {

            }

            @Override
            public void unregisterEventListener(String topic) {

            }

            @Override
            public RoutingKey routingKey(Object magicKey, String tag) {
                return null;
            }

            @Override
            public RoutingKey routingKey(Object magicKey, String tag, int routingNumber) {
                return null;
            }

            @Override
            public boolean onEvent(Event event) {
                return false;
            }

            @Override
            public void start() throws Exception {

            }

            @Override
            public void shutdown() throws Exception {

            }
        };
    }

    @Override
    public EventService subscribe(String topic, EventListener callback) {
        return null;
    }

    @Override
    public void unsubscribe(String topic) {

    }

    @Override
    public AccessIndexService accessIndexService() {
        return new TestAccessIndexService();
    }



    @Override
    public DeployService deployService() {
        return null;
    }

    @Override
    public RecoverService recoverService() {
        return new TestRecoverService();
    }

    @Override
    public <T extends ServiceProvider> T serviceProvider(String name) {
        return null;
    }

    @Override
    public ClusterStore clusterStore(String size,String name) {
        return null;
    }

    public ClusterStore clusterStore(String size,String name,boolean map,boolean index,boolean queue){
        return null;
    }

    @Override
    public void registerMetricsListener(MetricsListener metricsListener) {

    }

    @Override
    public String registerReloadListener(ReloadListener reloadListener) {
        return null;
    }

    @Override
    public void unregisterReloadListener(String registerKey) {

    }
    public void registerNodeListener(NodeListener nodeListener){}

    @Override
    public Summary summary() {
        return null;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    public int partition(byte[] key){
        return 0;//_cluster.getPartitionService().getPartition(key).getPartitionId();
    }
}
