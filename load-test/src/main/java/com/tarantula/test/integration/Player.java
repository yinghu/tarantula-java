package com.tarantula.test.integration;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.protocol.Messenger;
import com.icodesoftware.util.CipherUtil;
import com.icodesoftware.util.HttpCaller;
import com.icodesoftware.util.JsonUtil;

import javax.crypto.Cipher;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;

public class Player implements Runnable{

    private CountDownLatch counter;
    private String game;
    private String userName;

    private String token;
    private String systemId;
    private String ticket;

    private String tag;

    private Cipher encryper;
    private DatagramSocket udp;
    private MessageBuffer messageBuffer;
    private MessageBuffer.MessageHeader messageHeader;

    private int udpReceiveTimeout = 3000; //udp receive timeout 3 secs

    private int udpRounds = 10; //total udp rounds

    private boolean joined;

    private long battleId;
    private long tournamentId;

    private boolean udpTested;

    static short STATISTICS_QUERY = 1;
    static short STATISTICS_COMMIT = 2;

    private HttpCaller httpCaller;
    public Player(HttpCaller httpCaller, CountDownLatch counter, String game,String userName,boolean udpTested,int udpReceiveTimeout,int udpRounds){
        this.httpCaller = httpCaller;
        this.counter = counter;
        this.game = game;
        this.userName = userName;
        this.udpTested = udpTested;
        this.udpReceiveTimeout = udpReceiveTimeout;
        this.udpRounds = udpRounds;
    }

    private boolean onPresence(String payload){
        JsonObject json = JsonUtil.parse(payload);
        boolean suc = json.get("Successful").getAsBoolean();
        if(!suc) return false;
        token = json.get("Token").getAsString();
        systemId = json.get("SystemId").getAsString();
        //ticket = json.get("Ticket").getAsString();
        return true;
    }

    private void join() {
        try{
            String[] headers = new String[]{
                    Session.TARANTULA_TAG,"index/user",
                    Session.TARANTULA_ACTION,"onDevice",
                    Session.TARANTULA_MAGIC_KEY,userName,
            };
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("DeviceId",userName);
            long requestStart = System.currentTimeMillis();
            String resp = httpCaller.post("user/action",jsonObject.toString().getBytes(),headers);
            long delta = System.currentTimeMillis()-requestStart;
            LoadResult.totalHttpRequestTime.addAndGet(delta);
            LoadResult.totalHttpRequestCount.incrementAndGet();
            RequestResult result = LoadResult.requestResult("onDevice");
            if(!onPresence(resp)) {
                result.totalFailure.incrementAndGet();
                result.totalTimed.addAndGet(delta);
                throw new RuntimeException(resp);
            }else{
                result.totalSuccess.incrementAndGet();
                result.totalTimed.addAndGet(delta);
            }
            saveOnSet(Main.inventoryKey);
            Thread.sleep(Main.httpRequestInterval);
            saveOnGet(Main.inventoryKey);
            Thread.sleep(Main.httpRequestInterval);
            createProfile();
            Thread.sleep(Main.httpRequestInterval);
            fetchProfile();
            Thread.sleep(Main.httpRequestInterval);
            loadShop();
            Thread.sleep(Main.httpRequestInterval);
            loadTournament();
            Thread.sleep(Main.httpRequestInterval);
            onUpdateGame();
            Thread.sleep(Main.httpRequestInterval);
            headers = new String[]{
                    Session.TARANTULA_TAG,game+"/lobby",
                    Session.TARANTULA_ACTION,"onPlay",
                    Session.TARANTULA_TOKEN,token,
            };
            requestStart = System.currentTimeMillis();
            resp = httpCaller.get("service/action",headers);
            result = LoadResult.requestResult("onPlay");
            delta = System.currentTimeMillis()-requestStart;
            LoadResult.totalHttpRequestTime.addAndGet(delta);
            LoadResult.totalHttpRequestCount.incrementAndGet();
            result.totalTimed.addAndGet(delta);
            onJoin(resp,result);
            Thread.sleep(Main.httpRequestInterval);
            onStartGame();
            Thread.sleep(Main.httpRequestInterval);
            for(int i=0;i<Main.playerUpdateRound;i++){
                onUpdateGameScoreTournament();
                Thread.sleep(Main.httpRequestInterval);
                loadRaceBoard();
                Thread.sleep(Main.httpRequestInterval);
                saveOnSet(Main.campaignKey);
                Thread.sleep(Main.httpRequestInterval);
                saveOnGet(Main.campaignKey);
            }
            Thread.sleep(Main.httpRequestInterval);
            onGameEvent();
            Thread.sleep(Main.httpRequestInterval);
            onEndGame();
        }catch (Exception ex){
            ex.printStackTrace();
            String error = ex.getMessage();
            if(error==null || !error.equals("failed")){
                LoadResult.totalFailureOther.incrementAndGet();
            }
            udpRounds = 0;
            counter.countDown();
            joined = false;
        }
    }

