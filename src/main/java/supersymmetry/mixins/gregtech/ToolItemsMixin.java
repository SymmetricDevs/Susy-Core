package supersymmetry.mixins.gregtech;

import gregtech.api.GTValues;
import gregtech.api.items.toolitem.*;
import gregtech.api.unification.material.Materials;
import gregtech.common.items.ToolItems;
import gregtech.common.items.tool.*;
import gregtech.core.sound.GTSoundEvents;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import supersymmetry.common.item.behavior.PipeNetWalkerBehavior;

import static gregtech.common.items.ToolItems.*;

@Mixin(value = ToolItems.class, remap = false)
public abstract class ToolItemsMixin {

    /**
     * @author Bruberu
     * @reason There really isn't a better way to add behaviors to basic tools; it's very limited.
     */
    @Overwrite
    public static void init() {
        SWORD = register(ItemGTSword.Builder.of(GTValues.MODID, "sword")
                .toolStats(b -> b.attacking()
                        .attackDamage(3.0F).attackSpeed(-2.4F))
                .toolClasses(ToolClasses.SWORD)
                .oreDict(ToolOreDict.toolSword));
        PICKAXE = register(ItemGTTool.Builder.of(GTValues.MODID, "pickaxe")
                .toolStats(b -> b.blockBreaking().attackDamage(1.0F).attackSpeed(-2.8F)
                        .behaviors(TorchPlaceBehavior.INSTANCE))
                .toolClasses(ToolClasses.PICKAXE)
                .oreDict(ToolOreDict.toolPickaxe));
        SHOVEL = register(ItemGTTool.Builder.of(GTValues.MODID, "shovel")
                .toolStats(b -> b.blockBreaking().attackDamage(1.5F).attackSpeed(-3.0F)
                        .behaviors(GrassPathBehavior.INSTANCE))
                .toolClasses(ToolClasses.SHOVEL)
                .oreDict(ToolOreDict.toolShovel));
        AXE = register(ItemGTAxe.Builder.of(GTValues.MODID, "axe")
                .toolStats(b -> b.blockBreaking()
                        .attackDamage(5.0F).attackSpeed(-3.2F).baseEfficiency(2.0F)
                        .behaviors(DisableShieldBehavior.INSTANCE, TreeFellingBehavior.INSTANCE))
                .toolClasses(ToolClasses.AXE)
                .oreDict(ToolOreDict.toolAxe));
        HOE = register(ItemGTHoe.Builder.of(GTValues.MODID, "hoe")
                .toolStats(b -> b.cannotAttack().attackSpeed(-1.0F))
                .toolClasses(ToolClasses.HOE)
                .oreDict(ToolOreDict.toolHoe));
        SAW = register(ItemGTTool.Builder.of(GTValues.MODID, "saw")
                .toolStats(b -> b.crafting().damagePerCraftingAction(2)
                        .attackDamage(-1.0F).attackSpeed(-2.6F)
                        .behaviors(HarvestIceBehavior.INSTANCE))
                .oreDict(ToolOreDict.toolSaw)
                .secondaryOreDicts("craftingToolSaw")
                .symbol('s')
                .toolClasses(ToolClasses.SAW));
        HARD_HAMMER = register(ItemGTTool.Builder.of(GTValues.MODID, "hammer")
                .toolStats(b -> b.blockBreaking().crafting().damagePerCraftingAction(2)
                        .attackDamage(1.0F).attackSpeed(-2.8F)
                        .behaviors(new EntityDamageBehavior(2.0F, EntityGolem.class)))
                .oreDict(ToolOreDict.toolHammer)
                .secondaryOreDicts("craftingToolHardHammer")
                .sound(SoundEvents.BLOCK_ANVIL_LAND)
                .symbol('h')
                .toolClasses(ToolClasses.PICKAXE, ToolClasses.HARD_HAMMER));
        SOFT_MALLET = register(ItemGTTool.Builder.of(GTValues.MODID, "mallet")
                .toolStats(b -> b.crafting().cannotAttack().attackSpeed(-2.4F))
                .oreDict(ToolOreDict.toolMallet)
                .secondaryOreDicts("craftingToolSoftHammer")
                .sound(GTSoundEvents.SOFT_MALLET_TOOL)
                .symbol('r')
                .toolClasses(ToolClasses.SOFT_MALLET)
                .markerItem(() -> ToolHelper.getAndSetToolData(SOFT_MALLET, Materials.Wood, 47, 1, 4F, 1F)));
        MINING_HAMMER = register(ItemGTTool.Builder.of(GTValues.MODID, "mining_hammer")
                .toolStats(b -> b.blockBreaking().aoe(1, 1, 0)
                        .efficiencyMultiplier(0.4F).attackDamage(1.5F).attackSpeed(-3.2F)
                        .durabilityMultiplier(3.0F)
                        .behaviors(TorchPlaceBehavior.INSTANCE))
                .toolClasses(ToolClasses.PICKAXE)
                .oreDict(ToolOreDict.toolMiningHammer));
        SPADE = register(ItemGTTool.Builder.of(GTValues.MODID, "spade")
                .toolStats(b -> b.blockBreaking().aoe(1, 1, 0)
                        .efficiencyMultiplier(0.4F).attackDamage(1.5F).attackSpeed(-3.2F)
                        .durabilityMultiplier(3.0F)
                        .behaviors(GrassPathBehavior.INSTANCE))
                .toolClasses(ToolClasses.SHOVEL)
                .oreDict(ToolOreDict.toolSpade));
        WRENCH = register(ItemGTTool.Builder.of(GTValues.MODID, "wrench")
                .toolStats(b -> b.blockBreaking().crafting().sneakBypassUse()
                        .attackDamage(1.0F).attackSpeed(-2.8F)
                        .behaviors(BlockRotatingBehavior.INSTANCE, new EntityDamageBehavior(3.0F, EntityGolem.class),
                                PipeNetWalkerBehavior.INSTANCE))
                .sound(GTSoundEvents.WRENCH_TOOL, true)
                .oreDict(ToolOreDict.toolWrench)
                .secondaryOreDicts("craftingToolWrench")
                .symbol('w')
                .toolClasses(ToolClasses.WRENCH));
        FILE = register(ItemGTTool.Builder.of(GTValues.MODID, "file")
                .toolStats(b -> b.crafting().damagePerCraftingAction(4)
                        .cannotAttack().attackSpeed(-2.4F))
                .sound(GTSoundEvents.FILE_TOOL)
                .oreDict(ToolOreDict.toolFile)
                .secondaryOreDicts("craftingToolFile")
                .symbol('f')
                .toolClasses(ToolClasses.FILE));
        CROWBAR = register(ItemGTTool.Builder.of(GTValues.MODID, "crowbar")
                .toolStats(b -> b.blockBreaking().crafting()
                        .attackDamage(2.0F).attackSpeed(-2.4F)
                        .sneakBypassUse().behaviors(RotateRailBehavior.INSTANCE))
                .sound(SoundEvents.ENTITY_ITEM_BREAK)
                .oreDict(ToolOreDict.toolCrowbar)
                .secondaryOreDicts("craftingToolCrowbar")
                .symbol('c')
                .toolClasses(ToolClasses.CROWBAR));
        SCREWDRIVER = register(ItemGTTool.Builder.of(GTValues.MODID, "screwdriver")
                .toolStats(b -> b.crafting().damagePerCraftingAction(4).sneakBypassUse()
                        .attackDamage(-1.0F).attackSpeed(3.0F)
                        .behaviors(new EntityDamageBehavior(3.0F, EntitySpider.class)))
                .sound(GTSoundEvents.SCREWDRIVER_TOOL)
                .oreDict(ToolOreDict.toolScrewdriver)
                .secondaryOreDicts("craftingToolScrewdriver")
                .symbol('d')
                .toolClasses(ToolClasses.SCREWDRIVER));
        MORTAR = register(ItemGTTool.Builder.of(GTValues.MODID, "mortar")
                .toolStats(b -> b.crafting().damagePerCraftingAction(2)
                        .cannotAttack().attackSpeed(-2.4F))
                .sound(GTSoundEvents.MORTAR_TOOL)
                .oreDict(ToolOreDict.toolMortar)
                .secondaryOreDicts("craftingToolMortar")
                .symbol('m')
                .toolClasses(ToolClasses.MORTAR));
        WIRE_CUTTER = register(ItemGTTool.Builder.of(GTValues.MODID, "wire_cutter")
                .toolStats(b -> b.blockBreaking().crafting().damagePerCraftingAction(4)
                        .attackDamage(-1.0F).attackSpeed(-2.4F)
                        .behaviors(PipeNetWalkerBehavior.INSTANCE))
                .sound(GTSoundEvents.WIRECUTTER_TOOL, true)
                .oreDict(ToolOreDict.toolWireCutter)
                .secondaryOreDicts("craftingToolWireCutter")
                .symbol('x')
                .toolClasses(ToolClasses.WIRE_CUTTER));
        SCYTHE = register(ItemGTSword.Builder.of(GTValues.MODID, "scythe")
                .toolStats(b -> b.blockBreaking().attacking()
                        .attackDamage(5.0F).attackSpeed(-3.0F).durabilityMultiplier(3.0F)
                        .aoe(2, 2, 2)
                        .behaviors(HoeGroundBehavior.INSTANCE, HarvestCropsBehavior.INSTANCE)
                        .canApplyEnchantment(EnumEnchantmentType.DIGGER))
                .oreDict(ToolOreDict.toolScythe)
                .toolClasses(ToolClasses.SCYTHE, ToolClasses.HOE));
        KNIFE = register(ItemGTSword.Builder.of(GTValues.MODID, "knife")
                .toolStats(b -> b.crafting().attacking().attackSpeed(3.0F))
                .oreDict(ToolOreDict.toolKnife)
                .secondaryOreDicts("craftingToolKnife")
                .symbol('k')
                .toolClasses(ToolClasses.KNIFE, ToolClasses.SWORD));
        BUTCHERY_KNIFE = register(ItemGTSword.Builder.of(GTValues.MODID, "butchery_knife")
                .toolStats(b -> b.attacking()
                        .attackDamage(1.5F).attackSpeed(-1.3F).defaultEnchantment(Enchantments.LOOTING, 3))
                .oreDict(ToolOreDict.toolButcheryKnife)
                .secondaryOreDicts("craftingToolButcheryKnife")
                .toolClasses(ToolClasses.BUTCHERY_KNIFE));
        DRILL_LV = register(ItemGTTool.Builder.of(GTValues.MODID, "drill_lv")
                .toolStats(b -> b.blockBreaking().aoe(1, 1, 0)
                        .attackDamage(1.0F).attackSpeed(-3.2F).durabilityMultiplier(3.0F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_LV)
                        .behaviors(TorchPlaceBehavior.INSTANCE))
                .oreDict(ToolOreDict.toolDrill)
                .secondaryOreDicts(ToolOreDict.toolPickaxe, ToolOreDict.toolShovel)
                .sound(GTSoundEvents.DRILL_TOOL, true)
                .toolClasses(ToolClasses.DRILL)
                .electric(GTValues.LV));
        DRILL_MV = register(ItemGTTool.Builder.of(GTValues.MODID, "drill_mv")
                .toolStats(b -> b.blockBreaking().aoe(1, 1, 2)
                        .attackDamage(1.0F).attackSpeed(-3.2F).durabilityMultiplier(4.0F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_MV)
                        .behaviors(TorchPlaceBehavior.INSTANCE))
                .oreDict(ToolOreDict.toolDrill)
                .secondaryOreDicts(ToolOreDict.toolPickaxe, ToolOreDict.toolShovel)
                .sound(GTSoundEvents.DRILL_TOOL, true)
                .toolClasses(ToolClasses.DRILL)
                .electric(GTValues.MV));
        DRILL_HV = register(ItemGTTool.Builder.of(GTValues.MODID, "drill_hv")
                .toolStats(b -> b.blockBreaking().aoe(2, 2, 4)
                        .attackDamage(1.0F).attackSpeed(-3.2F).durabilityMultiplier(5.0F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_HV)
                        .behaviors(TorchPlaceBehavior.INSTANCE))
                .oreDict(ToolOreDict.toolDrill)
                .secondaryOreDicts(ToolOreDict.toolPickaxe, ToolOreDict.toolShovel)
                .sound(GTSoundEvents.DRILL_TOOL, true)
                .toolClasses(ToolClasses.DRILL)
                .electric(GTValues.HV));
        DRILL_EV = register(ItemGTTool.Builder.of(GTValues.MODID, "drill_ev")
                .toolStats(b -> b.blockBreaking().aoe(3, 3, 6)
                        .attackDamage(1.0F).attackSpeed(-3.2F).durabilityMultiplier(6.0F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_EV)
                        .behaviors(TorchPlaceBehavior.INSTANCE))
                .oreDict(ToolOreDict.toolDrill)
                .secondaryOreDicts(ToolOreDict.toolPickaxe, ToolOreDict.toolShovel)
                .sound(GTSoundEvents.DRILL_TOOL, true)
                .toolClasses(ToolClasses.DRILL)
                .electric(GTValues.EV));
        DRILL_IV = register(ItemGTTool.Builder.of(GTValues.MODID, "drill_iv")
                .toolStats(b -> b.blockBreaking().aoe(4, 4, 8)
                        .attackDamage(1.0F).attackSpeed(-3.2F).durabilityMultiplier(7.0F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_IV)
                        .behaviors(TorchPlaceBehavior.INSTANCE))
                .oreDict(ToolOreDict.toolDrill)
                .secondaryOreDicts(ToolOreDict.toolPickaxe, ToolOreDict.toolShovel)
                .sound(GTSoundEvents.DRILL_TOOL, true)
                .toolClasses(ToolClasses.DRILL)
                .electric(GTValues.IV));
        CHAINSAW_LV = register(ItemGTAxe.Builder.of(GTValues.MODID, "chainsaw_lv")
                .toolStats(b -> b.blockBreaking()
                        .efficiencyMultiplier(2.0F)
                        .attackDamage(5.0F).attackSpeed(-3.2F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_LV)
                        .behaviors(HarvestIceBehavior.INSTANCE, DisableShieldBehavior.INSTANCE,
                                TreeFellingBehavior.INSTANCE))
                .oreDict(ToolOreDict.toolAxe)
                .secondaryOreDicts(ToolOreDict.toolChainsaw)
                .sound(GTSoundEvents.CHAINSAW_TOOL, true)
                .toolClasses(ToolClasses.AXE)
                .electric(GTValues.LV));
        WRENCH_LV = register(ItemGTTool.Builder.of(GTValues.MODID, "wrench_lv")
                .toolStats(b -> b.blockBreaking().crafting().sneakBypassUse()
                        .efficiencyMultiplier(2.0F)
                        .attackDamage(1.0F).attackSpeed(-2.8F)
                        .behaviors(BlockRotatingBehavior.INSTANCE, new EntityDamageBehavior(3.0F, EntityGolem.class),
                                PipeNetWalkerBehavior.INSTANCE)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_LV))
                .sound(GTSoundEvents.WRENCH_TOOL, true)
                .oreDict(ToolOreDict.toolWrench)
                .secondaryOreDicts("craftingToolWrench")
                .toolClasses(ToolClasses.WRENCH)
                .electric(GTValues.LV));
        WRENCH_HV = register(ItemGTTool.Builder.of(GTValues.MODID, "wrench_hv")
                .toolStats(b -> b.blockBreaking().crafting().sneakBypassUse()
                        .efficiencyMultiplier(3.0F)
                        .attackDamage(1.0F).attackSpeed(-2.8F)
                        .behaviors(BlockRotatingBehavior.INSTANCE, new EntityDamageBehavior(3.0F, EntityGolem.class),
                                PipeNetWalkerBehavior.INSTANCE)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_HV))
                .sound(GTSoundEvents.WRENCH_TOOL, true)
                .oreDict(ToolOreDict.toolWrench)
                .secondaryOreDicts("craftingToolWrench")
                .toolClasses(ToolClasses.WRENCH)
                .electric(GTValues.HV));
        WRENCH_IV = register(ItemGTTool.Builder.of(GTValues.MODID, "wrench_iv")
                .toolStats(b -> b.blockBreaking().crafting().sneakBypassUse()
                        .efficiencyMultiplier(4.0F)
                        .attackDamage(1.0F).attackSpeed(-2.8F)
                        .behaviors(BlockRotatingBehavior.INSTANCE, new EntityDamageBehavior(3.0F, EntityGolem.class),
                                PipeNetWalkerBehavior.INSTANCE)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_IV))
                .sound(GTSoundEvents.WRENCH_TOOL, true)
                .oreDict(ToolOreDict.toolWrench)
                .secondaryOreDicts("craftingToolWrench")
                .toolClasses(ToolClasses.WRENCH)
                .electric(GTValues.IV));
        BUZZSAW = register(ItemGTTool.Builder.of(GTValues.MODID, "buzzsaw")
                .toolStats(b -> b.crafting().attackDamage(1.5F).attackSpeed(-3.2F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_LV))
                .sound(GTSoundEvents.CHAINSAW_TOOL, true)
                .oreDict(ToolOreDict.toolSaw)
                .secondaryOreDicts("craftingToolSaw")
                .secondaryOreDicts(ToolOreDict.toolBuzzsaw)
                .toolClasses(ToolClasses.SAW)
                .electric(GTValues.LV));
        SCREWDRIVER_LV = register(ItemGTTool.Builder.of(GTValues.MODID, "screwdriver_lv")
                .toolStats(b -> b.crafting().sneakBypassUse()
                        .attackDamage(-1.0F).attackSpeed(3.0F)
                        .behaviors(new EntityDamageBehavior(3.0F, EntitySpider.class))
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_LV))
                .sound(GTSoundEvents.SCREWDRIVER_TOOL)
                .oreDict(ToolOreDict.toolScrewdriver)
                .secondaryOreDicts("craftingToolScrewdriver")
                .toolClasses(ToolClasses.SCREWDRIVER)
                .electric(GTValues.LV));
        PLUNGER = register(ItemGTTool.Builder.of(GTValues.MODID, "plunger")
                .toolStats(b -> b.cannotAttack().attackSpeed(-2.4F).sneakBypassUse()
                        .behaviors(PlungerBehavior.INSTANCE))
                .sound(GTSoundEvents.PLUNGER_TOOL)
                .oreDict(ToolOreDict.toolPlunger)
                .toolClasses(ToolClasses.PLUNGER)
                .markerItem(() -> ToolHelper.getAndSetToolData(PLUNGER, Materials.Rubber, 255, 1, 4F, 0F)));
        WIRECUTTER_LV = register(ItemGTTool.Builder.of(GTValues.MODID, "wire_cutter_lv")
                .toolStats(b -> b.blockBreaking().crafting().damagePerCraftingAction(4)
                        .efficiencyMultiplier(2.0F)
                        .attackDamage(-1.0F).attackSpeed(-2.4F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_LV)
                        .behaviors(PipeNetWalkerBehavior.INSTANCE))
                .sound(GTSoundEvents.WIRECUTTER_TOOL, true)
                .oreDict(ToolOreDict.toolWireCutter)
                .secondaryOreDicts("craftingToolWireCutter")
                .toolClasses(ToolClasses.WIRE_CUTTER)
                .electric(GTValues.LV));
        WIRECUTTER_HV = register(ItemGTTool.Builder.of(GTValues.MODID, "wire_cutter_hv")
                .toolStats(b -> b.blockBreaking().crafting().damagePerCraftingAction(4)
                        .efficiencyMultiplier(3.0F)
                        .attackDamage(-1.0F).attackSpeed(-2.4F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_LV)
                        .behaviors(PipeNetWalkerBehavior.INSTANCE))
                .sound(GTSoundEvents.WIRECUTTER_TOOL, true)
                .oreDict(ToolOreDict.toolWireCutter)
                .secondaryOreDicts("craftingToolWireCutter")
                .toolClasses(ToolClasses.WIRE_CUTTER)
                .electric(GTValues.HV));
        WIRECUTTER_IV = register(ItemGTTool.Builder.of(GTValues.MODID, "wire_cutter_iv")
                .toolStats(b -> b.blockBreaking().crafting().damagePerCraftingAction(4)
                        .efficiencyMultiplier(4.0F)
                        .attackDamage(-1.0F).attackSpeed(-2.4F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_LV)
                        .behaviors(PipeNetWalkerBehavior.INSTANCE))
                .sound(GTSoundEvents.WIRECUTTER_TOOL, true)
                .oreDict(ToolOreDict.toolWireCutter)
                .secondaryOreDicts("craftingToolWireCutter")
                .toolClasses(ToolClasses.WIRE_CUTTER)
                .electric(GTValues.IV));
    }
}
