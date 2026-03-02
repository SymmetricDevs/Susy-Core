package supersymmetry.common.metatileentities.multi.electric;

import static net.minecraft.block.BlockDirectional.FACING;
import static supersymmetry.api.metatileentity.multiblock.SuSyPredicates.eccentricRolls;
import static supersymmetry.api.metatileentity.multiblock.SuSyPredicates.metalSheets;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.*;
import gregtech.api.pattern.*;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import supersymmetry.api.blocks.IAnimatablePartBlock;
import supersymmetry.api.metatileentity.multiblock.SuSyPredicates;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockGrinderCasing;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.metatileentities.SuSyMetaTileEntities;

public class MetaTileEntityEccentricRollCrusher extends RecipeMapMultiblockController {

    /**
     * Identifier used for input hatch base textures
     * -1 for None, 0~15 for normal metal sheets and 16~31 for large ones.
     * This is getting on server-side and sync to client side later.
     */
    protected byte metalSheetIdentifier = -1;

    /**
     * List of animatable blocks
     * Much like {@link #variantActiveBlocks} in vanilla CEu
     */
    protected List<BlockPos> ercRolls;

    /**
     * The axis direction of the eccentric roll (rotates CCW)
     * Only gets updated on the server-side
     * 
     * @see #updateRollOrientation()
     */
    protected EnumFacing rollOrientation = EnumFacing.DOWN;

    public MetaTileEntityEccentricRollCrusher(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap) {
        super(metaTileEntityId, recipeMap);
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return SusyTextures.ECCENTRIC_ROLL_CRUSHER_OVERLAY;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityEccentricRollCrusher(metaTileEntityId, recipeMap);
    }

