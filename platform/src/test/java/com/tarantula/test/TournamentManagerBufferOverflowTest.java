package com.tarantula.test;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.BufferProxy;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.tournament.TournamentManager;
import com.tarantula.platform.tournament.TournamentSchedule;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;

public class TournamentManagerBufferOverflowTest extends DataStoreHook{
    @Test()
    public void bufferOverflowTest() {
        ByteBuffer scheduleBuffer = ByteBuffer.allocateDirect(700);
        Recoverable.DataBuffer scheduleProxy = BufferProxy.buffer(scheduleBuffer);
        scheduleProxy.writeUTF8("");
        scheduleProxy.writeUTF8("");
        scheduleProxy.writeUTF8("");
        scheduleProxy.writeUTF8("");
        scheduleProxy.writeUTF8("");
        scheduleProxy.writeUTF8("");
        scheduleProxy.writeBoolean(false);
        scheduleProxy.writeUTF8("{" +
                "    \"Global\": true,\n" +
                "    \"NotificationOnFinish\": false,\n" +
                "    \"TargetScore\": \"0\",\n" +
                "    \"Name\": \"Umbranu1Week1_6_20\",\n" +
                "    \"Type\": \"UmbranuHeroEvent\",\n" +
                "    \"Schedule\": 3,\n" +
                "    \"StartTime\": \"2024-10-08T17:00\",\n" +
                "    \"EndTime\": \"2024-10-14T01:00\",\n" +
                "    \"DurationMinutesPerInstance\": \"0\",\n" +
                "    \"MaxEntriesPerInstance\": \"1\",\n" +
                "    \"EnterCost\": \"0\",\n" +
                "    \"Credit\": \"0\",\n" +
                "    \"SegmentsPerSchedule\": \"3\",\n" +
                "    \"StartLevel\": \"6\",\n" +
                "    \"EndLevel\": \"20\",\n" +
                "    \"TypeId\": \"Hero\"\n" +
                "  }");
        scheduleProxy.writeUTF8("{}");
        scheduleProxy.writeUTF8("[]");

        scheduleBuffer.flip();

        ConfigurableObject configurableObject = new ConfigurableObject();
        configurableObject.read(scheduleProxy);
        TournamentSchedule tournamentSchedule = new TournamentSchedule(configurableObject);

        Assert.assertEquals(tournamentSchedule.typeId(), "Hero");

        TournamentManager tournamentManager = new TournamentManager(tournamentSchedule);
        tournamentManager.typeId("hero");

        ByteBuffer buffer = ByteBuffer.allocateDirect(700);
        Recoverable.DataBuffer proxy = BufferProxy.buffer(buffer);

        Assert.assertTrue(tournamentManager.write(proxy));

        buffer.flip();

        TournamentManager tournamentManager2 = new TournamentManager();
        Assert.assertTrue(tournamentManager2.read(proxy));

        Assert.assertEquals(tournamentManager2.typeId(), "hero");
    }
}
