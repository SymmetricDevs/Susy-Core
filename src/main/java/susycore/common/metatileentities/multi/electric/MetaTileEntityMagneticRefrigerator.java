package susycore.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.capability.IHeatingCoil;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.*;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.recipeproperties.TemperatureProperty;
import gregtech.api.util.GTUtility;
import gregtech.api.worldgen.bedrockFluids.BedrockFluidVeinHandler;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockWireCoil;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import susycore.common.blocks.BlockCoolingCoil;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import susycore.api.capability.impl.CoolingCoilRecipeLogic;
import susycore.common.blocks.BlockCoolingCoil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

import static gregtech.GregTechMod.proxy;
import static gregtech.api.GregTechAPI.HEATING_COILS;

public class MetaTileEntityMagneticRefrigerator extends RecipeMapMultiblockController implements IHeatingCoil{

    /* Init cooling coils exist */

    public static final Object2ObjectOpenHashMap<IBlockState, IHeatingCoilBlockStats> COOLING_COILS = new Object2ObjectOpenHashMap<>();

    BlockCoolingCoil.CoolingCoilType type : BlockCoolingCoil.CoolingCoilType.values()) {
        COOLING_COILS.put(MetaBlocks.COOLING_COIL.getState(type), type);
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        proxy.onPostLoad();
        BedrockFluidVeinHandler.recalculateChances(true);
        // registers coil types for the BlastTemperatureProperty used in Blast Furnace Recipes
        // runs AFTER craftTweaker
        for (Map.Entry<IBlockState, IHeatingCoilBlockStats> entry : GregTechAPI.COOLING_COILS.entrySet()) {
            IHeatingCoilBlockStats value = entry.getValue();
            if (value != null) {
                String name = entry.getKey().getBlock().getTranslationKey();
                if (!name.endsWith(".name")) name = String.format("%s.name", name);
                TemperatureProperty.registerCoilType(value.getCoilTemperature(), value.getMaterial(), name);
            }
        }
    }

    public static Supplier<TraceabilityPredicate> COOLING_COILS = () -> new TraceabilityPredicate(blockWorldState -> {
        IBlockState blockState = blockWorldState.getBlockState();
        if (susycore.COOLING_COILS.containsKey(blockState)) {
            IHeatingCoilBlockStats stats = GregTechAPI.COOLING_COILS.get(blockState);
            Object currentCoil = blockWorldState.getMatchContext().getOrPut("CoolingCoilType", stats);
            if (!currentCoil.equals(stats)) {
                blockWorldState.setError(new PatternStringError("gregtech.multiblock.pattern.error.coils"));
                return false;
            }
            blockWorldState.getMatchContext().getOrPut("VABlock", new LinkedList<>()).add(blockWorldState.getPos());
            return true;
        }
        return false;
    });

    public static TraceabilityPredicate coolingCoils() {
        return TraceabilityPredicate.COOLING_COILS.get();
    }

    /* Init cooling coils exist */

    private int magneticRefrigeratorTemperature;

    public MetaTileEntityMagneticRefrigerator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.REFRIGERATION_RECIPES);
        this.recipeMapWorkable = new CoolingCoilRecipeLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMagneticRefrigerator(metaTileEntityId);
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        if (isStructureFormed()) {
            textList.add(new TextComponentTranslation("susycore.multiblock.magnetic_refrigerator.min_temperature",
                    new TextComponentTranslation(GTUtility.formatNumbers(magneticRefrigeratorTemperature) + "K").setStyle(new Style().setColor(TextFormatting.RED))));
        }
        super.addDisplayText(textList);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        Object type = context.get("CoolingCoilType");
        if (type instanceof IHeatingCoilBlockStats) {
            this.magneticRefrigeratorTemperature = ((IHeatingCoilBlockStats) type).getCoilTemperature();
        } else {
            this.magneticRefrigeratorTemperature = BlockCoolingCoil.CoolingCoilType.MANGANESEIRONARSENICPHOSPHIDE.getCoilTemperature();
        }

        this.magneticRefrigeratorTemperature += 100 * Math.max(0, GTUtility.getTierByVoltage(getEnergyContainer().getInputVoltage()) - GTValues.MV);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.magneticRefrigeratorTemperature = 0;
    }

    @Override
    public boolean checkRecipe(@Nonnull Recipe recipe, boolean consumeIfSuccess) {
        return this.magneticRefrigeratorTemperature <= recipe.getProperty(TemperatureProperty.getInstance(), 293);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "CCC", "CCC", "XXX")
                .aisle("XXX", "C#C", "C#C", "XXX")
                .aisle("XSX", "CCC", "CCC", "XXX")
                .where('S', selfPredicate())
                .where('X', states(getCasingState()).setMinGlobalLimited(9)
                        .or(autoAbilities(true, true, true, true, true, true, false)))
                .where('C', heatingCoils())
                .where('#', air())
                .build();
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.ALUMINIUM_FROSTPROOF);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.FROST_PROOF_CASING;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
    }

    @Override
    public int getCurrentTemperature() {
        return this.magneticRefrigeratorTemperature;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.BLAST_FURNACE_OVERLAY;
    }

    @Override
    public boolean canBeDistinct() {
        return true;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return true;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
        MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder()
                .aisle("XEM", "CCC", "CCC", "XXX")
                .aisle("FXD", "C#C", "C#C", "XXX")
                .aisle("ISO", "CCC", "CCC", "XXX")
                .where('X', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.ALUMINIUM_FROSTPROOF))
                .where('S', MetaTileEntities.ELECTRIC_BLAST_FURNACE, EnumFacing.SOUTH)
                .where('#', Blocks.AIR.getDefaultState())
                .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.MV], EnumFacing.NORTH)
                .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.LV], EnumFacing.SOUTH)
                .where('O', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.LV], EnumFacing.SOUTH)
                .where('F', MetaTileEntities.FLUID_IMPORT_HATCH[GTValues.LV], EnumFacing.WEST)
                .where('D', MetaTileEntities.FLUID_EXPORT_HATCH[GTValues.LV], EnumFacing.EAST)
                .where('H', MetaTileEntities.MUFFLER_HATCH[GTValues.LV], EnumFacing.UP)
                .where('M', () -> ConfigHolder.machines.enableMaintenance ? MetaTileEntities.MAINTENANCE_HATCH : MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.ALUMINIUM_FROSTPROOF), EnumFacing.NORTH);
        GregTechAPI.COOLING_COILS.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> entry.getValue().getTier()))
                .forEach(entry -> shapeInfo.add(builder.where('C', entry.getKey()).build())); /* :gregtrolllaugh: */
        return shapeInfo;
    }

    @Nonnull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = super.getDataInfo();
        list.add(new TextComponentTranslation("susycore.multiblock.magnetic_refrigerator.min_temperature",
                new TextComponentTranslation(GTUtility.formatNumbers(magneticRefrigeratorTemperature) + "K").setStyle(new Style().setColor(TextFormatting.RED))));
        return list;
    }
}
