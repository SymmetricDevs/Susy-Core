package supersymmetry.api.metatileentity.logistics;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;


public abstract class MetaTileEntityDelegator extends MetaTileEntity implements IDelegator {

    protected final Predicate<Capability<?>> capFilter;
    protected final int baseColor;

    public MetaTileEntityDelegator(ResourceLocation metaTileEntityId, Predicate<Capability<?>> capFilter, int baseColor) {
        super(metaTileEntityId);
        this.capFilter = capFilter;
        this.baseColor = baseColor;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        T delegatedCapability = getDelegatedCapability(capability, side);
        return delegatedCapability == null ? getDefaultCapability(capability, side) : delegatedCapability;
    }

    protected <T> T getDefaultCapability(Capability<T> capability, EnumFacing side) {
        return side != null && capFilter.test(capability) && DefaultCapabilities.hasCapability(capability) ? DefaultCapabilities.getCapability(capability) : super.getCapability(capability, side);
    }

    protected <T> T getDelegatedCapability(Capability<T> capability, EnumFacing side) {
        if (capability == null || !capFilter.test(capability) || side == null) return null;
        EnumFacing delegatingFacing = getDelegatingFacing(side);
        if (delegatingFacing == null) return null;
        TileEntity te = getWorld().getTileEntity(getPos().offset(delegatingFacing));
        if (te == null || (te instanceof MetaTileEntityHolder holder && holder.getMetaTileEntity() instanceof IDelegator))
            return null;
        // TODO: make IDelegator a capability when Jet Wingsuit PR gets merged
        return te.getCapability(capability, delegatingFacing.getOpposite());
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] colouredPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(this.getPaintingColorForRendering())));
        for (EnumFacing facing : EnumFacing.values()) {
            Textures.renderFace(renderState, translation, colouredPipeline, facing, Cuboid6.full, this.getBaseTexture(), BlockRenderLayer.CUTOUT_MIPPED);
        }
    }

    @SideOnly(Side.CLIENT)
    protected TextureAtlasSprite getBaseTexture() {
        return Textures.PIPE_SIDE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        addRecursiveWarning(stack, world, tooltip, advanced);
    }

    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(getBaseTexture(), getPaintingColorForRendering());
    }

    @Override
    public int getDefaultPaintingColor() {
        return this.baseColor;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

}
