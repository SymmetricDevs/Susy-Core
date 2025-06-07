package supersymmetry.api.metatileentity.multiblock;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/** This class is used to do something client side at each block in a given pattern, this accounts for the direction of the multi.
 * @author h3tR / RMI
 */
public abstract class CachedPatternRecipeMapMultiblock extends RecipeMapMultiblockController  {

    public static final int REFRESH_CACHED_PATTERN = GregtechDataCodes.assignId();

    @SideOnly(Side.CLIENT)
    protected Vec3i[] cachedPattern = new Vec3i[0];

    public CachedPatternRecipeMapMultiblock(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap) {
        super(metaTileEntityId, recipeMap);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        //TODO: getWorld() != null might be redundant here
        if(!getWorld().isRemote)
            writeCustomData(REFRESH_CACHED_PATTERN, buf ->{
                buf.writeEnumValue(this.frontFacing.getOpposite());
                buf.writeBoolean(this.isFlipped);
            });
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if(dataId == REFRESH_CACHED_PATTERN) {
            EnumFacing facing =  buf.readEnumValue(EnumFacing.class);
            boolean isFlipped = buf.readBoolean();
            //Only generate the cachedFluidPattern on the client as it isn't used anywhere on the server
            this.cachedPattern = generateCachedPattern(getPattern(), getPatternOffset(), facing, isFlipped);
        }
    }

    @SideOnly(Side.CLIENT)
    protected abstract String[][] getPattern();

    @SideOnly(Side.CLIENT)
    protected abstract Vec3i getPatternOffset();


    //This should never be overwritten as it is not supported
    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }


    /**
     * @param pattern The pattern it should cache positions for, this is similar to {@link gregtech.api.pattern.FactoryBlockPattern} aisles but the layers are horizontal rather than vertical.<br>
     *                It will count a space as empty and anything else as valid (rows don't require trailing spaces, they can be cut off or left empty for empty rows). <br>
     *                Example: see usage in {@link supersymmetry.common.metatileentities.multi.electric.MetaTileEntityClarifier} or {@link supersymmetry.common.metatileentities.multi.electric.MetaTileEntityGravitySeparator}
     * @param patternOffset The offset the pattern is generated in.
     * @param facing which direction the pattern should be generated in (Only Horizontal directions are allowed)
     * @param isFlipped Works the same as {@link gregtech.api.metatileentity.multiblock.MultiblockControllerBase#isFlipped()}
     *
     * @return An array of positions that correspond with the pattern at given offset and direction/facing
     *
     * @throws IllegalArgumentException when Vertical facing directions are used
     */

    @SideOnly(Side.CLIENT)
    public static Vec3i[] generateCachedPattern(String[][] pattern, Vec3i patternOffset, EnumFacing facing, boolean isFlipped) {
        if (facing == EnumFacing.UP || facing == EnumFacing.DOWN)
            throw new IllegalArgumentException("Vertical facing not allowed: " + facing);

        List<Vec3i> cachedPattern = new ArrayList<>();
        for (int y = 0; y < pattern.length; y++) {
            for (int z = 0; z < pattern[y].length; z++) {
                for (int x = 0; x < pattern[y][z].length(); x++) {
                    if(pattern[y][z].charAt(x) == ' ') continue;

                    int patternXOffset = x + patternOffset.getX();
                    int patternZOffset = z + patternOffset.getZ();

                    if(isFlipped ^ (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH))
                        patternXOffset = -patternXOffset;


                    cachedPattern.add(new Vec3i(
                            patternZOffset * facing.getXOffset() + patternXOffset * facing.getZOffset(),
                            patternOffset.getY() + y,
                            patternXOffset * facing.getXOffset() + patternZOffset * facing.getZOffset()
                    ));
                }
            }
        }
        return cachedPattern.toArray(new Vec3i[0]);
    }
}
