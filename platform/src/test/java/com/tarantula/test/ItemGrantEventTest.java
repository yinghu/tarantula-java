package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.util.SnowflakeKey;
import com.tarantula.platform.inbox.PlatformItemGrantEvent;
import com.tarantula.platform.inbox.PlatformItemGrantEventQuery;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ItemGrantEventTest extends DataStoreHook{

    @Test(groups = { "ItemGrantEvent" })
    public void itemGrantEventTest() {
        DataStore itemGrantEventDataStore = dataStoreProvider.createDataStore("player_item_grant_events");

        PlatformItemGrantEvent itemGrantEvent = new PlatformItemGrantEvent("Individual", "12345", "Gems", 10, false, LocalDateTime.now());
        long presenceId = serviceContext.distributionId();
        itemGrantEvent.ownerKey(SnowflakeKey.from(presenceId));

        Assert.assertTrue(itemGrantEventDataStore.create(itemGrantEvent));

        List<PlatformItemGrantEvent> itemGrantEvents = new ArrayList<>(itemGrantEventDataStore.list(new PlatformItemGrantEventQuery(presenceId)));

        Assert.assertEquals(itemGrantEvents.size(), 1);
        Assert.assertNotNull(itemGrantEvents.get(0));
        Assert.assertEquals(itemGrantEvents.get(0), itemGrantEvent);
    }
}
