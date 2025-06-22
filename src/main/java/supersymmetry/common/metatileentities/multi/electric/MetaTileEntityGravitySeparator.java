package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.items.materialitem.MetaPrefixItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.unification.material.Material;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.metatileentity.multiblock.CachedPatternRecipeMapMultiblock;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.particles.SusyParticleDust;
import supersymmetry.common.blocks.BlockSeparatorRotor;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static gregtech.api.util.RelativeDirection.*;
import static supersymmetry.api.blocks.VariantHorizontalRotatableBlock.FACING;

public class MetaTileEntityGravitySeparator extends CachedPatternRecipeMapMultiblock {

    private static final int UPDATE_MATERIAL_COLOR = GregtechDataCodes.assignId();

    private static final String[][] ROTOR_PATTERN = {{"", "RRR", "", ""},
            {"", "", "", "", "RRR"}};
    private static final Vec3i PATTERN_OFFSET = new Vec3i(-1, 2, 1);

    private int[] particleColors;

    public MetaTileEntityGravitySeparator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.GRAVITY_SEPARATOR_RECIPES);
        this.recipeMapWorkable = new MultiblockRecipeLogic(this) {
            @Override
            protected void setupRecipe(Recipe recipe) {
                super.setupRecipe(recipe);
                updateRenderInfo(recipe);
            }
        };
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        updateRenderInfo(recipeMapWorkable.getPreviousRecipe());
    }

    public void updateRenderInfo(Recipe recipe) {
        if (recipe == null) return;
        Stream<ItemStack> flattenedInputs = recipe.getInputs().stream().map(GTRecipeInput::getInputStacks).map(Arrays::asList).flatMap(List::stream).filter(stack -> MetaPrefixItem.tryGetMaterial(stack) != null);
        Stream<ItemStack> flattenedOutputs = recipe.getOutputs().stream();

        int[] materialColors = Stream.concat(flattenedInputs, flattenedOutputs).map(stack -> Objects.requireNonNull(MetaPrefixItem.tryGetMaterial(stack))).map(Material::getMaterialRGB).mapToInt(Integer::intValue).toArray();

        if (materialColors.length == 0) {
            this.writeCustomData(UPDATE_MATERIAL_COLOR, buf -> buf.writeVarIntArray(new int[]{0xFFFFFF}));
            return;
        }
        this.writeCustomData(UPDATE_MATERIAL_COLOR, buf -> buf.writeVarIntArray(materialColors));

    }


    @Override
    protected String[][] getPattern() {
        return ROTOR_PATTERN;
    }

    @Override
    protected Vec3i getPatternOffset() {
        return PATTERN_OFFSET;
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityGravitySeparator(this.metaTileEntityId);
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        // Different characters use common constraints. Copied from GCYM
        TraceabilityPredicate casingPredicate = states(getCasingState()).setMinGlobalLimited(90);

        return FactoryBlockPattern.start(RIGHT, UP, FRONT)
                //front of R facing right side
                .aisle("C   C", "CC CC", "CFCFC", "CCSCC", " CCC ", " CCC ", "     ").aisle("     ", " OOO ", "C###C", "RRRRR", "C###C", "C   C", "     ").aisle("C   C", "C   C", "MCCCM", "C###C", "RRRRR", "C   C", "     ").aisle("C   C", "C   C", "ECCCE", "RRRRR", "C###C", "C###C", "     ").aisle("     ", "     ", "C   C", "CCCCC", "RRRRR", "C###C", "C###C").aisle("     ", "C   C", "C   C", " CCC ", "C###C", "RRRRR", "J###J").aisle("C   C", "CC CC", "CCCCC", " CCC ", " CCC ", "CCCCC", "CIIIC")
                /* Other orientation
               .aisle("C CC  C", "C CC CC", "CCCECCC", "CRCRC  ", " CRCRC ", " CCCCRC", "    CJC")
               .aisle("       ", "CO    C", "F#CC  C", "CR#RCCC", "C#R#R#C", "C  ##RC", "    ##I")
               .aisle("       ", " O    C", "C#CC  C", "CR#RCCC", "C#R#R#C", "C  ##RC", "    ##I")
               .aisle("       ", "CO    C", "F#CC  C", "CR#RCCC", "C#R#R#C", "C  ##RC", "    ##I")
               .aisle("C CC  C", "C CC CC", "CCCSCCC", "CRCRC  ", " CRCRC ", " CCCCRC", "    CJC")
               */.where('S', selfPredicate()).where('R', rotorOrientation()).where('C', casingPredicate).where('M', casingPredicate.or(autoAbilities(true, false))).where('E', casingPredicate.or(autoAbilities(true, false, false, false, false, false, false))).where('I', casingPredicate.or(autoAbilities(false, false, true, false, false, false, false))).where('O', casingPredicate.or(autoAbilities(false, false, false, true, false, false, false))).where('J', casingPredicate.or(autoAbilities(false, false, false, false, true, false, false))).where('F', casingPredicate.or(autoAbilities(false, false, false, false, false, true, false))).where('#', air()).where(' ', any()).build();
    }

    /* can be reimplemented with states for R if rotation is not supposed to be specified
    public IBlockState[] getRotorStates() {
        return new IBlockState[] {
                SuSyBlocks.SEPARATOR_ROTOR.getState(BlockSeparatorRotor.BlockSeparatorRotorType.STEEL).withProperty(FACING, EnumFacing.SOUTH),
                SuSyBlocks.SEPARATOR_ROTOR.getState(BlockSeparatorRotor.BlockSeparatorRotorType.STEEL).withProperty(FACING, EnumFacing.NORTH),
                SuSyBlocks.SEPARATOR_ROTOR.getState(BlockSeparatorRotor.BlockSeparatorRotorType.STEEL).withProperty(FACING, EnumFacing.EAST),
                SuSyBlocks.SEPARATOR_ROTOR.getState(BlockSeparatorRotor.BlockSeparatorRotorType.STEEL).withProperty(FACING, EnumFacing.WEST)
        };
    }
     */

    //makes sure block at position is properly oriented rotor
    protected TraceabilityPredicate rotorOrientation() {
        //makes sure rotor's front faces the left side (relative to the player) of controller front
        EnumFacing leftFacing = RelativeDirection.RIGHT.getRelativeFacing(getFrontFacing(), getUpwardsFacing(), isFlipped());

        // converting the left facing to positive x or z axis direction
        // this is needed for the following update which converts this rotatable block from horizontal directional into axial directional.
        EnumFacing axialFacing = leftFacing.getIndex() < 4 ? EnumFacing.SOUTH : EnumFacing.WEST;

        Supplier<BlockInfo[]> supplier = () -> new BlockInfo[]{new BlockInfo(steelRotorState().withProperty(FACING, axialFacing))};
        return new TraceabilityPredicate(blockWorldState -> {
            IBlockState state = blockWorldState.getBlockState();
            if (!(state.getBlock() instanceof BlockSeparatorRotor)) return false;

            // auto-correct rotor orientation
            if (state != steelRotorState().withProperty(FACING, axialFacing))
                getWorld().setBlockState(blockWorldState.getPos(), steelRotorState().withProperty(FACING, axialFacing));

            return true;
        }, supplier);
    }

    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }

    protected IBlockState steelRotorState() {
        return SuSyBlocks.SEPARATOR_ROTOR.getState(BlockSeparatorRotor.BlockSeparatorRotorType.STEEL);
    }

    protected static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    @Nonnull
    protected ICubeRenderer getFrontOverlay() {
        return Textures.BLAST_FURNACE_OVERLAY;
    }

    private static final float PARTICLE_SPEED = .05F;

    @Override
    public void update() {
        super.update();
        if (this.isActive() && getWorld().isRemote && this.particleColors != null) {
            createParticles();
        }
    }

    @SideOnly(Side.CLIENT)
    private void createParticles() {
        Random rand = getWorld().rand;
        if (cachedPattern == null || cachedPattern.length == 0) generateCachedPattern(getPattern(), getPatternOffset(), this.frontFacing, isFlipped());
        for (Vec3i offset : cachedPattern) {
            BlockPos pos = this.getPos().add(offset);

            //Lots of particles, not sure how performant this is
            Minecraft.getMinecraft().effectRenderer.addEffect(new SusyParticleDust(getWorld(),
                    pos.getX() + rand.nextDouble(),
                    pos.getY() + .5F / 16,
                    pos.getZ() + rand.nextDouble(),
                    PARTICLE_SPEED * this.getFrontFacing().getXOffset(), 0,
                    PARTICLE_SPEED * this.getFrontFacing().getZOffset(), 1,
                    3F, particleColors[Math.abs(rand.nextInt() % particleColors.length)]));
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == UPDATE_MATERIAL_COLOR) {
            this.particleColors = buf.readVarIntArray();
        }
        super.receiveCustomData(dataId, buf);
    }

}
