package com.tarantula.test;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.protocol.Channel;
import com.icodesoftware.protocol.GameServerListener;
import com.icodesoftware.service.*;
import com.tarantula.platform.item.ConfigurableTemplate;
import com.tarantula.platform.item.JsonConfigurableTemplateParser;

import java.io.File;
import java.util.List;

public class TestDeploymentProvider implements DeploymentServiceProvider {

    private DataStoreProvider dataStoreProvider;
    public TestDeploymentProvider(DataStoreProvider dataStoreProvider){
        this.dataStoreProvider = dataStoreProvider;
    }
    @Override
    public OnAccess registerConnection(Connection connection) {
        return null;
    }

    @Override
    public boolean registerChannel(Channel channel) {
        return false;
    }

    @Override
    public void updateRoom(String typeId, String lobby, byte[] payload) {

    }

    @Override
    public void startConnection(Connection connection) {

    }

    @Override
    public void stopConnection(Connection connection) {

    }
    public Metrics metrics(String name){
        return null;
    }
    @Override
    public void verifyConnection(String typeId, String serverId) {

    }

    public Transaction transaction(int scope){
        return null;
    }

    @Override
    public byte[] serverKey(String typeId) {
        return new byte[0];
    }

    @Override
    public String registerGameServerListener(GameServerListener gameServerListener) {
        return null;
    }

    @Override
    public void unregisterGameServerListener(String registerKey) {

    }

    @Override
    public void register(ServiceProvider serviceProvider) {

    }

    @Override
    public void release(ServiceProvider serviceProvider) {

    }

    @Override
    public OnView view(String viewId) {
        return null;
    }

    @Override
    public Response createView(OnView onView) {
        return null;
    }

    @Override
    public Response deployResource(String contentUrl, String resourceName) {
        return null;
    }

    @Override
    public Content resource(String name) {
        return null;
    }

    @Override
    public void deleteResource(String name) {

    }

    @Override
    public String resetCode(String key) {
        return null;
    }

    @Override
    public String checkCode(String resetCode) {
        return null;
    }

    @Override
    public Module module(Descriptor descriptor) {
        return null;
    }

    @Override
    public void resource(Descriptor descriptor, String name, Module.OnResource onResource) {

    }

    @Override
    public Response deployModule(String contextUrl, String resourceName) {
        return null;
    }

    @Override
    public Response createModule(Descriptor descriptor) {
        return null;
    }

    @Override
    public Response exportModule(Descriptor descriptor) {
        return null;
    }

    @Override
    public boolean launchModule(String typeId) {
        return false;
    }

    @Override
    public boolean resetModule(Descriptor descriptor) {
        return false;
    }

    @Override
    public boolean shutdownModule(String typeId) {
        return false;
    }

    @Override
    public ClassLoader classLoader(String moduleId) {
        return null;
    }

    @Override
    public boolean createApplication(Descriptor descriptor, String configName, boolean launching) {
        return false;
    }

    @Override
    public boolean updateApplication(Descriptor descriptor, OnAccess properties) {
        return false;
    }

    @Override
    public boolean enableApplication(long applicationId) {
        return false;
    }

    @Override
    public boolean disableApplication(long applicationId) {
        return false;
    }

    @Override
    public <T extends OnAccess> T createGameCluster(Account accountId, String name, OnAccess properties) {
        return null;
    }

    @Override
    public <T extends OnAccess> List<T> gameClusterList(Access access) {
        return null;
    }

    @Override
    public <T extends OnAccess> T updateGameCluster(long gameClusterId, OnAccess properties) {
        return null;
    }

    @Override
    public <T extends OnAccess> boolean launchGameCluster(T gameCluster) {
        return false;
    }

    @Override
    public <T extends OnAccess> boolean shutdownGameCluster(T gameCluster) {
        return false;
    }

    @Override
    public <T extends OnAccess> T gameCluster(long key) {
        return null;
    }

    @Override
    public List<Descriptor> gameServiceList() {
        return null;
    }

    @Override
    public <T extends Configuration, S extends OnAccess> T configuration(S gameCluster, String config) {
        return (T)JsonConfigurableTemplateParser.itemSet(Thread.currentThread().getContextClassLoader().getResourceAsStream(config+".json"));

    }

    @Override
    public Lobby lobby(String typeId) {
        return null;
    }

    @Override
    public void registerAccessIndexListener(AccessIndexService.Listener listener) {

    }


    @Override
    public File issueDataStoreBackup(int scope) {
        return null;
    }

    @Override
    public List<String> listDataStore(int scope) {
        return null;
    }

    @Override
    public List<String> listServiceView() {
        return null;
    }

    @Override
    public List<String> listMetricsView() {
        return null;
    }

    @Override
    public DataStoreSummary validDataStore(String dataStore) {
        return null;
    }

    @Override
    public ClusterProvider.Summary clusterSummary() {
        return null;
    }

    @Override
    public DistributionCallback distributionCallback() {
        return null;
    }

    @Override
    public void onStart(EndPoint endPoint) {

    }

    @Override
    public void onUpdated(String mkey, double delta) {

    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
