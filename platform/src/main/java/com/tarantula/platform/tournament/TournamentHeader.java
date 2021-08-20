package com.tarantula.platform.tournament;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class TournamentHeader extends RecoverableObject implements Tournament, Portable {

    private static final String TOURNAMENT_REGISTER = "register";
    private static final String TOURNAMENT_PLAY = "play";

    protected String type;
    protected String description;
    protected String icon;
    protected Status status = Status.SCHEDULED;
    protected LocalDateTime startTime;
    protected LocalDateTime closeTime;
    protected LocalDateTime endTime;
    protected int maxEntriesPerInstance;
    protected int durationMinutes;

    public IndexSet tournamentRegisterIndex;
    public IndexSet tournamentPlayIndex;
    private ConcurrentHashMap<String,TournamentInstanceHeader> _instanceIndex;
    private ConcurrentLinkedDeque<TournamentRegistry> pendingRegistryQueue;

    public TournamentHeader(Schedule schedule){
        this.type = schedule.type();
        this.description = schedule.description();
        this.icon = schedule.icon();
        this.startTime = schedule.startTime();
        this.closeTime = schedule.closeTime();
        this.endTime = schedule.endTime();
        this.maxEntriesPerInstance = schedule.maxEntriesPerInstance();
        this.durationMinutes = schedule.instanceDurationInMinutes();
    }

    public TournamentHeader(){

    }
    @Override
    public String type() {
        return type;
    }
    @Override
    public void type(String type){
        this.type = type;
    }
    @Override
    public String description() {
        return description;
    }
    @Override
    public String icon() {
        return icon;
    }

    @Override
    public Status status(){
        return status;
    }
    @Override
    public LocalDateTime startTime() {
        return startTime;
    }
    public Map<String,Object> toMap(){
        properties.put("1",type);
        properties.put("2", TimeUtil.toUTCMilliseconds(startTime));
        properties.put("3", TimeUtil.toUTCMilliseconds(closeTime));
        properties.put("4", TimeUtil.toUTCMilliseconds(endTime));
        properties.put("5",maxEntriesPerInstance);
        properties.put("6",durationMinutes);
        return properties;
    }
    public void fromMap(Map<String,Object> properties){
        this.type = (String)properties.get("1");
        this.startTime = TimeUtil.fromUTCMilliseconds(((Number)properties.get("2")).longValue());
        this.closeTime = TimeUtil.fromUTCMilliseconds(((Number)properties.get("3")).longValue());
        this.endTime = TimeUtil.fromUTCMilliseconds(((Number)properties.get("4")).longValue());
        this.maxEntriesPerInstance = ((Number)properties.get("5")).intValue();
        this.durationMinutes = ((Number)properties.get("6")).intValue();
    }
    @Override
    public LocalDateTime closeTime() {
        return closeTime;
    }

    @Override
    public LocalDateTime endTime() {
        return endTime;
    }
    public int maxEntriesPerInstance(){
        return maxEntriesPerInstance;
    }
    public int durationMinutesPerInstance(){
        return durationMinutes;
    }


    @Override
    public String register(String systemId) {
        TournamentRegistry tournamentRegistry = pendingRegistryQueue.poll();
        if(tournamentRegistry==null){
            tournamentRegistry = new TournamentRegistry();
            this.dataStore.create(tournamentRegistry);
            tournamentRegisterIndex.keySet.add(tournamentRegistry.distributionKey());
            tournamentRegisterIndex.update();
        }
        tournamentRegistry.addPlayer(systemId);
        dataStore.update(tournamentRegistry);
        pendingRegistryQueue.offer(tournamentRegistry);
        return tournamentRegistry.instanceId();
    }
    public Tournament.Instance lookup(String instanceId){
        return _instanceIndex.computeIfAbsent(instanceId,(k)->{
            LocalDateTime _startTime = LocalDateTime.now();
            LocalDateTime _closeTime = _startTime.plusMinutes(durationMinutes-3);
            LocalDateTime _endTime = _startTime.plusMinutes(durationMinutes);
            TournamentInstanceHeader instanceHeader = new TournamentInstanceHeader(maxEntriesPerInstance,_startTime,_closeTime,_endTime);
            instanceHeader.distributionKey(instanceId);
            this.dataStore.create(instanceHeader);
            instanceHeader.dataStore(dataStore);
            tournamentPlayIndex.keySet.add(instanceHeader.distributionKey());
            tournamentPlayIndex.update();
            return instanceHeader;
        });
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.TOURNAMENT_CID;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeUTF("1",type);
        portableWriter.writeUTF("2",description);
        portableWriter.writeUTF("3",icon);
        portableWriter.writeLong("4",TimeUtil.toUTCMilliseconds(startTime));
        portableWriter.writeLong("5",TimeUtil.toUTCMilliseconds(closeTime));
        portableWriter.writeLong("6",TimeUtil.toUTCMilliseconds(endTime));
        portableWriter.writeInt("7",durationMinutes);
        portableWriter.writeUTF("8",bucket);
        portableWriter.writeUTF("9",oid);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        this.type = portableReader.readUTF("1");
        this.description = portableReader.readUTF("2");
        this.icon = portableReader.readUTF("3");
        this.startTime = TimeUtil.fromUTCMilliseconds(portableReader.readLong("4"));
        this.closeTime = TimeUtil.fromUTCMilliseconds(portableReader.readLong("5"));
        this.endTime = TimeUtil.fromUTCMilliseconds(portableReader.readLong("6"));
        this.durationMinutes = portableReader.readInt("7");
        this.bucket = portableReader.readUTF("8");
        this.oid = portableReader.readUTF("9");
    }

    public void setup(ConcurrentHashMap<String,TournamentInstanceHeader> instanceIndex){
        this._instanceIndex = instanceIndex;
        this.pendingRegistryQueue = new ConcurrentLinkedDeque();
        tournamentRegisterIndex = new IndexSet(TOURNAMENT_REGISTER);
        tournamentRegisterIndex.distributionKey(this.distributionKey());
        this.dataStore.createIfAbsent(tournamentRegisterIndex,true);
        this.tournamentRegisterIndex.dataStore(dataStore);
        this.tournamentRegisterIndex.keySet.forEach((k)->{
            System.out.println("LOADING  REGISTRY->"+k);
            TournamentRegistry tournamentRegistry = new TournamentRegistry(this.maxEntriesPerInstance);
            tournamentRegistry.distributionKey(k);
            if(this.dataStore.load(tournamentRegistry)){
                if(!tournamentRegistry.fullJoined()){
                    pendingRegistryQueue.offer(tournamentRegistry);
                }
            }
        });
        tournamentPlayIndex = new IndexSet(TOURNAMENT_PLAY);
        tournamentPlayIndex.distributionKey(this.distributionKey());
        this.dataStore.createIfAbsent(tournamentPlayIndex,true);
        tournamentPlayIndex.dataStore(this.dataStore);
        this.tournamentPlayIndex.keySet.forEach((k)->{
            System.out.println("LOADING INSTANCE->"+k);
            TournamentInstanceHeader instanceHeader = new TournamentInstanceHeader();
            instanceHeader.distributionKey(k);
            if(this.dataStore.load(instanceHeader)){
                instanceHeader.dataStore(dataStore);
                instanceHeader.load();
                _instanceIndex.put(k,instanceHeader);
            }
        });
    }

}
