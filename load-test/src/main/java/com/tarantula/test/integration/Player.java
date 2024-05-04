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
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class Player implements Runnable{

    private CountDownLatch counter;

    private String game;
    private String userName;
    private String deviceName;
    private String clientId;
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

    private boolean udpTested;

    static short STATISTICS_QUERY = 1;
    static short STATISTICS_COMMIT = 2;

    private HttpCaller httpCaller;
    public Player(HttpCaller httpCaller, CountDownLatch counter, String game,String userName,int sequence,boolean udpTested,int udpReceiveTimeout,int udpRounds){
        this.httpCaller = httpCaller;
        this.counter = counter;
        this.game = game;
        this.userName = userName;
        this.deviceName = "test-"+sequence;
        this.clientId = UUID.randomUUID().toString();
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
            LoadResult.totalHttpRequestTime.addAndGet(System.currentTimeMillis()-requestStart);
            LoadResult.totalHttpRequestCount.incrementAndGet();
            if(!onPresence(resp)) {
                LoadResult.totalFailureRegister.incrementAndGet();
            }else{
                LoadResult.totalSuccessRegister.incrementAndGet();
            }
            headers = new String[]{
                    Session.TARANTULA_TAG,game+"/lobby",
                    Session.TARANTULA_ACTION,"onPlay",
                    Session.TARANTULA_TOKEN,token,
                    Session.TARANTULA_NAME,deviceName,
                    Session.TARANTULA_CLIENT_ID, deviceName,
                    Session.TARANTULA_TOURNAMENT_ID, "n/a",
                    Session.TARANTULA_ACCESS_KEY,
                    "570342964778242048-6879508E047E7B7BF8A50EF2951A3A198EE81957-FA80A8B8A29B7C4C4D636032D4BFA571",
            };
            requestStart = System.currentTimeMillis();
            resp = httpCaller.get("service/action",headers);
            LoadResult.totalHttpRequestTime.addAndGet(System.currentTimeMillis()-requestStart);
            LoadResult.totalHttpRequestCount.incrementAndGet();
            onJoin(resp);
            //createProfile();
            //saveOnSet();
            //coinForm();
            //loadShop();
            //onStartGame();
            //onUpdateGameScoreTournament();
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
        LoadResult.totalHttpRequestTime.addAndGet(System.currentTimeMillis()-requestStart);
        LoadResult.totalHttpRequestCount.incrementAndGet();
        JsonObject json = JsonUtil.parse(resp);
        boolean suc = json.get("Successful").getAsBoolean();
        if(suc){
            LoadResult.totalSuccessLeave.incrementAndGet();
        }
        else {
            LoadResult.totalFailureLeave.incrementAndGet();
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

    private void onJoin(String resp) throws Exception{
        JsonObject joinPayload = JsonUtil.parse(resp);
        boolean suc = joinPayload.get("Successful").getAsBoolean();
        if(!suc){
            System.out.println(resp);
            LoadResult.totalFailureJoin.incrementAndGet();
            throw new RuntimeException("failed");
        }
        LoadResult.totalSuccessJoin.incrementAndGet();
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

    private void createProfile() {
        try{
            String[] headers = new String[]{
                    Session.TARANTULA_TAG,game+"/save",
                    Session.TARANTULA_ACTION,"onUpdateProfile",
                    Session.TARANTULA_TOKEN,token,
                    Session.TARANTULA_NAME,deviceName,
                    Session.TARANTULA_CLIENT_ID,clientId
            };
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("DisplayName",userName);
            jsonObject.addProperty("IconIndex",0);
            long requestStart = System.currentTimeMillis();
            String resp = httpCaller.post("service/action",jsonObject.toString().getBytes(),headers);
            LoadResult.totalHttpRequestTime.addAndGet(System.currentTimeMillis()-requestStart);
            LoadResult.totalHttpRequestCount.incrementAndGet();
            JsonObject json = JsonUtil.parse(resp);
            boolean suc = json.get("Successful").getAsBoolean();
            if(!suc) {
                LoadResult.totalFailureCreateProfile.incrementAndGet();
            }else{
                LoadResult.totalSuccessCreateProfile.incrementAndGet();
                fetchProfile();
            }
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

    private void fetchProfile() {
        try{
            String[] headers = new String[]{
                    Session.TARANTULA_TAG,game+"/save",
                    Session.TARANTULA_ACTION,"onFetchProfile",
                    Session.TARANTULA_TOKEN,token,
                    Session.TARANTULA_NAME,systemId,
                    Session.TARANTULA_CLIENT_ID,clientId
            };
            long requestStart = System.currentTimeMillis();
            String resp = httpCaller.get("service/action",headers);
            LoadResult.totalHttpRequestTime.addAndGet(System.currentTimeMillis()-requestStart);
            LoadResult.totalHttpRequestCount.incrementAndGet();
            if(!isValidProfile(resp)) {
                LoadResult.totalFailureFetchProfile.incrementAndGet();
            }else{
                LoadResult.totalSuccessFetchProfile.incrementAndGet();
            }
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

    private boolean isValidProfile(String resp) {
        JsonObject json = JsonUtil.parse(resp);
        var jsonArray = json.getAsJsonArray("_profileList");
        if (!jsonArray.isEmpty()) {
            var profile = jsonArray.get(0);
            return userName.equals(profile.getAsJsonObject().get("DisplayName").getAsString());
        }
        return false;
    }

    private void loadShop() {
        try{
            String[] headers = new String[]{
                    Session.TARANTULA_TAG,game+"/store",
                    Session.TARANTULA_ACTION,"onList",
                    Session.TARANTULA_TOKEN,token,
                    Session.TARANTULA_NAME,"tami",
                    Session.TARANTULA_CLIENT_ID,clientId
            };
            long requestStart = System.currentTimeMillis();
            String resp = httpCaller.get("service/action",headers);
            LoadResult.totalHttpRequestTime.addAndGet(System.currentTimeMillis()-requestStart);
            LoadResult.totalHttpRequestCount.incrementAndGet();
            JsonObject json = JsonUtil.parse(resp);
            boolean suc = json.get("Successful").getAsBoolean();
            if(!suc) {
                LoadResult.totalFailureLoadShop.incrementAndGet();
            }else{
                LoadResult.totalSuccessLoadShop.incrementAndGet();
            }
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

    private void saveOnSet() {
        try{
            String[] headers = new String[]{
                    Session.TARANTULA_TAG,game+"/save",
                    Session.TARANTULA_ACTION,"onSet",
                    Session.TARANTULA_TOKEN,token,
                    Session.TARANTULA_NAME,userName,
                    Session.TARANTULA_CLIENT_ID,clientId
            };
            long requestStart = System.currentTimeMillis();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name",userName);
            String resp = httpCaller.post("service/action",jsonObject.toString().getBytes(),headers);
            LoadResult.totalHttpRequestTime.addAndGet(System.currentTimeMillis()-requestStart);
            LoadResult.totalHttpRequestCount.incrementAndGet();
            JsonObject json = JsonUtil.parse(resp);
            boolean suc = json.get("Successful").getAsBoolean();
            if(!suc) {
                LoadResult.totalFailureSaveOnSet.incrementAndGet();
            }else{
                LoadResult.totalSuccessSaveOnSet.incrementAndGet();
                saveOnGet();
            }
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

    private void saveOnGet() {
        try{
            String[] headers = new String[]{
                    Session.TARANTULA_TAG,game+"/save",
                    Session.TARANTULA_ACTION,"onGet",
                    Session.TARANTULA_TOKEN,token,
                    Session.TARANTULA_NAME,userName,
                    Session.TARANTULA_CLIENT_ID,clientId
            };
            long requestStart = System.currentTimeMillis();
            String resp = httpCaller.get("service/action",headers);
            LoadResult.totalHttpRequestTime.addAndGet(System.currentTimeMillis()-requestStart);
            LoadResult.totalHttpRequestCount.incrementAndGet();
            System.out.println("resp is " + resp);
            JsonObject json = JsonUtil.parse(resp);
            boolean suc = userName.equals(json.get("name").getAsString());
            if(!suc) {
                LoadResult.totalFailureSaveOnSet.incrementAndGet();
            }else{
                LoadResult.totalSuccessSaveOnSet.incrementAndGet();
            }
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

    private void onStartGame() {
        try{
            String[] headers = new String[]{
                    Session.TARANTULA_ACTION,"onStartGame",
                    Session.TARANTULA_TOKEN,token,
                    Session.TARANTULA_NAME,deviceName,
                    Session.TARANTULA_CLIENT_ID,clientId,
                    Session.TARANTULA_TAG,game+"/lobby1",
            };
            var payload = "{\"ChapterId\":0,\"StageId\":0,\"Party\":[0,0,0,0,0],\"AutoBattle\":false,\"Difficulty\":\"Normal\",\"CampaignName\":\"Campaign\",\"DungeonChapterName\":\"ChapterOne\",\"StageNumber\":1,\"analytics\":[{\"currencyDelta\":-6,\"currencyId\":\"Energy\",\"currencyTotal\":214,\"context\":\"CampaignBattleStart\",\"messageType\":\"currencyUpdate\",\"messageCategory\":\"inventory\"}],\"TEMP_ChapterName\":\"ChapterOneConfig\",\"TEMP_StageName\":\"Chapter1_Stage1Config\",\"TEMP_StringParty\":[\"FelixUnitConfig\",\"GusUnitConfig\",\"MaxineUnitConfig\",\"MarkIVUnitConfig\",\"TerrenceUnitConfig\"]}";
            long requestStart = System.currentTimeMillis();
            String resp = httpCaller.post("service/action",payload.getBytes(),headers);
            LoadResult.totalHttpRequestTime.addAndGet(System.currentTimeMillis()-requestStart);
            LoadResult.totalHttpRequestCount.incrementAndGet();
            JsonObject json = JsonUtil.parse(resp);
            boolean suc = json.get("Successful").getAsBoolean();
            if(!suc) {
                LoadResult.totalFailureStartGame.incrementAndGet();
            }else{
                LoadResult.totalSuccessStartGame.incrementAndGet();
                onEndGame(json.get("BattleId").getAsString());
            }
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

    private void onUpdateGameScoreTournament() {
        try{
            String[] headers = new String[]{
                    Session.TARANTULA_ACTION,"onUpdateGame",
                    Session.TARANTULA_TOKEN,token,
                    Session.TARANTULA_NAME,deviceName,
                    Session.TARANTULA_CLIENT_ID,clientId,
                    Session.TARANTULA_TAG,game+"/lobby1",
            };
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("PlayerLevel",10);
            jsonObject.addProperty("Score",10);
            jsonObject.addProperty("UnitId",0);
            jsonObject.addProperty("UpdateId",0);
            jsonObject.addProperty("EquipmentId",0);
            long requestStart = System.currentTimeMillis();
            String resp = httpCaller.post("service/action",jsonObject.toString().getBytes(),headers);
            LoadResult.totalHttpRequestTime.addAndGet(System.currentTimeMillis()-requestStart);
            LoadResult.totalHttpRequestCount.incrementAndGet();
            JsonObject json = JsonUtil.parse(resp);
            boolean suc = json.get("Successful").getAsBoolean();
            if(!suc) {
                LoadResult.totalFailureScoreTournament.incrementAndGet();
            }else{
                LoadResult.totalSuccessScoreTournament.incrementAndGet();
            }
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

    private void onEndGame(String battleId) {
        try{
            String[] headers = new String[]{
                    Session.TARANTULA_ACTION,"onEndGame",
                    Session.TARANTULA_TOKEN,token,
                    Session.TARANTULA_NAME,deviceName,
                    Session.TARANTULA_CLIENT_ID,clientId,
                    Session.TARANTULA_TAG,game+"/lobby1",
            };
            var payload = "{\"BattleId\":" + battleId + ",\"Win\":true,\"EndedWithFastSpeed\":false,\"StarsEarned\":3,\"analytics\":[{\"currencyDelta\":540,\"currencyId\":\"SoftCurrency\",\"currencyTotal\":145540,\"context\":\"CampaignBattleEnd\",\"messageType\":\"currencyUpdate\",\"messageCategory\":\"inventory\"},{\"currencyDelta\":1,\"currencyId\":\"FelixCoin\",\"currencyTotal\":1,\"context\":\"CampaignBattleEnd\",\"messageType\":\"currencyUpdate\",\"messageCategory\":\"inventory\"},{\"currencyDelta\":2,\"currencyId\":\"EarthPotion\",\"currencyTotal\":2,\"context\":\"CampaignBattleEnd\",\"messageType\":\"currencyUpdate\",\"messageCategory\":\"inventory\"},{\"level\":1,\"totalXp\":18,\"xpGain\":18,\"messageType\":\"playerXpUp\",\"messageCategory\":\"player\"}]}";
            long requestStart = System.currentTimeMillis();
            String resp = httpCaller.post("service/action",payload.getBytes(),headers);
            LoadResult.totalHttpRequestTime.addAndGet(System.currentTimeMillis()-requestStart);
            LoadResult.totalHttpRequestCount.incrementAndGet();
            JsonObject json = JsonUtil.parse(resp);
            boolean suc = json.get("Successful").getAsBoolean();
            if(!suc) {
                LoadResult.totalFailureEndGame.incrementAndGet();
            }else{
                LoadResult.totalSuccessEndGame.incrementAndGet();
            }
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

    private void coinForm() {
        try{
            String[] headers = new String[]{
                    Session.TARANTULA_ACTION,"onGameClusterEvent",
                    Session.TARANTULA_MAGIC_KEY,userName,
                    Session.TARANTULA_NAME, clientId + "#ShippingFormCompleted",
                    Session.TARANTULA_ACCESS_KEY,
                    "570342964778242048-6879508E047E7B7BF8A50EF2951A3A198EE81957-FA80A8B8A29B7C4C4D636032D4BFA571",
            };
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("tournament_type",UUID.randomUUID().toString());
            long requestStart = System.currentTimeMillis();
            String resp = httpCaller.post("server",jsonObject.toString().getBytes(),headers);
            LoadResult.totalHttpRequestTime.addAndGet(System.currentTimeMillis()-requestStart);
            LoadResult.totalHttpRequestCount.incrementAndGet();
            JsonObject json = JsonUtil.parse(resp);
            boolean suc = json.get("successful").getAsBoolean();
            if(!suc) {
                LoadResult.totalFailureCoinForm.incrementAndGet();
            }else{
                LoadResult.totalSuccessCoinForm.incrementAndGet();
            }
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
}
