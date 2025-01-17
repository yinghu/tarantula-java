package com.tarantula.test;


import com.icodesoftware.DataStore;
import com.tarantula.platform.presence.pvp.DefenseTeam;
import com.tarantula.platform.presence.pvp.EquipmentInstance;
import com.tarantula.platform.presence.pvp.TeamFormationIndex;
import com.tarantula.platform.presence.pvp.UnitInstance;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class DefenseTeamTest extends DataStoreHook{

    @Test(groups = { "DefenseTeam" })
    public void fullSizePayloadTest(){
        Exception exception = null;
        try(InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("SampleDefenseFormation-full.json")){
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            inputStream.transferTo(outputStream);
            DefenseTeam defenseTeam = DefenseTeam.parse(outputStream.toByteArray());
            Assert.assertEquals(defenseTeam.unitInstances.size(),5);
            defenseTeam.unitInstances.forEach(unitInstance -> {
                Assert.assertTrue(unitInstance.level>0);
                Assert.assertTrue(unitInstance.rank>0);
                Assert.assertTrue(unitInstance.currentLevelExperience>0);
                Assert.assertTrue(unitInstance.currentRankExperience>0);
                Assert.assertTrue(unitInstance.configID!=null);
                Assert.assertTrue(unitInstance.passiveRank>0);
                Assert.assertTrue(unitInstance.bootsIDValue>0);
                Assert.assertTrue(unitInstance.chestPieceIDValue>0);
                Assert.assertTrue(unitInstance.helmetIDValue>0);
                Assert.assertTrue(unitInstance.weaponIDValue>0);
                Assert.assertTrue(unitInstance.forceFieldIDValue>0);
                Assert.assertTrue(unitInstance.glovesIDValue>0);
                Assert.assertTrue(unitInstance.abilityRanks[0]==1);
                Assert.assertTrue(unitInstance.abilityRanks[1]==0);
                Assert.assertTrue(unitInstance.abilityRanks[2]==0);
                Assert.assertTrue(unitInstance.abilityRanks[3]==0);
            });
            Assert.assertEquals(defenseTeam.equipmentInstances.size(),30);
            defenseTeam.equipmentInstances.forEach(equipmentInstance -> {
                Assert.assertTrue(equipmentInstance.level>0);
                Assert.assertTrue(equipmentInstance.rank>0);
                Assert.assertTrue(equipmentInstance.currentLevelExperience>0);
                Assert.assertTrue(equipmentInstance.currentRankExperience>0);
                Assert.assertTrue(equipmentInstance.configID!=null);
                Assert.assertTrue(equipmentInstance.setConfigID!=null);
                Assert.assertTrue(equipmentInstance.rewardConfigID!=null);
                Assert.assertTrue(equipmentInstance.snowflakeIDValue>0);
                Assert.assertTrue(equipmentInstance.primaryStat!=null);
                Assert.assertTrue(equipmentInstance.primaryStat.statConfigID!=null);
                Assert.assertTrue(equipmentInstance.primaryStat.subStatRolledPercentageNormalized>0);
                Assert.assertTrue(equipmentInstance.primaryStat.hasSubStatRoll);
                for(int i=0;i<equipmentInstance.subStats.length;i++){
                    Assert.assertTrue(equipmentInstance.subStats[i].statConfigID!=null);
                    Assert.assertTrue(equipmentInstance.subStats[i].subStatRolledPercentageNormalized>0);
                    Assert.assertTrue(equipmentInstance.subStats[i].hasSubStatRoll);
                }
            });
            Assert.assertTrue(defenseTeam.teamPower>0);
        }catch (Exception ex){
            exception = ex;
        }
        Assert.assertNull(exception);
    }

    @Test(groups = { "DefenseTeam" })
    public void noEquipmentsPayloadTest(){
        Exception exception = null;
        try(InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("SampleDefenseFormation-no-equipments.json")){
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            inputStream.transferTo(outputStream);
            DefenseTeam defenseTeam = DefenseTeam.parse(outputStream.toByteArray());
            Assert.assertEquals(defenseTeam.unitInstances.size(),5);
            Assert.assertEquals(defenseTeam.equipmentInstances.size(),0);
            Assert.assertTrue(defenseTeam.teamPower>0);
        }catch (Exception ex){
            exception = ex;
        }
        Assert.assertNull(exception);
    }

    @Test(groups = { "DefenseTeam" })
    public void teamFormationIndexTest(){
        Exception exception = null;
        try(InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("SampleDefenseFormation-no-equipments.json")){
            DataStore dataStore = dataStoreProvider.createDataStore("test_pvp_teams");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            inputStream.transferTo(outputStream);
            DefenseTeam defenseTeam = DefenseTeam.parse(outputStream.toByteArray());
            TeamFormationIndex teamFormationIndex = new TeamFormationIndex();
            teamFormationIndex.distributionId(100);
            dataStore.createIfAbsent(teamFormationIndex,true);
            Assert.assertTrue(teamFormationIndex.expired());
            defenseTeam.save(dataStore,teamFormationIndex,5);
            Assert.assertTrue(teamFormationIndex.teamId>0);
            DefenseTeam load = new DefenseTeam();
            load.load(dataStore,teamFormationIndex);
            load.toJson();
        }catch (Exception ex){
            exception = ex;
        }
        Assert.assertNull(exception);
    }

    @Test(groups = { "DefenseTeam" })
    public void fullTeamFormationIndexTest(){
        Exception exception = null;
        try(InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("SampleDefenseFormation-full.json")){
            DataStore dataStore = dataStoreProvider.createDataStore("test_pvp_teams");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            inputStream.transferTo(outputStream);
            DefenseTeam defenseTeam = DefenseTeam.parse(outputStream.toByteArray());
            TeamFormationIndex teamFormationIndex = new TeamFormationIndex();
            teamFormationIndex.distributionId(200);
            dataStore.createIfAbsent(teamFormationIndex,true);
            Assert.assertTrue(teamFormationIndex.expired());
            defenseTeam.save(dataStore,teamFormationIndex,5);
            Assert.assertTrue(teamFormationIndex.teamId>0);
            DefenseTeam load = new DefenseTeam();
            load.load(dataStore,teamFormationIndex);
            load.toJson();
        }catch (Exception ex){
            exception = ex;
        }
        Assert.assertNull(exception);
    }
}