    private static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    private static IBlockState getPipeCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    private static IBlockState getGearBoxState() {
        return MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX);
    }

    private static IBlockState getPistonState() {
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE);
    }

    private static IBlockState getJawCasingState() {
        return SuSyBlocks.GRINDER_CASING.getState(BlockGrinderCasing.Type.ABRASION_RESISTANT_CASING);
    }

    private static IBlockState getHydraulicGearboxState() {
        return SuSyBlocks.GRINDER_CASING.getState(BlockGrinderCasing.Type.HYDRAULIC_MECHANICAL_GEARBOX);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        if (getWorld() != null) updateRollOrientation();

        TraceabilityPredicate casings = states(getCasingState()).setMinGlobalLimited(22);
        TraceabilityPredicate metalSheets = metalSheets();

        return FactoryBlockPattern.start()
                .aisle("  CCDC  ", "  CCGC  ", "  CCMX  ", "    MMX ", "     MMX")
                .aisle("JJJJ#CD ", "JHJ#R#C ", "  J##M  ", "   J##M ", "     ##N")
                .aisle("  BJ#CD ", " BJ#R#C ", " PJ##M  ", " P J##M ", "     ##N")
                .aisle("JJJJ#CD ", "JHJ#R#C ", "  J##M  ", "   J##M ", "     ##N")
                .aisle("  CCDC  ", "  CSGC  ", "  CCMX  ", "    MMX ", "     MMX")
                .where(' ', any())
                .where('#', air())
                .where('B', states(getPipeCasingState()))
                .where('C', casings.or(autoAbilities(true, true,
                        false, false, true, true, false)))
                .where('D', casings.or(abilities(MultiblockAbility.EXPORT_ITEMS)))
                .where('G', states(getGearBoxState()))
                .where('H', states(getHydraulicGearboxState()))
                .where('J', states(getJawCasingState()))
                .where('R', eccentricRolls(rollOrientation))
                .where('P', states(getPistonState()))
                .where('X', frames(Materials.Steel))
                .where('S', selfPredicate())
                .where('M', metalSheets)
                .where('N', metalSheets.or(abilities(MultiblockAbility.IMPORT_ITEMS)))
                .build();
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();

        MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder()
                .aisle("  CCCC  ", "  CCGC  ", "  CCMX  ", "    MMX ", "     MMX")
                .aisle("JJJJ CC ", "JHJ R C ", "  J  M  ", "   J  M ", "       M")
                .aisle("  BJ CO ", " BJ R C ", " PJ  M  ", " P J  M ", "       I")
                .aisle("JJJJ CC ", "JHJ R C ", "  J  M  ", "   J  M ", "       M")
                .aisle("  CECK  ", "  CSGC  ", "  CCMX  ", "    MMX ", "     MMX")
                .where(' ', Blocks.AIR.getDefaultState())
                .where('B', getPipeCasingState())
                .where('C', getCasingState())
                .where('G', getGearBoxState())
                .where('H', getHydraulicGearboxState())
                .where('J', getJawCasingState())
                .where('R', SuSyBlocks.ECCENTRIC_ROLL.getDefaultState()
                        .withProperty(FACING, EnumFacing.SOUTH))
                .where('P', getPistonState())
                .where('X', MetaBlocks.FRAMES.get(Materials.Steel).getBlock(Materials.Steel))
                .where('S', SuSyMetaTileEntities.ECCENTRIC_ROLL_CRUSHER, EnumFacing.SOUTH)
                .where('K', MetaTileEntities.MAINTENANCE_HATCH, EnumFacing.SOUTH)
                .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.LV], EnumFacing.SOUTH)
                .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.LV], EnumFacing.EAST)
                .where('O', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.LV], EnumFacing.EAST);

        for (EnumDyeColor color : EnumDyeColor.values()) {
            shapeInfo.add(builder.where('M', MetaBlocks.LARGE_METAL_SHEET.getState(color)).build());
            shapeInfo.add(builder.where('M', MetaBlocks.METAL_SHEET.getState(color)).build());
        }
        return shapeInfo;
    }

    /**
     * Gets the correct axis direction of the roll
     * 
     * @see SuSyPredicates#eccentricRolls(EnumFacing)
     */
    public void updateRollOrientation() {
        World world = getWorld();

        EnumFacing front = getFrontFacing();
        EnumFacing back = front.getOpposite();
        EnumFacing left = front.rotateYCCW();

        IBlockState state = world.getBlockState(getPos().offset(left));
        this.rollOrientation = state == getGearBoxState() ? front : back;
    }

    @Override
    public void checkStructurePattern() {
        if (!this.isStructureFormed()) {
            reinitializeStructurePattern();
        }
        super.checkStructurePattern();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        this.metalSheetIdentifier = context.get("MetalSheet");

        /// This has to be called before [MultiblockWithDisplayBase#formStructure(PatternMatchContext)] calls
        /// where [#replaceVariantBlocksActive(boolean)] is called
        /// @see [#setAnimatablesActive(boolean)]
        this.ercRolls = context.getOrDefault("ERC_Rolls", new LinkedList<>());
        super.formStructure(context);
        World world = getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.UPDATE_COLOR,
                    buf -> buf.writeByte(metalSheetIdentifier));
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.metalSheetIdentifier = -1;
        this.rollOrientation = EnumFacing.DOWN;
        World world = getWorld();
        if (world != null && !world.isRemote) {
            /// [GregtechDataCodes#UPDATE_COLOR] is only used for [MetaTileEntityFusionReactor]
            /// at the moment, so this should be fine.
            writeCustomData(GregtechDataCodes.UPDATE_COLOR,
                    buf -> buf.writeByte(metalSheetIdentifier));
        }
    }

    @Override
    protected void replaceVariantBlocksActive(boolean isActive) {
        super.replaceVariantBlocksActive(isActive);
        setAnimatablesActive(isActive);
    }

    /**
     * Set animatable blocks active or inactive
     *
     * @param isActive whether the multi is active
     */
    protected void setAnimatablesActive(boolean isActive) {
        if (ercRolls != null && !ercRolls.isEmpty()) {
            World world = getWorld();
            for (BlockPos pos : ercRolls) {
                IBlockState state = world.getBlockState(pos);
                // In case that it is air (or somehow replaced)
                if (state.getProperties().containsKey(IAnimatablePartBlock.ACTIVE)) {
                    world.setBlockState(pos, state.withProperty(IAnimatablePartBlock.ACTIVE, isActive));
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ICubeRenderer getBaseTexture(IMultiblockPart part) {
        if (metalSheetIdentifier >= 0 && part instanceof IMultiblockAbilityPart<?>abilityPart &&
                abilityPart.getAbility() == MultiblockAbility.IMPORT_ITEMS) {
            return SusyTextures.METAL_SHEETS[metalSheetIdentifier];
        }
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == GregtechDataCodes.UPDATE_COLOR) {
            this.metalSheetIdentifier = buf.readByte();
        } else {
            super.receiveCustomData(dataId, buf);
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(metalSheetIdentifier);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.metalSheetIdentifier = buf.readByte();
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }
}
