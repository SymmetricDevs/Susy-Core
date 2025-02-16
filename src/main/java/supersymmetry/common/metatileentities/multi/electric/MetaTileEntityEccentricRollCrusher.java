package supersymmetry.common.metatileentities.multi.electric;

import gregicality.multiblocks.common.block.GCYMMetaBlocks;
import gregicality.multiblocks.common.block.blocks.BlockUniqueCasing;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import supersymmetry.client.renderer.textures.SusyTextures;

import static supersymmetry.api.metatileentity.multiblock.SuSyPredicates.metalSheets;

public class MetaTileEntityEccentricRollCrusher extends RecipeMapMultiblockController {

    protected byte metalSheet = -1;

    public MetaTileEntityEccentricRollCrusher(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap) {
        super(metaTileEntityId, recipeMap);
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

    private static IBlockState getRollState() { // TODO: unique casing!!!
        return GCYMMetaBlocks.UNIQUE_CASING.getState(BlockUniqueCasing.UniqueCasingType.CRUSHING_WHEELS);
    }

    private static IBlockState getJewState() { // TODO: unique casing.
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PTFE_INERT_CASING);
    }

    private static IBlockState getMechanicalCasingState() { // TODO: unique casing.
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.POLYTETRAFLUOROETHYLENE_PIPE);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityEccentricRollCrusher(metaTileEntityId, recipeMap);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {

        TraceabilityPredicate casings = states(getCasingState()).setMinGlobalLimited(24); // TODO
        TraceabilityPredicate metalSheets = metalSheets();

        return FactoryBlockPattern.start()
                //      12345678    12345678    12345678    12345678    12345678
                .aisle("  CCDC  ", "  CCGC  ", "  CCMX  ", "    MMX ", "     MMX")
                .aisle("CCCJ#CD ", "CGJ#R#C ", "  J##M  ", "   J##M ", "     ##N")
                .aisle("  PJ#CD ", " PJ#R#C ", " HJ##M  ", " H J##M ", "     ##N")
                .aisle("CCCJ#CD ", "CGJ#R#C ", "  J##M  ", "   J##M ", "     ##N")
                .aisle("  CCDC  ", "  CSGC  ", "  CCMX  ", "    MMX ", "     MMX")
                .where(' ', any())
                .where('#', air())
                .where('C', casings.or(autoAbilities(true, true, false, true, true, true, false)))
                .where('D', casings.or(abilities(MultiblockAbility.EXPORT_ITEMS)))
                .where('G', states(getGearBoxState()))
                .where('P', states(getPipeCasingState()))
                .where('J', states(getJewState()))
                .where('R', states(getRollState()))
                .where('H', states(getMechanicalCasingState()))
                .where('X', frames(Materials.Steel))
                .where('S', selfPredicate())
                .where('M', metalSheets)
                .where('N', metalSheets.or(abilities(MultiblockAbility.IMPORT_ITEMS)))
                .build();
    }

    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.metalSheet = context.get("MetalSheet");
        World world = getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.UPDATE_COLOR, buf -> buf.writeByte(metalSheet));
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.metalSheet = -1;
        World world = getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.UPDATE_COLOR, buf -> buf.writeByte(metalSheet));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ICubeRenderer getBaseTexture(IMultiblockPart part) {
        if (metalSheet >= 0 && part instanceof IMultiblockAbilityPart<?> abilityPart && abilityPart.getAbility() == MultiblockAbility.IMPORT_ITEMS) {
            return SusyTextures.METAL_SHEETS[metalSheet];
        }
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == GregtechDataCodes.UPDATE_COLOR) {
            this.metalSheet = buf.readByte();
        } else {
            super.receiveCustomData(dataId, buf);
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(metalSheet);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.metalSheet = buf.readByte();
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }
}
