package com.tarantula.test;


import com.icodesoftware.DataStore;
import com.icodesoftware.util.SnowflakeKey;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.game.GamePortableRegistry;
import com.tarantula.game.Rating;
import com.tarantula.platform.presence.PresencePortableRegistry;
import com.tarantula.platform.presence.saves.SavedGame;
import com.tarantula.platform.presence.saves.SavedGameQuery;
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
        //SavedGame systemSave = new SavedGame();
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
        Rating rating = new Rating();
        rating.ownerKey(load.key());
        Assert.assertTrue(dataStoreForRanking.create(rating));
        GamePortableRegistry registry = new GamePortableRegistry();
        List<Rating> rlist = dataStoreForRanking.list(new RecoverableQuery<>(save.key(),rating.label(),GamePortableRegistry.RATING_CID,registry));
        System.out.println(rlist.size());
    }
}
