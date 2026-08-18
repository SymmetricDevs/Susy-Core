package supersymmetry.integration.jei;

import static supersymmetry.api.fluids.SusyGeneratedFluidHandler.CAST_MATERIALS;
import static supersymmetry.common.blocks.SuSyMetaBlocks.TANKLESS_FLUID_PIPES;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import lombok.val;
import mezz.jei.api.*;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.jetbrains.annotations.NotNull;

import cam72cam.immersiverailroading.IRItems;
import gregtech.api.GTValues;
import gregtech.api.modules.GregTechModule;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.util.Mods;
import gregtech.integration.IntegrationSubmodule;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import supersymmetry.Supersymmetry;
import supersymmetry.api.particle.ParticleBeam;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.api.rocketry.fuels.RocketFuelEntry;
import supersymmetry.common.metatileentities.SuSyMetaTileEntities;
import supersymmetry.integration.jei.category.StrandCategory;
import supersymmetry.integration.jei.category.StrandInfo;
import supersymmetry.integration.jei.ingredient.ParticleBeamHelper;
import supersymmetry.integration.jei.ingredient.ParticleBeamListFactory;
import supersymmetry.integration.jei.ingredient.ParticleBeamRenderer;
import supersymmetry.integration.jei.ingredient.ParticleType;
import supersymmetry.modules.SuSyModules;

@JEIPlugin
@GregTechModule(moduleID = SuSyModules.MODULE_JEI,
                containerID = Supersymmetry.MODID,
                modDependencies = Mods.Names.JUST_ENOUGH_ITEMS,
                name = "SuSy JEI Integration",
                description = "SuSy JEI Integration Module")
public class JeiModule extends IntegrationSubmodule implements IModPlugin {

    @Override
    public void registerSubtypes(@NotNull ISubtypeRegistry subtypeRegistry) {
        subtypeRegistry.registerSubtypeInterpreter(IRItems.ITEM_ROLLING_STOCK.internal,
                new RollingStockSubtypeHandler());
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registry) {
        List<ParticleBeam> particleBeamList = ParticleBeamListFactory.createList();
        ParticleBeamHelper particleBeamHelper = new ParticleBeamHelper();
        ParticleBeamRenderer particleBeamRenderer = new ParticleBeamRenderer();
        registry.register(ParticleType.Particle, particleBeamList, particleBeamHelper, particleBeamRenderer);
    }

    @Override
    public void register(IModRegistry registry) {
        String semiFluidMapId = GTValues.MODID + ":" + RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.getUnlocalizedName();

        registry.addRecipeCatalyst(SuSyMetaTileEntities.LARGE_BRONZE_BOILER.getStackForm(), semiFluidMapId);
        registry.addRecipeCatalyst(SuSyMetaTileEntities.LARGE_STEEL_BOILER.getStackForm(), semiFluidMapId);
        registry.addRecipeCatalyst(SuSyMetaTileEntities.STEAM_BOILER_LIQUID_BRONZE.getStackForm(), semiFluidMapId);
        registry.addRecipeCatalyst(SuSyMetaTileEntities.STEAM_BOILER_LIQUID_STEEL.getStackForm(), semiFluidMapId);

        String solidMapId = GTValues.MODID + ":" + SuSyRecipeMaps.BOILER_RECIPES.getUnlocalizedName();

        registry.addRecipeCatalyst(SuSyMetaTileEntities.LARGE_BRONZE_BOILER.getStackForm(), solidMapId);
        registry.addRecipeCatalyst(SuSyMetaTileEntities.LARGE_STEEL_BOILER.getStackForm(), solidMapId);
        registry.addRecipeCatalyst(SuSyMetaTileEntities.STEAM_BOILER_COAL_BRONZE.getStackForm(), solidMapId);
        registry.addRecipeCatalyst(SuSyMetaTileEntities.STEAM_BOILER_COAL_STEEL.getStackForm(), solidMapId);

        registry.addRecipes(RocketFuelEntry.getFuelRegistry().values().stream().map(RocketFuelWrapper::new)
                .collect(Collectors.toList()), RocketFuelCategory.UID);
        registry.addRecipeCatalyst(SuSyMetaTileEntities.LAUNCH_PAD.getStackForm(), RocketFuelCategory.UID);
        String largeRESMapId = GTValues.MODID + ":" + SuSyRecipeMaps.LARGE_RES_RECIPES.getUnlocalizedName();
        registry.addRecipeCatalyst(SuSyMetaTileEntities.LARGE_RES.getStackForm(), largeRESMapId);

        String strandCastingId = GTValues.MODID + ":strand_casting";
        registry.addRecipes(CAST_MATERIALS.stream().map(StrandInfo::new).toList(), strandCastingId);
        registry.addRecipeCatalyst(SuSyMetaTileEntities.TURNING_ZONE.getStackForm(), strandCastingId);
        registry.addRecipeCatalyst(SuSyMetaTileEntities.ROLLING_MILL.getStackForm(), strandCastingId);
        registry.addRecipeCatalyst(SuSyMetaTileEntities.CLUSTER_MILL.getStackForm(), strandCastingId);
        registry.addRecipeCatalyst(SuSyMetaTileEntities.FLYING_SHEAR.getStackForm(), strandCastingId);
        registry.addRecipeCatalyst(SuSyMetaTileEntities.SLAB_MOLD.getStackForm(), strandCastingId);
        registry.addRecipeCatalyst(SuSyMetaTileEntities.BILLET_MOLD.getStackForm(), strandCastingId);
        registry.addRecipeCatalyst(SuSyMetaTileEntities.STRAND_COOLER.getStackForm(), strandCastingId);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new RocketFuelCategory(registry.getJeiHelpers().getGuiHelper()));
        registry.addRecipeCategories(new StrandCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerCollapsibleGroups(@NonNull ICollapsibleGroupRegistry registry) {
        buildTanklessFluidPipeGroup(registry);
    }

    private static void buildTanklessFluidPipeGroup(@NonNull ICollapsibleGroupRegistry registry) {
        List<ItemStack> stacks = new ArrayList<>();
        for (val blocks : TANKLESS_FLUID_PIPES.values()) {
            for (val block : blocks) {
                addSubBlocks(stacks, block);
            }
        }
        addGroup(registry, "tankless_fluid_pipes", stacks);
    }


    @NullMarked
    private static void addSubBlocks(List<ItemStack> out, Block block) {
        NonNullList<ItemStack> sub = NonNullList.create();
        block.getSubBlocks(CreativeTabs.SEARCH, sub);
        out.addAll(sub);
    }

    @NullMarked
    private static void addGroup(ICollapsibleGroupRegistry registry,
                                 String id,
                                 Collection<ItemStack> stacks) {
        registry.newGroup(Supersymmetry.MODID + ":" + id, Supersymmetry.MODID + ".jei.group." + id)
                .add(stacks.toArray())
                .build();
    }
}
