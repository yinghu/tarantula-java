package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Event;
import com.tarantula.platform.presence.GameCluster;

import java.io.IOException;

/**
 * Created by yinghu lu on 5/24/2020.
 */
public class GameClusterLaunchEvent extends Data implements Event {


    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.GAME_CLUSTER_LAUNCH_EVENT_CID;
    }

    public GameClusterLaunchEvent(){

    }
    public GameClusterLaunchEvent(String destination, String gameClusterId){
        this.destination = destination;
        this.trackId = gameClusterId;
    }

    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.destination);
        out.writeUTF("2",this.trackId);

    }

    public void readPortable(PortableReader in) throws IOException {
        this.destination = in.readUTF("1");
        this.trackId = in.readUTF("2");
    }

    @Override
    public String toString(){
        return "Game cluster launch Event ["+trackId+"]";
    }
}
