package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.util.JsonUtil;

import java.util.concurrent.atomic.AtomicInteger;

public class CheatDetectionRule {
    private final Earth8GameServiceProvider earth8GameServiceProvider;
    private AtomicInteger HardCurrency = new AtomicInteger(300000);
    private AtomicInteger SoftCurrency = new AtomicInteger(300000);
    private AtomicInteger Energy = new AtomicInteger(300000);
    private AtomicInteger SkillDungeonEnergy = new AtomicInteger(300000);
    private AtomicInteger Coin = new AtomicInteger(300000);
    private AtomicInteger Potion = new AtomicInteger(300000);
    private AtomicInteger MultiBattleTicket = new AtomicInteger(300000);
    private AtomicInteger ClassAttackerSkillUpMaterial = new AtomicInteger(300000);
    private AtomicInteger ClassDefenderSkillUpMaterial = new AtomicInteger(300000);
    private AtomicInteger ClassSupportSkillUpMaterial = new AtomicInteger(300000);
    private AtomicInteger SkillCommonRankUpMaterial = new AtomicInteger(300000);
    private AtomicInteger SkillEpicRankUpMaterial = new AtomicInteger(300000);
    private AtomicInteger SkillLegendaryRankUpMaterial = new AtomicInteger(300000);
    private AtomicInteger SkillRareRankUpMaterial = new AtomicInteger(300000);

    public CheatDetectionRule(Earth8GameServiceProvider earth8GameServiceProvider){
        this.earth8GameServiceProvider = earth8GameServiceProvider;
    }

    public void detect(BattleUpdate update, Session session){
        if (update.updateId == BattleUpdate.UpdateId.ManualAnalyticsBatch) {
            for(AnalyticsBatchUtils.AnalyticsData analytic: ((ManualAnalyticsBatch)update).analytics){
                if(analytic.messageType.equals("currencyUpdate")){
                    String currencyId = analytic.clientData.get("currencyId").getAsString();
                    int currencyDelta = analytic.clientData.get("currencyDelta").getAsInt();
                    int maxValueExceeded = 0;

                    if(currencyId.equals("HardCurrency") && currencyDelta > HardCurrency.get())
                        maxValueExceeded = HardCurrency.get();
                    else if (currencyId.equals("SoftCurrency") && currencyDelta > SoftCurrency.get())
                        maxValueExceeded = SoftCurrency.get();
                    else if (currencyId.equals("Energy") && currencyDelta > Energy.get())
                        maxValueExceeded = Energy.get();
                    else if (currencyId.equals("SkillDungeonEnergy") && currencyDelta > SkillDungeonEnergy.get())
                        maxValueExceeded = SkillDungeonEnergy.get();
                    else if (currencyId.equals("MultiBattleTicket") && currencyDelta > MultiBattleTicket.get())
                        maxValueExceeded = MultiBattleTicket.get();
                    else if (currencyId.equals("ClassAttackerSkillUpMaterial") && currencyDelta > ClassAttackerSkillUpMaterial.get())
                        maxValueExceeded = ClassAttackerSkillUpMaterial.get();
                    else if (currencyId.equals("ClassDefenderSkillUpMaterial") && currencyDelta > ClassDefenderSkillUpMaterial.get())
                        maxValueExceeded = ClassDefenderSkillUpMaterial.get();
                    else if (currencyId.equals("ClassSupportSkillUpMaterial") && currencyDelta > ClassSupportSkillUpMaterial.get())
                        maxValueExceeded = ClassSupportSkillUpMaterial.get();
                    else if (currencyId.equals("SkillCommonRankUpMaterial") && currencyDelta > SkillCommonRankUpMaterial.get())
                        maxValueExceeded = SkillCommonRankUpMaterial.get();
                    else if (currencyId.equals("SkillEpicRankUpMaterial") && currencyDelta > SkillEpicRankUpMaterial.get())
                        maxValueExceeded = SkillEpicRankUpMaterial.get();
                    else if (currencyId.equals("SkillLegendaryRankUpMaterial") && currencyDelta > SkillLegendaryRankUpMaterial.get())
                        maxValueExceeded = SkillLegendaryRankUpMaterial.get();
                    else if (currencyId.equals("SkillRareRankUpMaterial") && currencyDelta > SkillRareRankUpMaterial.get())
                        maxValueExceeded = SkillRareRankUpMaterial.get();
                    else if (currencyId.contains("Coin") && currencyDelta > Coin.get())
                        maxValueExceeded = Coin.get();
                    else if (currencyId.contains("Potion") && currencyDelta > Potion.get())
                        maxValueExceeded = Potion.get();
                    else
                        return;

                    earth8GameServiceProvider.sendCheatDetectedAnalytic(session, maxValueExceeded, analytic);
                }
            }
        }
    }

    public void configure(byte [] payload){
        JsonObject jsonObject = JsonUtil.parse(payload);

        HardCurrency.set(jsonObject.get("HardCurrency").getAsInt());
        SoftCurrency.set(jsonObject.get("SoftCurrency").getAsInt());
        Energy.set(jsonObject.get("Energy").getAsInt());
        SkillDungeonEnergy.set(jsonObject.get("SkillDungeonEnergy").getAsInt());
        Coin.set(jsonObject.get("Coin").getAsInt());
        Potion.set(jsonObject.get("Potion").getAsInt());
        MultiBattleTicket.set(jsonObject.get("MultiBattleTicket").getAsInt());
        ClassAttackerSkillUpMaterial.set(jsonObject.get("ClassAttackerSkillUpMaterial").getAsInt());
        ClassDefenderSkillUpMaterial.set(jsonObject.get("ClassDefenderSkillUpMaterial").getAsInt());
        ClassSupportSkillUpMaterial.set(jsonObject.get("ClassSupportSkillUpMaterial").getAsInt());
        SkillCommonRankUpMaterial.set(jsonObject.get("SkillCommonRankUpMaterial").getAsInt());
        SkillEpicRankUpMaterial.set(jsonObject.get("SkillEpicRankUpMaterial").getAsInt());
        SkillLegendaryRankUpMaterial.set(jsonObject.get("SkillLegendaryRankUpMaterial").getAsInt());
        SkillRareRankUpMaterial.set(jsonObject.get("SkillRareRankUpMaterial").getAsInt());
    }

}
