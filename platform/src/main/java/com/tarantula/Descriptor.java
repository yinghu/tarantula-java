package com.tarantula;

import com.icodesoftware.Recoverable;

public interface Descriptor extends Recoverable {

    //the order of the deployment
    int deployPriority();
    void deployPriority(int deployPriority);

    //the none zero code of module or app provider. Zero deploy code is system default modules or apps
    int deployCode();
    void deployCode(int deployCode);

    //module code location that could be http endpoint, local file folder
    String codebase();
    void codebase(String codebase);

    //module package name
    String moduleArtifact();
    void moduleArtifact(String moduleArtifact);

    //module package version
    String moduleVersion();
    void moduleVersion(String moduleVersion);

    //module class name that implements Module interface
    String moduleName();
    void moduleName(String moduleName);

    //module update timer milliseconds. 50ms per server frame
    long timerOnModule();
    void timerOnModule(long timerOnModule);

    String applicationId(); //application deploy unique ID
    String instanceId(); //instance deploy unique ID
    void applicationId(String application);
    void instanceId(String instanceId);

    //end of deployment control

    boolean logEnabled();
    void logEnabled(boolean logEnabled);

    String typeId(); //application type ID that should be associated with the lobby

    String type(); //application type
    String subtypeId();//application group id associated with the module artifact

    String category(); //lobby, game, service, etc
    String responseLabel();

	String name();
    String icon();
	void icon(String icon);
    String description();

    String viewId();
    void viewId(String viewId);

    int capacity(); //the max joined number of the instance

    boolean singleton(); //single instance per node

    int accessMode();
    int accessControl(); // 0 - 10
    int accessRank();
    String tag(); //service deploy tag
    double entryCost();

	void typeId(String id);//the system lobby unique ID

    void type(String type);
    void subtypeId(String subtypeId);//the none-unique group ID

    void category(String category);
    void responseLabel(String responseLabel);
	void name(String name);

	void description(String description);

	void capacity(int capacity);	

    void singleton(boolean singleton);

    void accessControl(int accessControl);
    void accessMode(int accessMode);
    void accessRank(int accessRank);
    void tag(String tag);
    void entryCost(double entryCost);

    int maxIdlesOnInstance(); //max client no-action idles before timeout. per idle time is the setting of per app ticker 60 seconds
    void maxIdlesOnInstance(int maxIdlesOnInstance);

    int maxInstancesPerPartition();
    void maxInstancesPerPartition(int maxPoolSize);

    int instancesOnStartupPerPartition();
    void instancesOnStartupPerPartition(int instancesOnStartup);

    String applicationClassName();

    void applicationClassName(String applicationClassName);

    long runtimeDuration(); //the application duration time in minutes
    void runtimeDuration(long runtimeDuration);

    long runtimeDurationOnInstance(); //the instance duration time in minutes
    void runtimeDurationOnInstance(long runtimeDuration);

    boolean resetEnabled();
    void resetEnabled(boolean resetEnabled);

    default Descriptor copy(){
        return null;
    }

}