    private void leave() throws Exception{
        if(!joined) return;
        String[] headers = new String[]{
                Session.TARANTULA_TAG,tag,
                Session.TARANTULA_ACTION,"onLeave",
                Session.TARANTULA_TOKEN,token
        };
        long requestStart = System.currentTimeMillis();
        String resp = httpCaller.get("service/action",headers);
        long delta = System.currentTimeMillis() - requestStart;
        LoadResult.totalHttpRequestTime.addAndGet(delta);
        LoadResult.totalHttpRequestCount.incrementAndGet();
        RequestResult result = LoadResult.requestResult("onLeave");
        result.totalTimed.addAndGet(delta);
        JsonObject json = JsonUtil.parse(resp);
        boolean suc = json.get("Successful").getAsBoolean();
        if(suc){
            result.totalSuccess.incrementAndGet();
        }
        else {
            result.totalFailure.incrementAndGet();
        }
    }

    public void run(){
        join();
        try{
            if(joined && udpTested){
                while (udpRounds>0){
                    messageHeader.commandId = Messenger.REQUEST;
                    messageHeader.encrypted = false;
                    messageBuffer.reset();
                    messageBuffer.writeHeader(messageHeader);
                    messageBuffer.writeShort(STATISTICS_COMMIT);
                    messageBuffer.writeUTF8("kills");
                    messageBuffer.writeDouble(1);
                    messageBuffer.flip();
                    byte[] outbound = messageBuffer.toArray();
                    for (int i=0;i<10;i++){
                        long udpStart = System.currentTimeMillis();
                        udp.send(new DatagramPacket(outbound,outbound.length));
                        LoadResult.totalUDPSentTime.addAndGet(System.currentTimeMillis()-udpStart);
                        LoadResult.totalUDPBytesSent.addAndGet(outbound.length);
                        LoadResult.totalSuccessUDPSent.incrementAndGet();
                        Thread.sleep(1);
                    }
                    messageHeader.commandId = Messenger.REQUEST;
                    messageBuffer.reset();
                    messageBuffer.writeHeader(messageHeader);
                    messageBuffer.writeShort(STATISTICS_QUERY);
                    messageBuffer.flip();
                    outbound = messageBuffer.toArray();
                    udp.send(new DatagramPacket(outbound,outbound.length));
                    LoadResult.totalUDPBytesSent.addAndGet(outbound.length);
                    LoadResult.totalSuccessUDPReceived.incrementAndGet();
                    for(int i=0; i<5; i++){
                        DatagramPacket d = new DatagramPacket(new byte[MessageBuffer.PAYLOAD_SIZE],MessageBuffer.PAYLOAD_SIZE);
                        long udpStart = System.currentTimeMillis();
                        try{
                            udp.receive(d);
                        }catch (Exception udpEx){
                            LoadResult.totalUDPReceiveTimeout.incrementAndGet();
                        }
                        LoadResult.totalSuccessUDPReceived.incrementAndGet();
                        LoadResult.totalUDPReceiveTime.addAndGet(System.currentTimeMillis()-udpStart);
                        byte[] inbound = d.getData();
                        messageBuffer.reset(inbound);
                        messageBuffer.flip();
                        MessageBuffer.MessageHeader h = messageBuffer.readHeader();
                        LoadResult.totalUDPBytesReceived.addAndGet(inbound.length);
                        if(h.batch == h.batchSize){
                            break;
                        }
                    }
                    Thread.sleep(10);
                    udpRounds--;
                }
            }
            leave();
            LoadResult.totalRounds.incrementAndGet();
            counter.countDown();
        }catch (Exception ex){
            ex.printStackTrace();
            counter.countDown();
        }
    }

