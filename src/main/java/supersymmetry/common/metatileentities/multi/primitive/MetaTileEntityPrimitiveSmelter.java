package supersymmetry.common.metatileentities.multi.primitive;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import com.codetaylor.mc.pyrotech.modules.core.ModuleCore;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapPrimitiveMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.GTUtility;
import gregtech.client.particle.VanillaParticleEffects;
import gregtech.client.renderer.CubeRendererState;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.cclop.ColourOperation;
import gregtech.client.renderer.cclop.LightMapOperation;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import supersymmetry.api.metatileentity.multiblock.SuSyMultiblockAbilities;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;

public class MetaTileEntityPrimitiveSmelter extends RecipeMapPrimitiveMultiblockController {

    private static final TraceabilityPredicate SNOW_PREDICATE = new TraceabilityPredicate(
            bws -> GTUtility.isBlockSnow(bws.getBlockState()));

    public MetaTileEntityPrimitiveSmelter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.PRIMITIVE_SMELTER);
    }

    @Override
    protected void initializeAbilities() {
        this.importItems = new ItemHandlerList(getAbilities(SuSyMultiblockAbilities.PRIMITIVE_IMPORT_ITEMS));
        this.exportItems = new ItemHandlerList(getAbilities(SuSyMultiblockAbilities.PRIMITIVE_EXPORT_ITEMS));
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.initializeAbilities();
    }

    public static IBlockState getCasingState() {
        return ModuleCore.Blocks.MASONRY_BRICK.getDefaultState();
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("BBB", "BBB", "SBS")
                .aisle("BBB", "B#B", "B B")
                .aisle("BBB", "BCB", "SBS")
                .where('B', states(getCasingState()).setMinGlobalLimited(14)
                        .or(abilities(SuSyMultiblockAbilities.PRIMITIVE_IMPORT_ITEMS).setPreviewCount(1))
                        .or(abilities(SuSyMultiblockAbilities.PRIMITIVE_EXPORT_ITEMS).setPreviewCount(1)))
                .where('C', selfPredicate())
                .where('S', states(ModuleCore.Blocks.MASONRY_BRICK_SLAB.getDefaultState()))
                .where('#', air().or(SNOW_PREDICATE))
                .where(' ', air())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return SusyTextures.MASONRY_BRICK;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityPrimitiveSmelter(this.metaTileEntityId);
    }

    @SideOnly(Side.CLIENT)
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return SusyTextures.PRIMITIVE_SMELTER_OVERLAY;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                recipeMapWorkable.isActive(), recipeMapWorkable.isWorkingEnabled());
        if (recipeMapWorkable.isActive() && isStructureFormed()) {
            EnumFacing back = getFrontFacing().getOpposite();
            Matrix4 offset = translation.copy().translate(back.getXOffset(), 0.2, back.getZOffset());
            CubeRendererState op = Textures.RENDER_STATE.get();
            Textures.RENDER_STATE.set(new CubeRendererState(op.layer, CubeRendererState.PASS_MASK, op.world));
            SusyTextures.SLAG_HOT.renderSided(EnumFacing.UP, renderState, offset,
                    ArrayUtils.addAll(pipeline, new LightMapOperation(240, 240), new ColourOperation(0xFFFFFFFF)));
            Textures.RENDER_STATE.set(op);
        }
    }

    @Override
    public void update() {
        super.update();

        if (this.isActive() && !getWorld().isRemote) {
            damageEntitiesAndBreakSnow();
        }
    }

    private void damageEntitiesAndBreakSnow() {
        BlockPos middlePos = this.getPos();
        middlePos = middlePos.offset(getFrontFacing().getOpposite());
        this.getWorld().getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(middlePos))
                .forEach(entity -> entity.attackEntityFrom(DamageSource.LAVA, 3.0f));

        if (getOffsetTimer() % 10 == 0) {
            IBlockState state = getWorld().getBlockState(middlePos);
            GTUtility.tryBreakSnow(getWorld(), middlePos, state, true);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick() {
        if (this.isActive()) {
            VanillaParticleEffects.defaultFrontEffect(this, 0.3F, EnumParticleTypes.SMOKE_LARGE,
                    EnumParticleTypes.FLAME);

            BlockPos pos = getPos();

            EnumFacing facing = getFrontFacing().getOpposite();
            float x = (float) facing.getXOffset() + (float) pos.getX() + 0.5F;
            float y = (float) facing.getYOffset() + (float) pos.getY() + 1.2F +
                    (float) (GTValues.RNG.nextDouble() * 2.0 / 16.0);;
            float z = (float) facing.getZOffset() + (float) pos.getZ() + 0.5F;

            for (int i = 0; i < 4; i++) {
                double offsetX = (GTValues.RNG.nextDouble() * 2.0 - 1.0) * 0.3;
                double offsetY = (GTValues.RNG.nextDouble() * 2.0 - 1.0) * 0.3;
                double offsetZ = (GTValues.RNG.nextDouble() * 2.0 - 1.0) * 0.3;
                getWorld().spawnParticle(EnumParticleTypes.FLAME, x + offsetX, y + offsetY, z + offsetZ, 0.0, 0.0, 0.0);

                if (GTValues.RNG.nextDouble() < 0.05) {
                    getWorld().spawnParticle(EnumParticleTypes.LAVA, x + offsetX, y + offsetY, z + offsetZ, 0.0, 0.0,
                            0.0);
                }
            }

            if (ConfigHolder.machines.machineSounds && GTValues.RNG.nextDouble() < 0.1) {
                getWorld().playSound(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F,
                        SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            }
        }
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        // Don't do anything since exportItems/importItems are proxies
        // This is called when the machine block is broken
        // The other way of fixing this could be writing a custom recipe workable,
        // which redirects getInputInventory() and getOutputInventory()
        // just like what MultiblockRecipeLogic did.
    }

    @Override
    public String getHarvestTool() {
        return "pickaxe";
    }
}
