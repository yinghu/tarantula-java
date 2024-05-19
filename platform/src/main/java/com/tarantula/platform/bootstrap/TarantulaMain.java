package com.tarantula.platform.bootstrap;

import java.io.*;
import java.util.Properties;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.EndPoint;
import com.tarantula.licensing.Validator;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.cluster.ScopedMemberDiscovery;

public class TarantulaMain {
	static {
		System.setProperty("java.util.logging.manager","com.icodesoftware.logging.TarantulaLogManager");
	}
	private static final TarantulaLogger log = JDKLogger.getLogger(TarantulaMain.class);

	public static TarantulaMain._Runtime runtime;


	public static void main(String[] args){
		try{
			if(args.length == 3){
				DataBootstrap.run(args[0],args[1],args[2]);
			}
			runtime = new TarantulaMain._Runtime();
			runtime.bootstrap();
		}catch(Exception ex){
			log.error("Failed to start system",ex);
			System.exit(-1);
		}
	}
	public static  class _Runtime{
        private  ShutdownHook hook;

        private String override(boolean overriding, String name, Properties user, Properties system){
			String value = system.getProperty(name);
			if(overriding && user.getProperty(name) != null){
				value = user.getProperty(name);
			}
			return value;
		}

		public void bootstrap() throws Exception{
			if(!Validator.validate()){
				throw new IllegalArgumentException("no license found");
			}
			TarantulaContext.releaseVersion = Validator.version();
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

			// This block uses the hostname to generate node id for both bucket and snowflake config
			// This allows us to not need to specify unique config for every node
			try {
				var useHostname = System.getenv("USE_HOSTNAME");
				if(useHostname.equals("true"))
				{
					var hostname = System.getenv("HOSTNAME");
					log.info("Hostname is " + hostname);

					var index = hostname.substring(hostname.lastIndexOf('-') + 1);
					if(index.length() == 1)
					{
						index = "0" + index;
					}
					log.info("Hostname Index is " + index);

					_user.setProperty("tarantula.data.bucket.node", "N" + index);
					_user.setProperty("tarantula.snowflake.node.number", index);
				}
			} catch (Exception ignored) {}

			TarantulaContext btx = TarantulaContext.getInstance();
			TarantulaContext.memberDiscovery = (ScopedMemberDiscovery) Class.forName(override(overriding,"tarantula.member.discovery.name",_user,_config)).getConstructor().newInstance();
			TarantulaContext.operationTimeout = Integer.parseInt(override(overriding,"tarantula.operation.timeout.seconds",_user,_config));
			TarantulaContext.lobbySubscriptionEnabled  = Boolean.parseBoolean(override(overriding,"tarantula.lobby.subscription.enabled",_user,_config));
			TarantulaContext.operationRetries = Integer.parseInt(override(overriding,"tarantula.operation.retries",_user,_config));
			TarantulaContext.operationRejectInterval = Long.parseLong(override(overriding,"tarantula.operation.reject.interval.ms",_user,_config));
			btx.snowflakeNodeNumber = Integer.parseInt(override(overriding,"tarantula.snowflake.node.number",_user,_config));
			btx.storeSizeMb = Integer.parseInt(override(overriding,"tarantula.data.store.size.mb",_user,_config));
			btx.storeKeySize = Integer.parseInt(override(overriding,"tarantula.data.store.key.size",_user,_config));
			btx.storeValueSize = Integer.parseInt(override(overriding,"tarantula.data.store.value.size",_user,_config));
			btx.storePendingBufferSize = Integer.parseInt(override(overriding,"tarantula.data.store.pending.buffer.size",_user,_config));
			btx.storeNoSync = Boolean.parseBoolean(override(overriding,"tarantula.data.store.no.sync",_user,_config));
			String[] epochStart = override(overriding,"tarantula.snowflake.epoch.start",_user,_config).split(",");
			btx.snowflakeEpochStart = new int[]{Integer.parseInt(epochStart[0]),Integer.parseInt(epochStart[1]),Integer.parseInt(epochStart[2])};
			btx.platformRoutingNumber = Integer.parseInt(override(overriding,"tarantula.platform.routing.number",_user,_config));
			btx.accessIndexRoutingNumber = Integer.parseInt(override(overriding,"tarantula.platform.access.index.routing.number",_user,_config));
			btx.maxReplicationNumber = Integer.parseInt(override(overriding,"tarantula.data.store.replication.max.number",_user,_config));
			btx.clusterInitialSize = Integer.parseInt(override(overriding,"tarantula.platform.cluster.initial.size",_user,_config));
			btx.clusterMaxSize = Integer.parseInt(override(overriding,"tarantula.platform.cluster.max.size",_user,_config));
			btx.clusterNameSuffix = override(overriding,"tarantula.cluster.name.suffix",_user,_config);
			btx.dataBucketGroup = override(!overriding,"tarantula.data.bucket.group",_user,_config);
			btx.dataBucketNode = override(overriding,"tarantula.data.bucket.node",_user,_config);
			btx.dataStoreDir = override(overriding,"tarantula.data.store.dir",_user,_config);
			btx.recoverBatchSize = Integer.parseInt(override(overriding,"tarantula.data.store.recover.batch.size",_user,_config));
            btx.eventThreadPoolSetting = override(overriding,"tarantula.event.pool.setting",_user,_config);
            btx.retries  = Integer.parseInt(override(overriding,"tarantula.event.max.retries",_user,_config));
            btx.retryInterval = Long.parseLong(override(overriding,"tarantula.event.retry.interval",_user,_config));
		    btx.tarantulaServerValidator = override(overriding,"tarantula.service.authenticator.provider",_user,_config);
		    btx.tarantulaDeploymentProvider = override(overriding,"tarantula.service.deployment.service.provider",_user,_config);
		    btx.singleModuleApplication = override(overriding,"tarantula.service.deployment.module.singleton.application",_user,_config);
			btx.servicePushAddress = override(overriding,"tarantula.service.push.address",_user,_config);
		    btx.deployDir  = override(overriding,"tarantula.service.deployment.dir",_user,_config);
		    btx.maxActiveSessionNumber = Integer.parseInt(override(overriding,"tarantula.endpoint.session.max.number",_user,_config));
			btx.tokenTimeout = Integer.parseInt(override(overriding,"tarantula.endpoint.session.timeout.m",_user,_config));
			btx.ticketTimeout = Integer.parseInt(override(overriding,"tarantula.endpoint.ticket.timeout.s",_user,_config));
		    btx.maxIdlesOnInstance = Integer.parseInt(override(overriding,"tarantula.instance.session.idle.number",_user,_config));
		    btx.timeoutOnInstance = 1000*Long.parseLong(override(overriding,"tarantula.instance.session.timeout.s",_user,_config));
			btx.applicationSchedulingPoolSetting = override(overriding,"tarantula.scheduler.pool.setting",_user,_config);
			btx.dataStoreDailyBackup = Boolean.parseBoolean(override(overriding,"tarantula.data.store.daily.backup",_user,_config));
			btx.authContext = override(overriding,"tarantula.auth.context",_user,_config);
			boolean udpEndpointEnabled = Boolean.parseBoolean(override(overriding,"tarantula.endpoint.udp.enable",_user,_config));
			boolean endpointEnabled = Boolean.parseBoolean(override(overriding,"tarantula.endpoint.http.enable",_user,_config));
			if(endpointEnabled){
				EndPoint he = (EndPoint)Class.forName(override(overriding,"tarantula.endpoint.http",_user,_config)).getConstructor().newInstance();
				he.address(override(overriding,"tarantula.endpoint.http.address",_user,_config));
				he.backlog(Integer.parseInt(override(overriding,"tarantula.endpoint.http.backlog",_user,_config)));
				he.inboundThreadPoolSetting(override(overriding,"tarantula.endpoint.http.pool.in.setting",_user,_config));
				he.port(Integer.parseInt(override(overriding,"tarantula.endpoint.http.port",_user,_config)));
				btx.endpointService().addEndPoint(he);
			}
			if(udpEndpointEnabled){
				EndPoint ud = (EndPoint)Class.forName(override(overriding,"tarantula.endpoint.udp",_user,_config)).getConstructor().newInstance();
				ud.address(override(overriding,"tarantula.endpoint.udp.address",_user,_config));
				ud.inboundThreadPoolSetting(override(overriding,"tarantula.endpoint.udp.pool.in.setting",_user,_config));
				ud.port(Integer.parseInt(override(overriding,"tarantula.endpoint.udp.port",_user,_config)));
				btx.endpointService().addEndPoint(ud);
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

		public void shutdown() throws Exception{
			Runtime.getRuntime().removeShutdownHook(hook);
			hook.run();
			System.exit(1);
		}
	}
	
}
