package supersymmetry.api.metatileentity;

import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTUtility;
import gregtech.client.particle.VanillaParticleEffects;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.ConfigHolder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import supersymmetry.api.capability.impl.UnpoweredRecipeLogic;

import java.util.List;

public abstract class UnpoweredMetaTileEntity extends MetaTileEntity {

    protected final ICubeRenderer casingRenderer;
    protected final ICubeRenderer overlayRenderer;
    protected final UnpoweredRecipeLogic workableHandler;

    public UnpoweredMetaTileEntity(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap,
            ICubeRenderer casingRenderer, ICubeRenderer overlayRenderer) {
        super(metaTileEntityId);
        this.casingRenderer = casingRenderer;
        this.overlayRenderer = overlayRenderer;
        this.workableHandler = new UnpoweredRecipeLogic(this, recipeMap);
    }

    @Override
    public boolean isActive() {
        return workableHandler.isActive() && workableHandler.isWorkingEnabled();
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(casingRenderer.getParticleSprite(), getPaintingColorForRendering());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] colouredPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        casingRenderer.render(renderState, translation, colouredPipeline);
        overlayRenderer.renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                workableHandler.isActive(), workableHandler.isWorkingEnabled());
    }

    public ModularUI.Builder createUITemplate(EntityPlayer player) {
        return ModularUI.builder(GuiTextures.BACKGROUND_STEAM.get(true), 176, 166)
                .label(6, 6, getMetaFullName())
                .shouldColor(false)
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT_STEAM.get(true), 0);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick() {
        if (this.isActive()) {

            VanillaParticleEffects.defaultFrontEffect(this, EnumParticleTypes.SMOKE_LARGE, EnumParticleTypes.FLAME);
            if (ConfigHolder.machines.machineSounds && GTValues.RNG.nextDouble() < 0.1) {

                BlockPos pos = getPos();
                getWorld().playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE,
                        SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            }
        }
    }

    @Override
    public boolean needsSneakToRotate() {
        return true;
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        tooltip.add(I18n.format("gregtech.tool_action.soft_mallet.reset"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }
}
