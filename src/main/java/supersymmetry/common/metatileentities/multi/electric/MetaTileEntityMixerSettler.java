package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.recipeproperties.IRecipePropertyStorage;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.api.recipes.properties.MixerSettlerCellsProperty;
import supersymmetry.common.blocks.BlockSuSyMultiblockCasing;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nonnull;

public class MetaTileEntityMixerSettler extends RecipeMapMultiblockController {
    public static final int MIN_RADIUS = 4;
    private int sDist;

    public MetaTileEntityMixerSettler(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.MIXER_SETTLER_RECIPES);
        this.recipeMapWorkable = new MixerSettlerRecipeLogic(this);
    }

    protected boolean updateStructureDimensions() {
        World world = getWorld();
        EnumFacing left = RelativeDirection.LEFT.getRelativeFacing(getFrontFacing(), getUpwardsFacing(), isFlipped());
        EnumFacing right = left.getOpposite();

        BlockPos.MutableBlockPos lPos = new BlockPos.MutableBlockPos(getPos());
        BlockPos.MutableBlockPos rPos = new BlockPos.MutableBlockPos(getPos());

        // find the distances from the controller to the secondary casing
        int sDist = 0;

        for (int i = 1; i < 9; i++) { // start at 1 for an off-by-one error
            if (isBlockEdge(world, lPos, left) & isBlockEdge(world, rPos, right)) {
                sDist = i; // The & is absolutely *essential* here.
                break;
            }
        }


        if (sDist < MIN_RADIUS) {
            invalidateStructure();
            return false;
        }

        this.sDist = sDist;

        if (!this.getWorld().isRemote) {
            writeCustomData(GregtechDataCodes.UPDATE_STRUCTURE_SIZE, buf -> {
                buf.writeInt(this.sDist);
            });
        }
        return true;
    }

    public boolean isBlockEdge(@Nonnull World world, @Nonnull BlockPos.MutableBlockPos pos, @Nonnull EnumFacing direction) {
        return world.getBlockState(pos.move(direction)) == getSecondaryCasingState();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.UPDATE_STRUCTURE_SIZE) {
            this.sDist = buf.readInt();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setInteger("sDist", this.sDist);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.sDist = data.getInteger("sDist");
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.sDist);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.sDist = buf.readInt();
        reinitializeStructurePattern();
    }

    public String[] buildVoxelStrings(int sDist) {
        // Each aisle has 5 strings, so we need 25 total strings
        String[] result = new String[25];
        int index = 0;

        // Build strings for each aisle
        for (int aisle = 0; aisle < 5; aisle++) {
            for (int layer = 0; layer < 5; layer++) {
                result[index++] = buildString(aisle, layer, sDist);
            }
        }

        return result;
    }

    private String buildString(int aisle, int layer, int sDist) {
        // Define patterns for each aisle and layer combination
        switch (aisle) {
            case 0: // First aisle
                switch (layer) {
                    case 0: case 4: // "ECCCCCCCE"
                        return buildRepeatingString('E', 'E', "CCCC", sDist);
                    case 1: case 2: case 3: // "GGGGGGGGG"
                        return buildRepeatingString('G', 'G', "GGGG", sDist);
                }
                break;

            case 1: // Second aisle
                switch (layer) {
                    case 0: // "ECCCCCCCE"
                        return buildRepeatingString('E', 'E', "CCCC", sDist);
                    case 1: // "G P G P G"
                        return buildRepeatingString('G', 'G', " P G", sDist);
                    case 2: // "GFG GFG G"
                        return buildRepeatingString('G', 'G', "FG G", sDist);
                    case 3: // "IFG PFG O"
                        return buildRepeatingString('I', 'O', "FG P", sDist);
                    case 4: // "EMCCCMCCE"
                        return buildRepeatingString('E', 'E', "MCCC", sDist);
                }
                break;

            case 2: // Third aisle
                switch (layer) {
                    case 0: case 4: // "ECCCCCCCE"
                        return buildRepeatingString('E', 'E', "CCCC", sDist);
                    case 1: case 2: case 3: // "GDGDGDGDG" or "GTGTGTGTG"
                        char repeatChar = (layer == 3) ? 'T' : 'D';
                        return buildRepeatingString('G', 'G', repeatChar + "G" + repeatChar + "G", sDist);
                }
                break;

            case 3: // Fourth aisle
                switch (layer) {
                    case 0: // "ECCCCCCCE"
                        return buildRepeatingString('E', 'E', "CCCC", sDist);
                    case 1: // "O G P G I"
                        return buildRepeatingString('O', 'I', " G P", sDist);
                    case 2: // "G GFG GFG"
                        return buildRepeatingString('G', 'G', " GFG", sDist);
                    case 3: // "G PFG PFG"
                        return buildRepeatingString('G', 'G', " PFG", sDist);
                    case 4: // "ECCMCCCME"
                        return buildRepeatingString('E', 'E', "CCMC", sDist);
                }
                break;

            case 4: // Fifth aisle
                switch (layer) {
                    case 0: // "ECCCSCCCE" - special case
                        return buildSpecialCenterString(sDist);
                    case 1: case 2: case 3: // "GGGGGGGGG"
                        return buildRepeatingString('G', 'G', "GGGG", sDist);
                    case 4: // "ECCCCCCCE"
                        return buildRepeatingString('E', 'E', "CCCC", sDist);
                }
                break;
        }

        return ""; // Should never reach here
    }

    private String buildRepeatingString(char first, char last, String repeatingUnit, int sDist) {
        StringBuilder sb = new StringBuilder();

        // Add first character
        sb.append(first);

        // Add repeating units
        for (int i = 0; i < sDist / 2; i++) {
            if (i == sDist / 2 - 1) {
                // Last repetition: skip last character of repeating unit
                sb.append(repeatingUnit.substring(0, repeatingUnit.length() - 1));
            } else {
                // Middle repetitions: use full repeating unit
                sb.append(repeatingUnit);
            }
        }

        // Add last character
        sb.append(last);

        return sb.toString();
    }

    private String buildSpecialCenterString(int sDist) {
        StringBuilder sb = new StringBuilder();

        // Build "ECCCSCCCE" -> "ECCCCCSCCCCCE" pattern
        sb.append('E');
        for (int i = 0; i < sDist - 1; i++) {
            sb.append('C');
        }
        sb.append('S');
        for (int i = 0; i < sDist - 1; i++) {
            sb.append('C');
        }
        sb.append('E');

        return sb.toString();
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        if (getWorld() != null) updateStructureDimensions();
        if (sDist < MIN_RADIUS) sDist = MIN_RADIUS; // Set up JEI correctly
        String[] strings = buildVoxelStrings(sDist);

        return FactoryBlockPattern.start()
                .aisle(strings[0], strings[1], strings[2], strings[3], strings[4])
                .aisle(strings[5], strings[6], strings[7], strings[8], strings[9])
                .aisle(strings[10], strings[11], strings[12], strings[13], strings[14])
                .aisle(strings[15], strings[16], strings[17], strings[18], strings[19])
                .aisle(strings[20], strings[21], strings[22], strings[23], strings[24])
                /*.aisle("ECCCCCCCE", "GGGGGGGGG", "GGGGGGGGG", "GGGGGGGGG", "ECCCCCCCE")
                .aisle("ECCCCCCCE", "G P G P G", "GFG GFG G", "IFG PFG O", "EMCCCMCCE")
                .aisle("ECCCCCCCE", "GDGDGDGDG", "GDGDGDGDG", "GTGTGTGTG", "ECCCCCCCE")
                .aisle("ECCCCCCCE", "O G P G I", "G GFG GFG", "G PFG PFG", "ECCMCCCME")
                .aisle("ECCCSCCCE", "GGGGGGGGG", "GGGGGGGGG", "GGGGGGGGG", "ECCCCCCCE")*/
                .where('S', selfPredicate())
                .where('I', abilities(MultiblockAbility.IMPORT_FLUIDS))
                .where('O', abilities(MultiblockAbility.EXPORT_FLUIDS))
                .where('T', states(SuSyBlocks.MULTIBLOCK_CASING.getState(BlockSuSyMultiblockCasing.CasingType.SIEVE_TRAY)))
                .where('P', states(getPipeCasingState()))
                //.where('B', abilities(MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.EXPORT_ITEMS))
                .where('D', states(getCasingState()))
                .where('C', states(getCasingState()).setMinGlobalLimited(50).or(autoAbilities(true, true, true, true, false, false, false)))
                .where('G', states(getGlassState()))
                .where('M', states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STAINLESS_STEEL_GEARBOX)))
                .where('F', frames(Materials.StainlessSteel))
                .where('E', states(getSecondaryCasingState()))
                .where(' ', air())
                .build();
    }


    protected static IBlockState getGlassState() {
        return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.TEMPERED_GLASS);
    }

    protected static IBlockState getPipeCasingState() {
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.POLYTETRAFLUOROETHYLENE_PIPE);
    }

    public IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN);
    }

    public IBlockState getSecondaryCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }


    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityMixerSettler(metaTileEntityId);
    }


    private class MixerSettlerRecipeLogic extends MultiblockRecipeLogic {
        public MixerSettlerRecipeLogic(MetaTileEntityMixerSettler metaTileEntityMixerSettler) {
            super(metaTileEntityMixerSettler);
        }

        @Override
        protected void modifyOverclockPost(int[] overclockResults, @NotNull IRecipePropertyStorage storage) {
            int cellsOff = storage.getRecipePropertyValue(MixerSettlerCellsProperty.getInstance(), 2) - (sDist / 2);
            // Divides the duration by an increasing factor that approaches 2 as the number of cells approaches infinity.
            overclockResults[1] = (int) ((double) overclockResults[1] / (Math.atan(cellsOff + 1) * 4 / Math.PI));

            super.modifyOverclockPost(overclockResults, storage);
        }
    }
}
