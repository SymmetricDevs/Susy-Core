package supersymmetry.common.item.armor;

import supersymmetry.common.item.SuSyArmorItem;
import supersymmetry.common.item.SuSyMetaItems;

import static supersymmetry.common.item.SuSyMetaItems.*;
import static net.minecraft.inventory.EntityEquipmentSlot.*;

public class SuSyMetaArmor extends SuSyArmorItem {

    @Override
    public void registerSubItems() {
        SIMPLE_GAS_MASK = addItem(0, "simple_gas_mask").setArmorLogic(new SimpleGasMask());
        SuSyMetaItems.JET_WINGPACK = addItem(1, "jet_wingpack").setArmorLogic(new JetWingpack());
        GAS_MASK = addItem(2, "gas_mask").setArmorLogic(new BreathingApparatus(HEAD, 300));
        GAS_TANK = addItem(3, "gas_tank").setArmorLogic(new BreathingApparatus(CHEST, 500));
        ASBESTOS_MASK = addItem(4, "asbestos_mask").setArmorLogic(new AdvancedBreathingApparatus(HEAD, 250, 1, "asbestos", 0, 0.3));
        ASBESTOS_CHESTPLATE = addItem(5, "asbestos_chestplate").setArmorLogic(new AdvancedBreathingTank(400, 1, "asbestos", 0, 0.3, 1200));
        ASBESTOS_LEGGINGS = addItem(6, "asbestos_leggings").setArmorLogic(new AdvancedBreathingApparatus(LEGS, 350, 1, "asbestos", 0, 0.3));
        ASBESTOS_BOOTS = addItem(7, "asbestos_boots").setArmorLogic(new AdvancedBreathingApparatus(FEET, 300, 1, "asbestos", 0, 0.3));
        REBREATHER_TANK = addItem(8, "rebreather_tank").setArmorLogic(new AdvancedBreathingTank(405, 1, "rebreather", 0, 0.3, 3600));
        REFLECTIVE_MASK = addItem(9, "reflective_mask").setArmorLogic(new AdvancedBreathingApparatus(HEAD, 250, 5, "reflective", 0, 0.4));
        REFLECTIVE_CHESTPLATE = addItem(10, "reflective_chestplate").setArmorLogic(new AdvancedBreathingTank(400, 5, "reflective", 0, 0.4, 1200));
        REFLECTIVE_LEGGINGS = addItem(11, "reflective_leggings").setArmorLogic(new AdvancedBreathingApparatus(LEGS, 350, 5, "reflective", 0, 0.4));
        REFLECTIVE_BOOTS = addItem(12, "reflective_boots").setArmorLogic(new AdvancedBreathingApparatus(FEET, 300, 5, "reflective", 0, 0.4));
        FILTERED_TANK = addItem(13, "filtered_tank").setArmorLogic(new AdvancedBreathingTank(415, 5, "filtered", 0, 0.4, AdvancedBreathingTank.INFINITE_OXYGEN));
        NOMEX_MASK = addItem(14, "nomex_mask").setArmorLogic(new AdvancedBreathingApparatus(HEAD, 700, 0, "nomex", 1, 0.6));
        NOMEX_CHESTPLATE = addItem(15, "nomex_chestplate").setArmorLogic(new AdvancedBreathingTank(1000, 0, "nomex", 1, 0.6, AdvancedBreathingTank.INFINITE_OXYGEN));
        NOMEX_LEGGINGS = addItem(16, "nomex_leggings").setArmorLogic(new AdvancedBreathingApparatus(LEGS, 900, 0, "nomex", 1, 0.6));
        NOMEX_BOOTS = addItem(17, "nomex_boots").setArmorLogic(new AdvancedBreathingApparatus(FEET, 850, 0, "nomex", 1, 0.6));
        ASTRONAUT_HELMET = addItem(18, "astronaut_helmet").setArmorLogic(new SpaceSuit(HEAD, 100, 0, "astronaut", 1, 0.6));
        ASTRONAUT_CHESTPLATE = addItem(19, "astronaut_chestplate").setArmorLogic(new SpaceSuitTank(200, 0, "astronaut", 1, 0.6, AdvancedBreathingTank.INFINITE_OXYGEN));
        ASTRONAUT_LEGGINGS = addItem(20, "astronaut_leggings").setArmorLogic(new SpaceSuit(LEGS, 175, 0, "astronaut", 1, 0.6));
        ASTRONAUT_BOOTS = addItem(21, "astronaut_boots").setArmorLogic(new SpaceSuit(FEET, 150, 0, "astronaut", 1, 0.6));
    }
}
