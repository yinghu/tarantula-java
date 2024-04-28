package com.tarantula.test;


import com.icodesoftware.DataStore;
import com.icodesoftware.protocol.ProtocolPortableRegistry;
import com.icodesoftware.protocol.statistics.UserRating;
import com.icodesoftware.util.SnowflakeKey;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.presence.PresencePortableRegistry;
import com.tarantula.platform.presence.saves.CurrentSaveIndex;
import com.tarantula.platform.presence.saves.SavedGame;
import com.tarantula.platform.util.RecoverableQuery;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.List;


public class SavedGameTest extends DataStoreHook{


    @Test(groups = { "SavedGame" })
    public void savedGameTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_save");
        DataStore dataStoreForRanking = dataStoreProvider.createDataStore("test_ranking");
        long playerId = serviceContext.distributionId();
        long stub = serviceContext.distributionId();
        SavedGame systemSave = new SavedGame();
        systemSave.name("save");
        systemSave.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        systemSave.distributionId(playerId);
        Assert.assertTrue(dataStore.createIfAbsent(systemSave,false));
        CurrentSaveIndex currentSaveIndex = new CurrentSaveIndex();
        currentSaveIndex.distributionId(stub);
        Assert.assertTrue(dataStore.createIfAbsent(currentSaveIndex,false));
        for(int i=0;i<3;i++){
            SavedGame savedGame = new SavedGame();
            savedGame.name("save"+i);
            savedGame.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
            savedGame.ownerKey(new SnowflakeKey(playerId));
            Assert.assertTrue(dataStore.create(savedGame));
        }
        RecoverableQuery<SavedGame> query = new RecoverableQuery<>(new SnowflakeKey(playerId),SavedGame.USER_SAVE, PresencePortableRegistry.SAVED_GAME_CID,PresencePortableRegistry.INS);
        List<SavedGame> saves = dataStore.list(query);////dataStore.list(new SavedGameQuery<>(new SnowflakeKey(playerId)));
        Assert.assertEquals(saves.size(),3);
        SavedGame save = saves.get(0);
        save.stub = stub;
        Assert.assertTrue(dataStore.update(save));
        SavedGame load = new SavedGame();
        load.distributionId(save.distributionId());
        Assert.assertTrue(dataStore.load(load));
        Assert.assertEquals(load.stub,save.stub);
        UserRating rating = new UserRating();
        rating.ownerKey(load.key());
        Assert.assertTrue(dataStoreForRanking.create(rating));
        ProtocolPortableRegistry registry = new ProtocolPortableRegistry();
        List<UserRating> rlist = dataStoreForRanking.list(new RecoverableQuery<>(save.key(),rating.label(), ProtocolPortableRegistry.USER_RATION_CID,registry));
        Assert.assertEquals(rlist.size(),1);
    }
}
