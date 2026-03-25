package supersymmetry.common.metatileentities.multi.electric;

import static supersymmetry.api.metatileentity.multiblock.SuSyPredicates.hiddenGearTooth;
import static supersymmetry.api.metatileentity.multiblock.SuSyPredicates.hiddenStates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.*;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import supersymmetry.api.capability.SuSyDataCodes;
import supersymmetry.api.metatileentity.IAnimatableMTE;
import supersymmetry.api.unification.ore.SusyOrePrefix;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockGrinderCasing;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.item.behavior.MillBallDurabilityManager;

public class MetaTileEntityBallMill extends RecipeMapMultiblockController implements IAnimatableMTE {

    private static final int PARALLEL_LIMIT = 32;
    public static final int MILL_BALL_REQUIREMENT = 8;

    @SideOnly(Side.CLIENT)
    private BlockPos lightPos;
    @SideOnly(Side.CLIENT)
    private Vec3i transformation;
    @SideOnly(Side.CLIENT)
    private AxisAlignedBB renderBounding;

    @SideOnly(Side.CLIENT)
    private AnimationFactory factory;

    @Nullable
    private Collection<BlockPos> hiddenBlocks;

    public MetaTileEntityBallMill(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap) {
        super(metaTileEntityId, recipeMap);
        this.recipeMapWorkable = new BallMillLogic(this);
    }

