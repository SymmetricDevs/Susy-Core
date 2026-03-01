package supersymmetry.common.metatileentities.multi.electric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.recipeproperties.IRecipePropertyStorage;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.api.recipes.properties.MixerSettlerCellsProperty;

public class MetaTileEntityMixerSettlerV2 extends RecipeMapMultiblockController {

    public static final int MIN_CELLS = 2;
    public static final int MAX_CELLS = 20;
    private int sDist;

    public MetaTileEntityMixerSettlerV2(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.MIXER_SETTLER_RECIPES);
        this.recipeMapWorkable = new MixerSettlerRecipeLogic(this);
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    protected boolean updateStructureDimensions() {
        World world = getWorld();
        // controller is facing outwards so right is left
        EnumFacing left = RelativeDirection.RIGHT.getRelativeFacing(getFrontFacing(), getUpwardsFacing(), isFlipped());
        EnumFacing right = left.getOpposite();
        EnumFacing back = left.rotateY();

        BlockPos.MutableBlockPos rPos = new BlockPos.MutableBlockPos(getPos());
        rPos.move(back);
        rPos.move(left); // start on left edge so we only need to move 3 at a time

        int cells = 0;
        for (int i = MIN_CELLS; i <= MAX_CELLS; i += 2) {
            rPos.move(right, 3);
            // checking for a MTE with fluid output multiblock ability seems annoying,
            // so we go until we don't see a PTFE pipe
            if (world.getBlockState(rPos) != getPipeCasingState()) {
                cells = i;
                break;
            }
        }

        if (cells < MIN_CELLS) {
            invalidateStructure();
            return false;
        }

        this.sDist = cells;

        if (!this.getWorld().isRemote) {
            writeCustomData(GregtechDataCodes.UPDATE_STRUCTURE_SIZE, buf -> {
                buf.writeInt(this.sDist);
            });
        }
        return true;
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.UPDATE_STRUCTURE_SIZE) {
            this.sDist = buf.readInt();
        }
    }

    @Override
    public void checkStructurePattern() {
        if (updateStructureDimensions()) {
            reinitializeStructurePattern();
        }
        super.checkStructurePattern();
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

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        if (getWorld() != null) {
            updateStructureDimensions();
        }
        if (sDist < MIN_CELLS) sDist = MIN_CELLS;
        return createStructurePattern(sDist);
    }

    private @NotNull String[][] buildPatternStrings(int cells) {
        // 1 2 3
        // FF CC
        // CC CC
        // CC CC
        // .. PP
        // CC CC GG
        final int width = cells / 2 * 3 + 1;
        final int depth = 5;
        char[][] layer0 = new char[depth][width];
        char[][] layer1 = new char[depth][width];
        char[][] layer2 = new char[depth][width];

        for (int x = 0; x < width; x++) {
            layer0[0][x] = (x == 0 || x == width - 1) ? ' ' : 'F'; // frames at the bottom back

            // tank casings
            layer0[1][x] = (x % 3 == 0) ? ' ' : 'C';
            layer0[2][x] = (x % 3 == 0) ? ' ' : 'C';
            layer1[0][x] = (x % 3 == 0) ? ' ' : 'C';
            layer1[1][x] = (x % 3 == 0) ? ' ' : 'C';
            layer1[2][x] = (x % 3 == 0) ? ' ' : 'C';

            // mixer-tank connecting pipes
            layer1[3][x] = (x % 3 == 0) ? ' ' : 'P';

            if (x == 0) {
                // top input, bottom output on left
                layer0[4][x] = 'O';
                layer1[4][x] = 'I';
            } else if (x == width - 1) {
                // top output, bottom input on right
                layer0[4][x] = 'I';
                layer1[4][x] = 'O';
            } else {
                // mixer casings & connecting pipes
                layer0[4][x] = (x % 3 == 0) ? 'P' : 'C';
                layer1[4][x] = (x % 3 == 0) ? 'P' : 'C';
            }

            // top row with casing on left, then 2 gearbox + frame for each cell pair
            layer2[4][x] = x == 0 ? 'C' : (x % 3 == 0 ? 'F' : 'G');
        }

        // empty sections under the pipes and above the tanks
        Arrays.fill(layer0[3], ' ');
        Arrays.fill(layer2[0], ' ');
        Arrays.fill(layer2[1], ' ');
        Arrays.fill(layer2[2], ' ');
        Arrays.fill(layer2[3], ' ');

        var layers = new String[3][depth];
        for (int z = 0; z < depth; z++) {
            layers[0][z] = new String(layer0[z]);
            layers[1][z] = new String(layer1[z]);
            layers[2][z] = new String(layer2[z]);
        }
        return layers;
    }

    protected @NotNull BlockPattern createStructurePattern(int sDist) {
        String[][] layers = buildPatternStrings(sDist);
        var pad = String.format("%-" + ((sDist / 2 - 1) * 3 + 1) + "s", "");
        return FactoryBlockPattern.start()
                .aisle(layers[0][0], layers[1][0], layers[2][0])
                .aisle(layers[0][1], layers[1][1], layers[2][1])
                .aisle(layers[0][2], layers[1][2], layers[2][2])
                .aisle(layers[0][3], layers[1][3], layers[2][3])
                .aisle(layers[0][4], layers[1][4], layers[2][4])
                .aisle(" XX" + pad, " SX" + pad, " XX" + pad) // control panel
                .where('S', selfPredicate())
                .where('X', states(getCasingState()).or(autoAbilities()))
                .where('I', abilities(MultiblockAbility.IMPORT_FLUIDS))
                .where('O', abilities(MultiblockAbility.EXPORT_FLUIDS))
                .where('P', states(getPipeCasingState()))
                .where('C', states(getCasingState()))
                .where('G',
                        states(MetaBlocks.TURBINE_CASING
                                .getState(BlockTurbineCasing.TurbineCasingType.STAINLESS_STEEL_GEARBOX)))
                .where('F', frames(Materials.StainlessSteel))
                .where(' ', any())
                .build();
    }

    public TraceabilityPredicate autoAbilities() {
        return super.autoAbilities(true, false)
                .or(abilities(MultiblockAbility.INPUT_ENERGY).setPreviewCount(1).setMinGlobalLimited(1)
                        .setMaxGlobalLimited(2))
                .or(abilities(MultiblockAbility.IMPORT_ITEMS).setPreviewCount(1).setMaxGlobalLimited(1))
                .or(abilities(MultiblockAbility.EXPORT_ITEMS).setPreviewCount(1).setMaxGlobalLimited(1));
    }

    protected static IBlockState getPipeCasingState() {
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.POLYTETRAFLUOROETHYLENE_PIPE);
    }

    public IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityMixerSettlerV2(metaTileEntityId);
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        if (this.structurePattern == null) {
            this.reinitializeStructurePattern();
            if (this.structurePattern == null) {
                return Collections.emptyList();
            }
        }

        List<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
        int radius = Math.max(sDist, MIN_CELLS);
        while (radius <= MAX_CELLS) {
            shapeInfo.add(
                    new MultiblockShapeInfo(createStructurePattern(radius).getPreview(new int[] { 1, 1, 1, 1, 1, 1 })));
            radius += 2;
        }
        return shapeInfo;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(TextFormatting.AQUA + I18n.format("susy.machine.mixer_settler.tooltip.1"));
        tooltip.add(TextFormatting.AQUA + I18n.format("susy.machine.mixer_settler.tooltip.2"));
    }

    private class MixerSettlerRecipeLogic extends MultiblockRecipeLogic {

        public MixerSettlerRecipeLogic(MetaTileEntityMixerSettlerV2 metaTileEntityMixerSettler) {
            super(metaTileEntityMixerSettler);
        }

        @Override
        protected void modifyOverclockPost(int[] overclockResults, @NotNull IRecipePropertyStorage storage) {
            int cellsOff = (sDist - storage.getRecipePropertyValue(MixerSettlerCellsProperty.getInstance(), 2)) / 2;
            // Divides the duration by an increasing factor that approaches 2 as the number of cells approaches
            // infinity.
            overclockResults[1] = (int) ((double) overclockResults[1] / (Math.atan(cellsOff + 1) * 4 / Math.PI));

            super.modifyOverclockPost(overclockResults, storage);
        }

        public int getParallelLimit() {
            return 16;
        }

        @Override
        public boolean checkRecipe(@NotNull Recipe recipe) {
            int cellsOff = (sDist - recipe.getRecipePropertyStorage()
                    .getRecipePropertyValue(MixerSettlerCellsProperty.getInstance(), 2));
            if (cellsOff < 0) {
                return false;
            }
            return super.checkRecipe(recipe);
        }
    }
}
