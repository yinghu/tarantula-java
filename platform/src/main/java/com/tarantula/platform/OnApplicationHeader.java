package com.tarantula.platform;

import com.icodesoftware.OnAccess;

/**
 * Updated by yinghu on 5/25/2020
 */
public class OnApplicationHeader extends ResponseHeader implements OnAccess {

    protected String applicationId;
    protected String instanceId;
    protected double entryCost;

    protected String systemId;
    protected String name;
    protected String typeId;
    protected String subtypeId;
    protected int accessMode;

    protected double balance;
    protected String event;
    protected boolean redeemed;

    protected int stub;

    protected String ticket;

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

    public String ticket(){
        return this.ticket;
    }
    public void ticket(String ticket){
        this.ticket = ticket;
    }
}
