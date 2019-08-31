package com.tarantula.test.integration;

import com.google.gson.GsonBuilder;
import com.tarantula.Descriptor;
import com.tarantula.Response;
import com.tarantula.platform.marketplace.MarketplaceContext;
import com.tarantula.platform.presence.IndexContext;
import com.tarantula.platform.presence.PresenceContext;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Updated by yinghu lu on 10/20/2017.
 */
public class Simulator implements Runnable {


    GsonBuilder gsonBuilder;
    AtomicInteger round;
    CountDownLatch countDownLatch;
    String host;
    String user;
    boolean secured;
    public Simulator(boolean secured,String host,GsonBuilder gsonBuilder, AtomicInteger round, CountDownLatch countDownLatch,String user){
        this.secured = secured;
        this.host = host;
        this.gsonBuilder = gsonBuilder;
        this.round = round;
        this.countDownLatch = countDownLatch;
        this.user = user;
    }

    public void run() {
        try{
            IndexContext index = new OnIndexCommand(secured,host,gsonBuilder).call();
            Descriptor[] app = {null};
            index.lobbyList.get(0).entryList().forEach((a)->{
                if(a.tag().equals("user")){
                    app[0] =a;
                }
            });
            Response register = new OnRegistrationCommand(secured,host,gsonBuilder,user,"password",app[0]).call();
            if(register.successful()){
                PresenceContext login = new OnLoginCommand(secured,host,gsonBuilder,user,"password",app[0]).call();
                if(login.successful()){
                    PresenceContext presence = new OnPresenceCommand(secured,host,gsonBuilder,login).call();
                    if(presence.successful()){
                        presence.presence = login.presence;
                        String appId = new OnLobbyCommand(secured,host,gsonBuilder,presence,"demo-sync").call();
                        new OnPlayCommand(secured,host,gsonBuilder,presence,appId).call();
                        new OnProfileCommand(secured,host,gsonBuilder,presence).call();
                        Response logout = new OnLogoutCommand(secured,host,gsonBuilder,login).call();
                        if(logout.successful()){
                            round.getAndIncrement();
                        }
                        else{
                            System.out.println("logout failed");
                        }
                    }
                    else{
                        System.out.println("presence failed");
                    }
                }
                else{
                    System.out.println("login failed");
                }
            }
            else{
                System.out.println("registration failed");
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        finally {
            countDownLatch.countDown();
        }
    }
}