    private static IBlockState getGearBoxState() {
        return MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX);
    }

    private static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    private static IBlockState getShellCasingState() {
        return SuSyBlocks.GRINDER_CASING.getState(BlockGrinderCasing.Type.WEAR_RESISTANT_LINED_MILL_SHELL);
    }

    private static IBlockState getShellHeadState() {
        return SuSyBlocks.GRINDER_CASING.getState(BlockGrinderCasing.Type.WEAR_RESISTANT_LINED_SHELL_HEAD);
    }

    private static IBlockState getDiaphragmState() {
        return SuSyBlocks.GRINDER_CASING.getState(BlockGrinderCasing.Type.INTERMEDIATE_DIAPHRAGM);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityBallMill(metaTileEntityId, recipeMap);
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return SusyTextures.BALL_MILL_OVERLAY;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ICubeRenderer getBaseTexture(IMultiblockPart part) {
        if (part instanceof IMultiblockAbilityPart<?>abilityPart) {
            var ability = abilityPart.getAbility();
            if (ability != MultiblockAbility.MAINTENANCE_HATCH && ability != MultiblockAbility.INPUT_ENERGY) {
                return SusyTextures.BALL_MILL_SHELL;
            }
        }
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        var shell = states(getShellCasingState());

        return FactoryBlockPattern.start()
                .aisle(" XMMMXXXXXXXX", "  NMM        ", "             ", "  G          ", "  G          ",
                        "  G          ", "             ", "             ")
                .aisle(" X          X", "             ", "  G          ", "  HCCCCCCCCH ", "  HCCCCCCCCH ",
                        "  HCCCCCCCCH ", "  G          ", "             ")
                .aisle(" X          X", " XG         X", " XHCCCCCCCCHX", " XH#####D##HX", " XH#####D##HX",
                        " XH#####D##H ", "  HCCCCCCCCH ", "  G          ")
                .aisle(" X          X", "  G          ", "  HCCCCCCCCH ", "OXH#####D##HY", "AA######D###Y",
                        "ZXH#####D##HI", "  HCCCCCCCCH ", "  G          ")
                .aisle(" X          X", " XG         X", " XHCCCCCCCCHX", " XH#####D##HX", " XH#####D##HX",
                        " XH#####D##H ", "  HCCCCCCCCH ", "  G          ")
                .aisle(" X          X", "             ", "  G          ", "  HCCCCCCCCH ", "  HCCCCCCCCH ",
                        "  HCCCCCCCCH ", "  G          ", "             ")
                .aisle(" XMMMXXXXXXXX", "  NSM        ", "             ", "  G          ", "  G          ",
                        "  G          ", "             ", "             ")
                .where('M', states(getCasingState()).or(autoAbilities(
                        true, true, false,
                        false, false, false, false)))
                .where('Y', abilities(MultiblockAbility.IMPORT_ITEMS).or(shell))
                .where('Z', abilities(MultiblockAbility.EXPORT_FLUIDS).or(shell))
                .where('I', abilities(MultiblockAbility.IMPORT_FLUIDS).or(shell))
                .where('O', abilities(MultiblockAbility.EXPORT_ITEMS).or(shell))
                .where('A', states(getShellCasingState()))
                .where('C', hiddenStates(getShellCasingState()))
                .where('H', hiddenStates(getShellHeadState()))
                .where('D', hiddenStates(getDiaphragmState()))
                .where('G', hiddenGearTooth(
                        // Since isFlipped() isn't reliable at this stage, and we just care about the Axis here
                        // anyway...
                        RelativeDirection.LEFT.getRelativeFacing(getFrontFacing(), getUpwardsFacing(), false)
                                .getAxis()))
                .where('N', states(getGearBoxState()))
                .where('X', frames(Materials.Steel))
                .where('S', selfPredicate())
                .where('#', air())
                .where(' ', any())
                .build();
    }

    @Override
    @Nullable
    public Collection<BlockPos> getHiddenBlocks() {
        return hiddenBlocks;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.hiddenBlocks = context.getOrDefault("Hidden", new ArrayList<>());
        World world = getWorld();

        // This will only be called on a server side world
        // so actually no need to check !world.isRemote
        if (world != null && !world.isRemote) {
            disableBlockRendering(true);
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        World world = getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(SuSyDataCodes.RESET_RENDER_FIELDS, buf -> {
                /* Do nothing */
            });
            disableBlockRendering(false);
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        World world = getWorld();
        if (world != null && !world.isRemote) {
            disableBlockRendering(isStructureFormed()); // This is a bit ugly tho...
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == SuSyDataCodes.RESET_RENDER_FIELDS) {
            this.lightPos = null;
            this.renderBounding = null;
            this.transformation = null;
        } else {
            super.receiveCustomData(dataId, buf);
        }
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    @Override
    protected void addErrorText(List<ITextComponent> textList) {
        super.addErrorText(textList);
        if (!((BallMillLogic) this.getRecipeLogic()).hasMillBalls) {
            textList.add(new TextComponentTranslation("susy.multiblock.ball_mill.error.missing_mill_balls"));
        }
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if (!((BallMillLogic) this.getRecipeLogic()).hasMillBalls) {
            textList.add(new TextComponentTranslation("susy.multiblock.ball_mill.error.missing_mill_balls"));
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        // Parallel
        tooltip.add(I18n.format("gregtech.universal.tooltip.parallel", PARALLEL_LIMIT));
        tooltip.add(I18n.format("susy.multiblock.ball_mill.tooltip.mill_balls", MILL_BALL_REQUIREMENT));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (this.renderBounding == null) {
            EnumFacing front = getFrontFacing();
            EnumFacing upwards = getUpwardsFacing();
            boolean flipped = isFlipped();
            // The left side of the controller, not from the player's perspective
            EnumFacing left = RelativeDirection.LEFT.getRelativeFacing(front, upwards, flipped);
            EnumFacing up = RelativeDirection.UP.getRelativeFacing(front, upwards, flipped);

            BlockPos pos = getPos();

            var v1 = pos.offset(left.getOpposite(), 4).offset(up.getOpposite());
            var v2 = pos.offset(left, 9).offset(up, 8).offset(front.getOpposite(), 6);
            this.renderBounding = new AxisAlignedBB(v1, v2);
        }
        return renderBounding;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Vec3i getTransformation() {
        if (this.transformation == null) {
            EnumFacing front = getFrontFacing();
            EnumFacing upwards = getUpwardsFacing();
            boolean flipped = isFlipped();
            EnumFacing back = front.getOpposite();
            EnumFacing left = RelativeDirection.LEFT.getRelativeFacing(front, upwards, flipped);
            EnumFacing up = RelativeDirection.UP.getRelativeFacing(front, upwards, flipped);

            int xOff = back.getXOffset() * 3 + left.getXOffset() * 4 + up.getXOffset() * 3;
            int yOff = back.getYOffset() * 3 + left.getYOffset() * 4 + up.getYOffset() * 3;
            int zOff = back.getZOffset() * 3 + left.getZOffset() * 4 + up.getZOffset() * 3;

            this.transformation = new Vec3i(xOff, yOff, zOff);
        }
        return transformation;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockPos getLightPos() {
        if (this.lightPos == null) {
            EnumFacing front = getFrontFacing();
            EnumFacing upwards = getUpwardsFacing();
            boolean flipped = isFlipped();
            EnumFacing back = front.getOpposite();
            EnumFacing left = RelativeDirection.LEFT.getRelativeFacing(front, upwards, flipped);
            EnumFacing up = RelativeDirection.UP.getRelativeFacing(front, upwards, flipped);

            this.lightPos = getPos().offset(up, 6).offset(back, 3).offset(left, 3); // TODO
        }
        return lightPos;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AnimationFactory getFactory() {
        if (this.factory == null) {
            this.factory = new AnimationFactory(this);
        }
        return this.factory;
    }

    @SideOnly(Side.CLIENT)
    private <T extends MetaTileEntity & IAnimatableMTE> PlayState predicate(AnimationEvent<T> event) {
        event.getController().setAnimation(new AnimationBuilder()
                .addAnimation("default_loop", ILoopType.EDefaultLoopTypes.LOOP));
        return isActive() ? PlayState.CONTINUE : PlayState.STOP;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 0.0F, this::predicate));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        if (isStructureFormed()) {
            IAnimatableMTE.super.renderMetaTileEntity(x, y, z, partialTicks);
        }
    }

    private static class BallMillLogic extends MultiblockRecipeLogic {

        private static final int EU_PER_DURABILITY = 256;
        private int[] slotCache = new int[MILL_BALL_REQUIREMENT];
        private boolean hasMillBalls = true;

        public BallMillLogic(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
            this.setParallelLimit(PARALLEL_LIMIT);
        }

        @Override
        protected void trySearchNewRecipe() {
            hasMillBalls = true;
            super.trySearchNewRecipe();
        }

        @Override
        public boolean checkRecipe(@NotNull Recipe recipe) {
            int slotCacheIndex = 0;
            // Check if we have enough mill balls in the input inventory
            for (int i = 0; i < getInputInventory().getSlots(); i++) {
                ItemStack stack = getInputInventory().getStackInSlot(i);
                if (!stack.isEmpty() && OreDictUnifier.getPrefix(stack) == SusyOrePrefix.millBall) {
                    if (MillBallDurabilityManager.getMillBallDamage(stack) ==
                            MillBallDurabilityManager.getMillBallMaxDurability(stack)) {
                        continue;
                    }
                    this.slotCache[slotCacheIndex] = i;
                    slotCacheIndex++;
                    if (slotCacheIndex >= MILL_BALL_REQUIREMENT) {
                        break;
                    }
                }
            }

            // If not enough mill balls were found, the recipe cannot run
            if (slotCacheIndex < MILL_BALL_REQUIREMENT) {
                hasMillBalls = false;
                this.invalidInputsForRecipes = true;
                return false;
            }

            return super.checkRecipe(recipe);
        }

        @Override
        protected boolean setupAndConsumeRecipeInputs(@NotNull Recipe recipe,
                                                      @NotNull IItemHandlerModifiable importInventory,
                                                      @NotNull IMultipleTankHandler importFluids) {
            if (!hasMillBalls) {
                return false;
            }
            int itemToDamage = (int) (Math.random() * MILL_BALL_REQUIREMENT);
            ItemStack stack = importInventory.getStackInSlot(slotCache[itemToDamage]);
            if (!stack.isEmpty() && OreDictUnifier.getPrefix(stack) == SusyOrePrefix.millBall) {
                // Calculate damage based on EUt * duration
                long totalEnergy = (long) recipe.getEUt() * recipe.getDuration();
                int damage = (int) (totalEnergy / EU_PER_DURABILITY);

                // Apply damage to the mill ball using NBT-based method
                // Output it if needed
                if (damage > 0 && MillBallDurabilityManager.applyMillBallDamage(stack, damage)) {
                    recipe.getOutputs().add(stack);
                }
            }

            return super.setupAndConsumeRecipeInputs(recipe, importInventory, importFluids);
        }
    }
}