    private void onJoin(String resp,RequestResult result) throws Exception{
        JsonObject joinPayload = JsonUtil.parse(resp);
        boolean suc = joinPayload.get("Successful").getAsBoolean();
        if(!suc){
            result.totalFailure.incrementAndGet();
            throw new RuntimeException(resp);
        }
        result.totalSuccess.incrementAndGet();
        joined = true;
        tag = joinPayload.get("Tag").getAsString();
        //ticket = joinPayload.get("Ticket").getAsString();
        if(!udpTested) return;
        JsonObject channel = joinPayload.get("_pushChannel").getAsJsonObject();
        byte[] serverKey = Base64.getDecoder().decode(channel.get("ServerKey").getAsString());
        this.encryper = CipherUtil.encrypt(serverKey);
        udp = new DatagramSocket();
        udp.setSoTimeout(udpReceiveTimeout);
        int channelId = channel.get("ChannelId").getAsInt();
        int sessionId = channel.get("SessionId").getAsInt();
        JsonObject _conn = channel.get("_connection").getAsJsonObject();
        String host = _conn.get("Host").getAsString();
        int port = _conn.get("Port").getAsInt();
        InetAddress addr = InetAddress.getByName(host);
        udp.connect(new InetSocketAddress(addr,port));
        messageHeader = new MessageBuffer.MessageHeader();
        messageHeader.channelId = channelId;
        messageHeader.sessionId = sessionId;
        messageHeader.commandId = Messenger.JOIN;
        messageHeader.encrypted = true;
        messageBuffer = new MessageBuffer();
        messageBuffer.writeHeader(messageHeader);
        messageBuffer.writeInt(sessionId);
        messageBuffer.writeUTF8(ticket);
        messageBuffer.flip();
        messageBuffer.readHeader();
        byte[] payload = this.encryper.doFinal(messageBuffer.readPayload());
        messageBuffer.reset();
        messageBuffer.writeHeader(messageHeader);
        messageBuffer.writePayload(payload);
        messageBuffer.flip();
        byte[] outbound = messageBuffer.toArray();
        udp.send(new DatagramPacket(outbound,0,outbound.length));
        LoadResult.totalUDPBytesSent.addAndGet(outbound.length);
        LoadResult.totalSuccessUDPSent.incrementAndGet();
        DatagramPacket rec = new DatagramPacket(new byte[MessageBuffer.PAYLOAD_SIZE],MessageBuffer.PAYLOAD_SIZE);
        try {
            udp.receive(rec);
        }catch (Exception rex){
            LoadResult.totalUDPReceiveTimeout.incrementAndGet();
            LoadResult.totalFailurePlay.incrementAndGet();
            leave();
            throw new RuntimeException("failed");
        }
        LoadResult.totalSuccessUDPReceived.incrementAndGet();
        byte[] inbound = rec.getData();
        LoadResult.totalUDPBytesReceived.addAndGet(inbound.length);
        messageBuffer.reset(inbound);
        messageBuffer.flip();
        MessageBuffer.MessageHeader h = messageBuffer.readHeader();
        int returnSessionId = messageBuffer.readInt();
        if(h.commandId == Messenger.ON_JOIN && returnSessionId == sessionId ){
            LoadResult.totalSuccessPlay.incrementAndGet();
            return;
        }
        leave();
        LoadResult.totalFailurePlay.incrementAndGet();
        throw new RuntimeException("failed");
    }

    private void createProfile() throws Exception{
        String[] headers = new String[]{
                Session.TARANTULA_TAG,game+"/save",
                Session.TARANTULA_ACTION,"onUpdateProfile",
                Session.TARANTULA_TOKEN,token,
        };
        JsonObject jsonObject = new JsonObject();
        int iconIndex = Main.index();
        jsonObject.addProperty("DisplayName",Main.displayNames[iconIndex]);
        jsonObject.addProperty("IconIndex",iconIndex);
        long requestStart = System.currentTimeMillis();
        String resp = httpCaller.post("service/action",jsonObject.toString().getBytes(),headers);
        long delta = System.currentTimeMillis()-requestStart;
        RequestResult result = LoadResult.requestResult("onUpdateProfile");
        LoadResult.totalHttpRequestTime.addAndGet(delta);
        LoadResult.totalHttpRequestCount.incrementAndGet();
        JsonObject json = JsonUtil.parse(resp);
        boolean suc = json.get("Successful").getAsBoolean();
        if(!suc) {
            result.totalFailure.incrementAndGet();
            result.totalTimed.addAndGet(delta);
            LoadResult.totalFailureCreateProfile.incrementAndGet();
        }else{
            result.totalSuccess.incrementAndGet();
            result.totalTimed.addAndGet(delta);
            LoadResult.totalSuccessCreateProfile.incrementAndGet();
        }
    }

