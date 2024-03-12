package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import supersymmetry.api.capability.impl.NoEnergyMultiblockRecipeLogic;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockSerpentine;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nonnull;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityHeatRadiator extends RecipeMapMultiblockController {
    public static final int MIN_RADIUS = 1;
    public static final int MIN_HEIGHT = 1;
    private int height;
    private int width;
    private int sDist = 0;
    private int bDist = 0;
    private int area;
    private int rateBonus;

    public MetaTileEntityHeatRadiator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.HEAT_RADIATOR_RECIPES);
        this.recipeMapWorkable = new NoEnergyMultiblockRecipeLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityHeatRadiator(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        if (getWorld() != null && !getWorld().isRemote) updateStructureDimensions();
        if (sDist < MIN_RADIUS) sDist = MIN_RADIUS;
        if (bDist < MIN_HEIGHT) bDist = MIN_HEIGHT;

        // A: Metal Casing; S: Radiator; C: Metal Casing or Hatches; B: Serpentine Block
        StringBuilder bottomBuilder = new StringBuilder();     // ASA
        StringBuilder rowBuilder = new StringBuilder();        // CBC
        StringBuilder topBuilder = new StringBuilder();        // AAA

        // Add Center
        for (int i = 0; i < sDist; i++) {
            if (i == 0) {
                bottomBuilder.append("S");
                rowBuilder.append("B");
                topBuilder.append("A");
            } else {
                bottomBuilder.append("A");
                bottomBuilder.insert(0, "A");
                rowBuilder.append("B");
                rowBuilder.insert(0, "B");
                topBuilder.append("A");
                rowBuilder.insert(0, "A");
            }
        }

        // Add Edges
        bottomBuilder.append("A");
        bottomBuilder.insert(0, "A");
        rowBuilder.append("C");
        rowBuilder.insert(0, "C");
        topBuilder.append("A");
        rowBuilder.insert(0, "A");

        return FactoryBlockPattern.start(RIGHT, FRONT, UP)
                .aisle(topBuilder.toString())
                .aisle(rowBuilder.toString()).setRepeatable(1, bDist)
                .aisle(bottomBuilder.toString())
                .where('S', selfPredicate())
                .where('A', states(MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID))
                        .or(autoAbilities(false, true, false, false, false, false, false)))
                .where('B', states(getRadiatorElementState()))
                .where('C', states(MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID))
                        .or(autoAbilities(false, false, false, false, true, false, false).setMinGlobalLimited(1))
                        .or(autoAbilities(false, false, false, false, false, true, false).setMinGlobalLimited(1)))
                .build();
    }

    protected boolean updateStructureDimensions() {

        World world = getWorld();
        EnumFacing front = getFrontFacing();
        EnumFacing back = front.getOpposite();
        EnumFacing left = front.rotateYCCW();
        EnumFacing right = left.getOpposite();

        BlockPos.MutableBlockPos lPos = new BlockPos.MutableBlockPos(getPos().offset(back)); // Can't have it looking the border to the left and right of the controller.
        BlockPos.MutableBlockPos rPos = new BlockPos.MutableBlockPos(getPos().offset(back));
        BlockPos.MutableBlockPos bPos = new BlockPos.MutableBlockPos(getPos());

        // find the distances from the controller to the plascrete blocks on one horizontal axis and the Y axis
        // repeatable aisles take care of the second horizontal axis
        int sDist = 0;
        int bDist = 0;

        // find the left, right, back, and front distances for the structure pattern
        // maximum size is 15x15 including walls, so check 7 block radius around the controller for blocks
        for (int i = 1; i < 8; i++) {
            if (sDist == 0 && isBlockEdge(world, lPos, left) & isBlockEdge(world, rPos, right))
                sDist = i; // The & is absolutely *essential* here.
            if (bDist == 0 && isBlockEdge(world, bPos, back)) bDist = i;
            if (sDist != 0 && bDist != 0) break;
        }


        if (sDist < MIN_RADIUS || bDist < MIN_RADIUS * 2) {
            invalidateStructure();
            return false;
        }

        this.sDist = sDist;
        this.bDist = bDist;

        writeCustomData(GregtechDataCodes.UPDATE_STRUCTURE_SIZE, buf -> {
            buf.writeInt(this.sDist);
            buf.writeInt(this.bDist);
        });
        return true;
    }

    public boolean isBlockEdge(@Nonnull World world, @Nonnull BlockPos.MutableBlockPos pos, @Nonnull EnumFacing direction) {
        return world.getBlockState(pos.move(direction)) == MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID) || world.getTileEntity(pos) instanceof MetaTileEntityHolder;
    }

    public IBlockState getRadiatorElementState() {
        return SuSyBlocks.SERPENTINE.getState(BlockSerpentine.SerpentineType.BASIC);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.RADIATOR_OVERLAY;
    }
}
