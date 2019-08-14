package com.tarantula.platform;

/**
 * Updated by yinghu on 5/26/2018.
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
    protected String from;
    protected int action; //tournament command
    protected String tag;
    protected String viewId;
    protected String type;

    protected String title;
    protected String description;
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
    public String from() {
        return this.from;
    }

    public void from(String from) {
        this.from = from;
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
    public void tag(String tag){
        this.tag = tag;
    }
    public String tag(){
        return this.tag;
    }
    public String viewId(){
        return this.viewId;
    }
    public void viewId(String viewId){
        this.viewId = viewId;
    }
    public void type(String type){
        this.type = type;
    }
    public String type(){
        return this.type;
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

    public void action(int command){
        this.action = command;
    }
    public int action(){
        return this.action;
    }
    public String title(){
        return this.title;
    }
    public void title(String title){
        this.title = title;
    }

    public String description(){
        return this.description;
    }
    public void description(String description){
        this.description = description;
    }
    public int stub(){return this.stub;}
    public void stub(int stub){ this.stub = stub;}

}