    private void fetchProfile() throws Exception{

            String[] headers = new String[]{
                    Session.TARANTULA_TAG,game+"/save",
                    Session.TARANTULA_ACTION,"onFetchProfile",
                    Session.TARANTULA_TOKEN,token,
                    Session.TARANTULA_NAME,systemId,
            };
            long requestStart = System.currentTimeMillis();
            String resp = httpCaller.get("service/action",headers);
            long delta = System.currentTimeMillis()-requestStart;
            LoadResult.totalHttpRequestTime.addAndGet(delta);
            LoadResult.totalHttpRequestCount.incrementAndGet();
            RequestResult result = LoadResult.requestResult("onFetchProfile");
            result.totalTimed.addAndGet(delta);
            if(!isValidProfile(resp)) {
                result.totalFailure.incrementAndGet();
                //throw new RuntimeException(resp);
            }else{
                result.totalSuccess.incrementAndGet();
            }
    }

    private boolean isValidProfile(String resp) {
        JsonObject json = JsonUtil.parse(resp);
        var jsonArray = json.getAsJsonArray("_profileList");
        return !jsonArray.isEmpty();
    }

    private void loadShop() throws Exception{
        String[] headers = new String[]{
                Session.TARANTULA_TAG,game+"/store",
                Session.TARANTULA_ACTION,"onList",
                Session.TARANTULA_TOKEN,token,
                Session.TARANTULA_NAME,"Tami",
        };
        long requestStart = System.currentTimeMillis();
        String resp = httpCaller.get("service/action",headers);
        long delta = System.currentTimeMillis()-requestStart;
        LoadResult.totalHttpRequestTime.addAndGet(delta);
        LoadResult.totalHttpRequestCount.incrementAndGet();
        RequestResult result = LoadResult.requestResult("onLoadShop");
        result.totalTimed.addAndGet(delta);
        JsonObject json = JsonUtil.parse(resp);
        boolean suc = json.get("Successful").getAsBoolean() && !json.get("_shoppingItemList").getAsJsonArray().isEmpty();
        if(!suc) {
            result.totalFailure.incrementAndGet();
            //throw new RuntimeException(resp);
        }else{
            result.totalSuccess.incrementAndGet();
        }
    }

    private void loadTournament() throws Exception{
        String[] headers = new String[]{
                Session.TARANTULA_TAG,game+"/tournament",
                Session.TARANTULA_ACTION,"onList",
                Session.TARANTULA_TOKEN,token,
                Session.TARANTULA_NAME,"Hero",
        };
        long requestStart = System.currentTimeMillis();
        String resp = httpCaller.get("service/action",headers);
        long delta = System.currentTimeMillis()-requestStart;
        LoadResult.totalHttpRequestTime.addAndGet(delta);
        LoadResult.totalHttpRequestCount.incrementAndGet();
        RequestResult result = LoadResult.requestResult("onLoadTournament");
        result.totalTimed.addAndGet(delta);
        JsonObject json = JsonUtil.parse(resp);
        boolean suc = json.get("Successful").getAsBoolean() && !json.get("_tournamentList").getAsJsonArray().isEmpty();
        if(!suc) {
            result.totalFailure.incrementAndGet();
            //throw new RuntimeException(resp);
        }else{
            JsonObject tournament = json.get("_tournamentList").getAsJsonArray().get(0).getAsJsonObject();
            tournamentId = tournament.get("TournamentId").getAsLong();
            result.totalSuccess.incrementAndGet();
        }
    }

