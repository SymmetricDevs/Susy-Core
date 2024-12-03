package supersymmetry.api.metatileentity.logistics;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


public abstract class MetaTileEntityDelegator extends MetaTileEntity implements IDelegator, IDataInfoProvider {

    protected final Predicate<Capability<?>> capFilter;

    public MetaTileEntityDelegator(ResourceLocation metaTileEntityId, Predicate<Capability<?>> capFilter) {
        super(metaTileEntityId);
        this.capFilter = capFilter;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        T delegatedCapability = getDelegatedCapability(capability, side);
        return delegatedCapability == null ? getDefaultCapability(capability, side) : delegatedCapability;
    }

    protected <T> T getDefaultCapability(Capability<T> capability, EnumFacing side) {
        return capFilter.test(capability) && DefaultCapabilities.hasCapability(capability) ? DefaultCapabilities.getCapability(capability) : super.getCapability(capability, side);
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
        getBaseRenderer().render(renderState, translation, colouredPipeline);
    }

    @SideOnly(Side.CLIENT)
    protected SimpleSidedCubeRenderer getBaseRenderer() {
        return Textures.VOLTAGE_CASINGS[GTValues.LV];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        // TODO
    }

    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(getBaseRenderer().getParticleSprite(), getPaintingColorForRendering());
    }

    @Override
    @NotNull
    public List<ITextComponent> getDataInfo() {
        return new ArrayList<>(); // TODO
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    public static class DefaultCapabilities {

        private static final Object2ObjectArrayMap<Capability<?>, ?> DEFAULT_CAPABILITIES = new Object2ObjectArrayMap<>();

        static {
            // Item
            addCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new ItemStackHandler(1) {

                @NotNull
                @Override
                public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                    return stack;
                }

                @NotNull
                @Override
                public ItemStack extractItem(int slot, int amount, boolean simulate) {
                    return ItemStack.EMPTY;
                }
            }));

            // Fluid
            addCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new FluidTank(10000) {

                @Override
                public int fill(FluidStack resource, boolean doFill) {
                    return 0;
                }

                @Override
                @Nullable
                public FluidStack drainInternal(int maxDrain, boolean doDrain) {
                    return null;
                }
            }));

            // GTEU
            addCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(IEnergyContainer.DEFAULT));
        }

        public static boolean hasCapability(@NotNull Capability<?> capability) {
            return DEFAULT_CAPABILITIES.containsKey(capability);
        }

        @Nullable
        @SuppressWarnings("unchecked")
        public static <T> T getCapability(@NotNull Capability<T> capability) {
            return (T) DEFAULT_CAPABILITIES.getOrDefault(capability, null);
        }

        public static <T> void addCapability(@NotNull Capability<T> capability, @NotNull T value) {
            DEFAULT_CAPABILITIES.put(capability, capability.cast(value));
        }
    }
}
