package com.icodesoftware;

public interface Descriptor extends Recoverable {

    String TYPE_LOBBY ="lobby";
    String TYPE_APPLICATION = "application";

    //the order of the deployment
    int deployPriority();
    void deployPriority(int deployPriority);

    //the none zero code of module or app provider. Zero deploy code is system default modules or apps
    int deployCode();
    void deployCode(int deployCode);

    //module code location that could be http endpoint, local file folder
    String moduleId();
    void moduleId(String moduleId);

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

    boolean logEnabled();
    void logEnabled(boolean logEnabled);

    String typeId(); //application type ID that should be associated with the lobby

    String type(); //application type

    String category(); //lobby, game, service, etc

	String name();

    int accessMode();
    int accessControl(); // 0 - 10
    int accessRank();
    String tag(); //service deploy tag
    double entryCost();

	void typeId(String id);//the system lobby unique ID

    void type(String type);

    void category(String category);

	void name(String name);

    void accessControl(int accessControl);
    void accessMode(int accessMode);
    void accessRank(int accessRank);
    void tag(String tag);
    void entryCost(double entryCost);


    String applicationClassName();

    void applicationClassName(String applicationClassName);


    boolean resetEnabled();
    void resetEnabled(boolean resetEnabled);

    boolean tournamentEnabled();
    void tournamentEnabled(boolean tournamentEnabled);

    default Descriptor copy(){
        return null;
    }

}
