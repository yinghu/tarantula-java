package com.tarantula.platform;

/**
 * Updated by yinghu on 8/23/19.
 */
public class OnApplicationHeader extends ResponseHeader{

    protected String applicationId;
    protected String instanceId;
    protected double entryCost;
    protected boolean tournamentEnabled;

    protected String systemId;
    protected String name;
    protected String typeId;
    protected String subtypeId;
    protected int accessMode;

    protected double balance;
    protected String event;
    protected boolean redeemed;

    protected int stub;



    public double balance() {
        return this.balance;
    }


    public void balance(double balance) {
        this.balance = balance;
    }

    public void redeemed(boolean redeemed){
        this.redeemed = redeemed;
    }
    public boolean redeemed(){
        return this.redeemed;
    }

    public boolean tournamentEnabled(){
        return this.tournamentEnabled;
    }
    public void tournamentEnabled(boolean tournamentEnabled){
        this.tournamentEnabled = tournamentEnabled;
    }


    public String systemId() {
        return this.systemId;
    }


    public void systemId(String systemId) {
        this.systemId = systemId;
    }


    public String applicationId() {
        return applicationId;
    }


    public void applicationId(String applicationId) {
        this.applicationId  = applicationId;
    }


    public String instanceId() {
        return this.instanceId;
    }


    public void instanceId(String instanceId) {
        this.instanceId = instanceId;
    }


    public double entryCost() {
        return this.entryCost;
    }


    public void entryCost(double entryCost) {
        this.entryCost = entryCost;
    }

    public int accessMode(){
        return this.accessMode;
    }
    public void accessMode(int mode){
        this.accessMode = mode;
    }

    public String name(){
        return this.name;
    }
    public void name(String name){
        this.name = name;
    }
    public String typeId(){
        return this.typeId;
    }
    public void typeId(String typeId){
        this.typeId = typeId;
    }
    public String subtypeId(){
        return this.subtypeId;
    }
    public void subtypeId(String subtypeId){
        this.subtypeId = subtypeId;
    }

    public void event(String event){
        this.event = event;
    }
    public String event(){
        return this.event;
    }


    public int stub(){return this.stub;}
    public void stub(int stub){ this.stub = stub;}

}
