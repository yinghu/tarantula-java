package com.perfectday.games.earth8;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.AbstractRecoverableListener;


public class Earth8PortableRegistry<T extends Recoverable> extends AbstractRecoverableListener {

    public static final int OID = 100;

    public static final int BATTLE_TRANSACTION_CID = 1;
    public static final int UNIT_XP_UP_CID = 2;
    public static final int UNIT_RANK_UP_CID = 3;
    public static final int UNIT_SKILL_UP_CID = 4;

    public static final int EQUIPMENT_XP_UP_CID = 5;
    public static final int EQUIPMENT_RANK_UP_CID = 6;

    public static final int EQUIPMENT_SALVAGE_CID = 7;

    public static final int EQUIPMENT_EQUIP_CID = 8;

    public static final int EQUIPMENT_UN_EQUIP_CID = 9;

    public static final int UNIT_CID = 10;
    public static final int EQUIPMENT_CID = 11;
    
    public static final int CAMPAIGN_PROGRESS_CID = 12;



    public static Earth8PortableRegistry INS;

    public Earth8PortableRegistry(){
        INS = this;
    }

    public T create(int i) {
        Recoverable pt = null;
        switch (i){
            case BATTLE_TRANSACTION_CID:
                pt = new BattleTransaction();
                break;
            case UNIT_XP_UP_CID:
                pt = new UnitXpUp();
                break;
            case UNIT_RANK_UP_CID:
                pt = new UnitRankUp();
                break;
            case UNIT_SKILL_UP_CID:
                pt = new UnitSkillUp();
                break;
            case EQUIPMENT_XP_UP_CID:
                pt = new EquipmentXpUp();
                break;
            case EQUIPMENT_RANK_UP_CID:
                pt = new EquipmentRankUp();
                break;
            case EQUIPMENT_SALVAGE_CID:
                pt = new EquipmentSalvage();
                break;
            case EQUIPMENT_EQUIP_CID:
                pt = new EquipmentEquip();
                break;
            case EQUIPMENT_UN_EQUIP_CID:
                pt = new EquipmentUnEquip();
                break;
            case UNIT_CID:
                pt = new Unit();
                break;
            case EQUIPMENT_CID:
                pt = new Equipment();
                break;
            case CAMPAIGN_PROGRESS_CID:
                pt = new CampaignProgress();
                break;
            default:
        }
        return (T)pt;
    }

    public int registryId() {
        return OID;
    }
}
