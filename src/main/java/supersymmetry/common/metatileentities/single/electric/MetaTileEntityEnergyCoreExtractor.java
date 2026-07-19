package supersymmetry.common.metatileentities.single.electric;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.apache.commons.lang3.ArrayUtils;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;

import appeng.api.config.Actionable;
import appeng.api.config.PowerUnits;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;

/**
 * Drains unrechargeable "condensed energy core" batteries and feeds the extracted power directly into an
 * adjacent AE2 network as AE power. Deliberately never exposes an EU capability (Forge Energy or
 * {@link GregtechCapabilities#CAPABILITY_ENERGY_CONTAINER}), so the only way to power the network through this
 * block is by feeding it cores.
 *
 * @author Claude Opus 4.8
 */
public class MetaTileEntityEnergyCoreExtractor extends MetaTileEntity {

    private final int tier;
    private AENetworkProxy aeProxy;

    public MetaTileEntityEnergyCoreExtractor(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId);
        this.tier = tier;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity holder) {
        return new MetaTileEntityEnergyCoreExtractor(metaTileEntityId, tier);
    }

    public int getTier() {
        return tier;
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        // the single slot handles both insertion and extraction, for automation on any side
        this.itemInventory = importItems;
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, 1, this, false) {

            @NotNull
            @Override
            public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                if (electricItem != null && !electricItem.chargeable() && electricItem.canProvideChargeExternally() &&
                        getTier() >= electricItem.getTier()) {
                    return super.insertItem(slot, stack, simulate);
                }
                return stack;
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        super.setFrontFacing(frontFacing);
        if (this.aeProxy != null) {
            this.aeProxy.setValidSides(EnumSet.of(frontFacing));
        }
    }

    @NotNull
    @Override
    public AECableType getCableConnectionType(@NotNull AEPartLocation part) {
        if (part.getFacing() != getFrontFacing()) {
            return AECableType.NONE;
        }
        return AECableType.COVERED;
    }

    @Nullable
    @Override
    public AENetworkProxy getProxy() {
        if (this.aeProxy == null) {
            this.aeProxy = createProxy();
        }
        if (this.aeProxy != null && !this.aeProxy.isReady() && getWorld() != null) {
            this.aeProxy.onReady();
        }
        return this.aeProxy;
    }

    @Nullable
    private AENetworkProxy createProxy() {
        if (getHolder() instanceof IGridProxyable holder) {
            AENetworkProxy proxy = new AENetworkProxy(holder, "energy_core_extractor", getStackForm(), true);
            proxy.setIdlePowerUsage(0.0);
            proxy.setValidSides(EnumSet.of(getFrontFacing()));
            return proxy;
        }
        return null;
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote) {
            return;
        }

        ItemStack stack = importItems.getStackInSlot(0);
        IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem == null || electricItem.getCharge() <= 0) {
            return;
        }

        AENetworkProxy proxy = getProxy();
        if (proxy == null) {
            return;
        }

        try {
            IEnergyGrid grid = proxy.getEnergy();

            long offeredEu = Math.min(electricItem.getCharge(), electricItem.getTransferLimit());
            if (offeredEu <= 0) {
                return;
            }

            double offeredAE = PowerUnits.GTEU.convertTo(PowerUnits.AE, offeredEu);
            double leftoverAE = grid.injectPower(offeredAE, Actionable.MODULATE);
            double usedAE = offeredAE - leftoverAE;
            if (usedAE <= 0) {
                return;
            }

            long usedEu = (long) Math.floor(PowerUnits.AE.convertTo(PowerUnits.GTEU, usedAE));
            if (usedEu > 0) {
                electricItem.discharge(usedEu, tier, false, true, false);
            }
        } catch (GridAccessException ignored) {
            // not connected to a formed AE network, nothing to feed
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] colouredPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        Textures.VOLTAGE_CASINGS[tier].render(renderState, translation, colouredPipeline);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return ModularUI.builder(GuiTextures.BACKGROUND, 176, 133)
                .label(10, 5, getMetaFullName())
                .widget(new SlotWidget(importItems, 0, 79, 30, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.BATTERY_OVERLAY))
                .bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 51)
                .build(getHolder(), entityPlayer);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("susy.machine.energy_core_extractor.tooltip.1"));
        tooltip.add(I18n.format("susy.machine.energy_core_extractor.tooltip.2"));
    }
}