    private void loadRaceBoard() throws Exception{
        if(tournamentId==0){
            LoadResult.totalFailureLoadTournamentRaceBoard.incrementAndGet();
            return;
        }
        String[] headers = new String[]{
                Session.TARANTULA_TAG,game+"/tournament",
                Session.TARANTULA_ACTION,"onBoard",
                Session.TARANTULA_TOKEN,token,
                Session.TARANTULA_NAME,""+tournamentId,
        };
        long requestStart = System.currentTimeMillis();
        String resp = httpCaller.get("service/action",headers);
        long delta = System.currentTimeMillis()-requestStart;
        LoadResult.totalHttpRequestTime.addAndGet(delta);
        LoadResult.totalHttpRequestCount.incrementAndGet();
        RequestResult result = LoadResult.requestResult("onLoadTournamentRaceBoard");
        result.totalTimed.addAndGet(delta);
        JsonObject json = JsonUtil.parse(resp);
        boolean suc = json.get("Successful").getAsBoolean() && json.get("_raceBoard")!=null && json.get("_myRaceBoard")!=null;
        if(!suc) {
            result.totalFailure.incrementAndGet();
            //throw new RuntimeException(resp);
        }else{
            result.totalSuccess.incrementAndGet();
        }
    }

    private void saveOnSet(String key) throws Exception{

        String[] headers = new String[]{
                Session.TARANTULA_TAG,game+"/save",
                Session.TARANTULA_ACTION,"onSet",
                Session.TARANTULA_TOKEN,token,
                Session.TARANTULA_NAME,key
        };
        long requestStart = System.currentTimeMillis();
        JsonObject jsonObject = JsonUtil.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream(key+".json"));
        String resp = httpCaller.post("service/action",jsonObject.toString().getBytes(),headers);
        long delta = System.currentTimeMillis()-requestStart;
        RequestResult result = LoadResult.requestResult("onSet");
        LoadResult.totalHttpRequestTime.addAndGet(delta);
        LoadResult.totalHttpRequestCount.incrementAndGet();
        JsonObject json = JsonUtil.parse(resp);
        boolean suc = json.get("Successful").getAsBoolean();
        if(!suc) {
            result.totalFailure.incrementAndGet();
            result.totalTimed.addAndGet(delta);
            //throw new RuntimeException(resp);
        }else{
            result.totalSuccess.incrementAndGet();
            result.totalTimed.addAndGet(delta);
        }
    }

    private void saveOnGet(String key) throws Exception{
        String[] headers = new String[]{
                Session.TARANTULA_TAG,game+"/save",
                Session.TARANTULA_ACTION,"onGet",
                Session.TARANTULA_TOKEN,token,
                Session.TARANTULA_NAME,key
        };
        long requestStart = System.currentTimeMillis();
        String resp = httpCaller.get("service/action",headers);
        long delta = System.currentTimeMillis()-requestStart;
        LoadResult.totalHttpRequestTime.addAndGet(delta);
        LoadResult.totalHttpRequestCount.incrementAndGet();
        RequestResult result = LoadResult.requestResult("onGet");
        //JsonObject json = JsonUtil.parse(resp);
        boolean suc = resp.length()>2000;
        if(!suc) {
            result.totalFailure.incrementAndGet();
            result.totalTimed.addAndGet(delta);
            //throw new RuntimeException(resp);
        }else{
            result.totalSuccess.incrementAndGet();
            result.totalTimed.addAndGet(delta);
        }
    }


    private void onUpdateGame() throws Exception{
        String[] headers = new String[]{
                Session.TARANTULA_ACTION,"onUpdateGame",
                Session.TARANTULA_TOKEN,token,
                Session.TARANTULA_TAG,game+"/lobby",
        };
        JsonObject jsonObject = JsonUtil.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream("updateGame.json"));
        long requestStart = System.currentTimeMillis();
        String resp = httpCaller.post("service/action",jsonObject.toString().getBytes(),headers);
        long delta = System.currentTimeMillis()-requestStart;
        LoadResult.totalHttpRequestTime.addAndGet(delta);
        LoadResult.totalHttpRequestCount.incrementAndGet();
        RequestResult result = LoadResult.requestResult("onUpdateGame");
        result.totalTimed.addAndGet(delta);
        JsonObject json = JsonUtil.parse(resp);
        boolean suc = json.get("Successful").getAsBoolean();
        if(!suc) {
            result.totalFailure.incrementAndGet();
            //throw new RuntimeException(resp);
        }else{
            result.totalSuccess.incrementAndGet();
        }
    }

    private void onStartGame() throws Exception{

        String[] headers = new String[]{
                Session.TARANTULA_ACTION,"onStartGame",
                Session.TARANTULA_TOKEN,token,
                Session.TARANTULA_TAG,game+"/lobby1",
        };
        JsonObject payload = JsonUtil.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream("updateGame.json"));
        long requestStart = System.currentTimeMillis();
        String resp = httpCaller.post("service/action",payload.toString().getBytes(),headers);
        long delta = System.currentTimeMillis() - requestStart;
        LoadResult.totalHttpRequestTime.addAndGet(delta);
        LoadResult.totalHttpRequestCount.incrementAndGet();
        RequestResult result = LoadResult.requestResult("onStartGame");
        result.totalTimed.addAndGet(delta);
        JsonObject json = JsonUtil.parse(resp);
        boolean suc = json.get("Successful").getAsBoolean() && json.get("BattleId").getAsLong()>0;
        if(!suc) {
            result.totalFailure.incrementAndGet();
            //throw new RuntimeException(resp);
        }else{
            battleId = json.get("BattleId").getAsLong();
            result.totalSuccess.incrementAndGet();
        }
    }

    private void onUpdateGameScoreTournament() throws Exception{
        String[] headers = new String[]{
                Session.TARANTULA_ACTION,"onUpdateGame",
                Session.TARANTULA_TOKEN,token,
                Session.TARANTULA_TAG,game+"/lobby1",
        };
        JsonObject jsonObject = JsonUtil.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream("updateGame.json"));
        jsonObject.addProperty("PlayerLevel",Main.rng.onNext(19));
        jsonObject.addProperty("Score",Main.rng.onNext(2000));
        long requestStart = System.currentTimeMillis();
        String resp = httpCaller.post("service/action",jsonObject.toString().getBytes(),headers);
        LoadResult.totalHttpRequestTime.addAndGet(System.currentTimeMillis()-requestStart);
        LoadResult.totalHttpRequestCount.incrementAndGet();
        JsonObject json = JsonUtil.parse(resp);
        boolean suc = json.get("Successful").getAsBoolean();
        if(!suc) {
            LoadResult.totalFailureScoreTournament.incrementAndGet();
            //throw new RuntimeException(resp);
        }else{
            LoadResult.totalSuccessScoreTournament.incrementAndGet();
        }
    }

    private void onEndGame() throws Exception {
        if(battleId==0){
            LoadResult.totalFailureEndGame.incrementAndGet();
            return;
        }
        String[] headers = new String[]{
                Session.TARANTULA_ACTION,"onEndGame",
                Session.TARANTULA_TOKEN,token,
                Session.TARANTULA_TAG,game+"/lobby1",
        };
        JsonObject payload = JsonUtil.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream("endGame.json"));
        payload.addProperty("BattleId",battleId);
        long requestStart = System.currentTimeMillis();
        String resp = httpCaller.post("service/action",payload.toString().getBytes(),headers);
        long delta = System.currentTimeMillis() - requestStart;
        LoadResult.totalHttpRequestTime.addAndGet(delta);
        LoadResult.totalHttpRequestCount.incrementAndGet();
        RequestResult result = LoadResult.requestResult("onEndGame");
        result.totalTimed.addAndGet(delta);
        JsonObject json = JsonUtil.parse(resp);
        boolean suc = json.get("Successful").getAsBoolean();
        if(!suc) {
            result.totalFailure.incrementAndGet();
            //throw new RuntimeException(resp);
        }else{
            result.totalSuccess.incrementAndGet();
        }
    }

    private void onGameEvent() throws Exception{
        String[] headers = new String[]{
                Session.TARANTULA_ACCESS_KEY,
                Main.accessKey,
                Session.TARANTULA_ACTION,
                "onGameClusterEvent",
                Session.TARANTULA_NAME,
                systemId+"#ShippingFormCompleted"
        };
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("tournament_type","Hero");
        long requestStart = System.currentTimeMillis();
        String resp = httpCaller.post("server",jsonObject.toString().getBytes(),headers);
        long delta = System.currentTimeMillis() - requestStart;
        LoadResult.totalHttpRequestTime.addAndGet(delta);
        LoadResult.totalHttpRequestCount.incrementAndGet();
        RequestResult result = LoadResult.requestResult("onGameClusterEvent");
        result.totalTimed.addAndGet(delta);
        JsonObject json = JsonUtil.parse(resp);
        boolean suc = json.get("successful").getAsBoolean();
        if(!suc) {
            result.totalFailure.incrementAndGet();
            //throw new RuntimeException(resp);
        }else{
            result.totalSuccess.incrementAndGet();
        }
    }

}
