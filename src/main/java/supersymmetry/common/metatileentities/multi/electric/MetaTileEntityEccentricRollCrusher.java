package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.metatileentity.multiblock.SuSyPredicates;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockEccentricRoll;

import java.util.LinkedList;
import java.util.List;

import static supersymmetry.api.metatileentity.multiblock.SuSyPredicates.eccentricRolls;
import static supersymmetry.api.metatileentity.multiblock.SuSyPredicates.metalSheets;

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
    protected List<BlockPos> animatables;

    /**
     * The axis direction of the eccentric roll (rotates CCW)
     * Only gets updated on the server-side
     * @see #updateRollOrientation()
     */
    protected EnumFacing rollOrientation = EnumFacing.DOWN;

    public MetaTileEntityEccentricRollCrusher(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap) {
        super(metaTileEntityId, recipeMap);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityEccentricRollCrusher(metaTileEntityId, recipeMap);
    }

    private static IBlockState getCasingState() { // TODO: unique casing...?
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    private static IBlockState getGearBoxState() { // TODO: unique casing...?
        return MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX);
    }

    private static IBlockState getPipeCasingState() { // TODO: unique casing...?
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE);
    }

    private static IBlockState getJawState() { // TODO: unique casing.
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PTFE_INERT_CASING);
    }

    private static IBlockState getMechanicalCasingState() { // TODO: unique casing.
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.POLYTETRAFLUOROETHYLENE_PIPE);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        if (getWorld() != null) updateRollOrientation();

        TraceabilityPredicate casings = states(getCasingState()).setMinGlobalLimited(24);
        TraceabilityPredicate metalSheets = metalSheets();

        return FactoryBlockPattern.start()
                .aisle("  CCDC  ", "  CCGC  ", "  CCMX  ", "    MMX ", "     MMX")
                .aisle("CCCJ#CD ", "CGJ#R#C ", "  J##M  ", "   J##M ", "     ##N")
                .aisle("  PJ#CD ", " PJ#R#C ", " HJ##M  ", " H J##M ", "     ##N")
                .aisle("CCCJ#CD ", "CGJ#R#C ", "  J##M  ", "   J##M ", "     ##N")
                .aisle("  CCDC  ", "  CSGC  ", "  CCMX  ", "    MMX ", "     MMX")
                .where(' ', any())
                .where('#', air())
                .where('C', casings.or(autoAbilities(true, true,
                        false, false, true, true, false)))
                .where('D', casings.or(abilities(MultiblockAbility.EXPORT_ITEMS)))
                .where('G', states(getGearBoxState()))
                .where('P', states(getPipeCasingState()))
                .where('J', states(getJawState()))
                .where('R', eccentricRolls(rollOrientation))
                .where('H', states(getMechanicalCasingState()))
                .where('X', frames(Materials.Steel))
                .where('S', selfPredicate())
                .where('M', metalSheets)
                .where('N', metalSheets.or(abilities(MultiblockAbility.IMPORT_ITEMS)))
                .build();
    }

    /**
     * Gets the correct axis direction of the roll
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

    protected void formStructure(PatternMatchContext context) {
        this.metalSheetIdentifier = context.get("MetalSheet");

        /// This has to be called before [MultiblockWithDisplayBase#formStructure(PatternMatchContext)] calls
        /// where [#replaceVariantBlocksActive(boolean)] is called
        /// @see [#setAnimatablesActive(boolean)]
        this.animatables = context.getOrDefault("Animatable", new LinkedList<>());
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
        if (animatables != null && !animatables.isEmpty()) {
            World world = getWorld();
            for (BlockPos pos : animatables) {
                IBlockState state = world.getBlockState(pos);
                world.setBlockState(pos, state.withProperty(BlockEccentricRoll.ACTIVE, isActive));
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ICubeRenderer getBaseTexture(IMultiblockPart part) {
        if (metalSheetIdentifier >= 0 && part instanceof IMultiblockAbilityPart<?> abilityPart
                && abilityPart.getAbility() == MultiblockAbility.IMPORT_ITEMS) {
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
