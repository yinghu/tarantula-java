package com.tarantula.platform.bootstrap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Properties;

import com.tarantula.TarantulaLogger;
import com.tarantula.licensing.Validator;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.EndPoint;
import com.tarantula.platform.service.cluster.ScopedMemberDiscovery;

public class TarantulaMain {
	static {
		System.setProperty("java.util.logging.manager","com.tarantula.logging.TarantulaLogManager");
	}
	private static final TarantulaLogger log = JDKLogger.getLogger(TarantulaMain.class);

	public static TarantulaMain._Runtime runtime;
	public static void main(String[] args){
		try{
			runtime = new TarantulaMain._Runtime();
			runtime.bootstrap();
		}catch(Exception ex){
			log.error("Failed to start system",ex);
			System.exit(-1);
		}
	}
	public static  class _Runtime{
        private ShutdownHook hook;
		private String override(boolean overriding,String name,Properties user,Properties system){
			String value = system.getProperty(name);
			if(overriding&&user.getProperty(name)!=null){
				value = user.getProperty(name);
			}
			return value;
		}
		public void bootstrap() throws Exception{
			if(!Validator.validate()){
				throw new IllegalArgumentException("no license found");
			}
			Properties _config = new Properties();
			_config.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("tarantula-default.properties"));
			Properties _user = new Properties();
			boolean overriding = true;
			try{
				log.info("Loading user configuration from /etc/tarantula/tarantula.properties");
				File f = new File("/etc/tarantula/tarantula.properties");
				if(f.exists()){
					InputStream in = new FileInputStream(f);
					_user.load(in);
				}
				else{
					log.info("Loading user configuration from class path ...");
					_user.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("tarantula.properties"));
				}
			}catch (Exception ex){
				log.warn("No user configurations used to override system configurations");
				overriding = false;
			}
			TarantulaContext btx = TarantulaContext.getInstance();
			btx.memberDiscovery = (ScopedMemberDiscovery) Class.forName(override(overriding,"tarantula.member.discovery.name",_user,_config)).getConstructor().newInstance();
			btx.platformVersion = override(overriding,"tarantula.platform.version",_user,_config);
			btx.platformRoutingNumber = Integer.parseInt(override(overriding,"tarantula.platform.routing.number",_user,_config));
			btx.bootstrapRetries = Integer.parseInt(override(overriding,"tarantula.bootstrap.max.retries",_user,_config));
			btx.clusterNamePrefix = override(overriding,"tarantula.cluster.name",_user,_config);
			btx.dataBucketGroup = override(overriding,"tarantula.data.bucket.group",_user,_config);
			btx.dataBucketNode = override(overriding,"tarantula.data.bucket.node",_user,_config);
			btx.dataBucketId = override(overriding,"tarantula.data.bucket.id",_user,_config);
			btx.dataStoreDir = override(overriding,"tarantula.data.store.dir",_user,_config);
			btx.dataStoreRecoveryDir = override(overriding,"tarantula.data.store.recovery.dir",_user,_config);
			btx.dataReplicationThreadPoolSetting = override(overriding,"tarantula.data.replication.pool.setting",_user,_config);
            btx.eventThreadPoolSetting = override(overriding,"tarantula.event.pool.setting",_user,_config);
            btx.retries  = Integer.parseInt(override(overriding,"tarantula.event.max.retries",_user,_config));
            btx.retryInterval = Long.parseLong(override(overriding,"tarantula.event.retry.interval",_user,_config));
			btx.operationRetries = Integer.parseInt(override(overriding,"tarantula.operation.retries",_user,_config));
			btx.operationRejectInterval = Long.parseLong(override(overriding,"tarantula.operation.reject.interval.ms",_user,_config));
		    btx.tarantulaServerValidator = override(overriding,"tarantula.service.authenticator.provider",_user,_config);
		    btx.tarantulaDeploymentProvider = override(overriding,"tarantula.service.deployment.service.provider",_user,_config);
		    btx.maxActiveSessionNumber = Integer.parseInt(override(overriding,"tarantula.endpoint.session.max.number",_user,_config));
			btx.tokenTimeout = Integer.parseInt(override(overriding,"tarantula.endpoint.session.timeout.m",_user,_config));
		    btx.maxIdlesOnInstance = Integer.parseInt(override(overriding,"tarantula.instance.session.idle.number",_user,_config));
		    btx.timeoutOnInstance = 1000*Long.parseLong(override(overriding,"tarantula.instance.session.timeout.s",_user,_config));
			btx.applicationSchedulingPoolSetting = override(overriding,"tarantula.scheduler.pool.setting",_user,_config);
			btx.dataStoreProviderConfiguration = override(overriding,"tarantula.data.store.configuration.file",_user,_config);
            btx.serviceConfiguration = override(overriding,"tarantula.service.configuration.file",_user,_config);
			btx.dataStoreMaster = override(overriding,"tarantula.data.store.master",_user,_config);
			btx.dataStoreDailyBackup = Boolean.parseBoolean(override(overriding,"tarantula.data.store.daily.backup",_user,_config));
			btx.authContext = override(overriding,"tarantula.auth.context",_user,_config);
			boolean tcpEnabled = Boolean.parseBoolean(override(overriding,"tarantula.endpoint.socket.enable",_user,_config));
			if(tcpEnabled){
				EndPoint se = (EndPoint)Class.forName(override(overriding,"tarantula.endpoint.socket",_user,_config)).getConstructor().newInstance();
				se.address(override(overriding,"tarantula.endpoint.socket.address",_user,_config));
				se.port(Integer.parseInt(override(overriding,"tarantula.endpoint.socket.port",_user,_config)));
				se.backlog(Integer.parseInt(override(overriding,"tarantula.endpoint.socket.backlog",_user,_config)));
				se.inboundThreadPoolSetting(override(overriding,"tarantula.endpoint.socket.pool.in.setting",_user,_config));
				btx.endpointService.addEndPoint(se);
			}

			boolean endpointEnabled = Boolean.parseBoolean(override(overriding,"tarantula.endpoint.http.enable",_user,_config));
			if(endpointEnabled){
				EndPoint he = (EndPoint)Class.forName(override(overriding,"tarantula.endpoint.http",_user,_config)).getConstructor().newInstance();
				he.address(override(overriding,"tarantula.endpoint.http.address",_user,_config));
				he.backlog(Integer.parseInt(override(overriding,"tarantula.endpoint.http.backlog",_user,_config)));
				he.inboundThreadPoolSetting(override(overriding,"tarantula.endpoint.http.pool.in.setting",_user,_config));
				he.port(Integer.parseInt(override(overriding,"tarantula.endpoint.http.port",_user,_config)));
				btx.endpointService.addEndPoint(he);
			}
			btx.start();
			hook = new ShutdownHook();
			hook.setName("tarantula-shutdown-hook");
			Runtime.getRuntime().addShutdownHook(hook);
			hook.register(btx);
			FileWriter fw = new FileWriter("tarantula.pid");
			fw.write(""+ProcessHandle.current().pid());
			fw.close();
		}
		public void reboot() throws Exception{
			Runtime.getRuntime().removeShutdownHook(hook);
			hook.run();
			bootstrap();
		}
	}
	
}
