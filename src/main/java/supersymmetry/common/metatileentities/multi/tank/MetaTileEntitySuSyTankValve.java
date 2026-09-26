package supersymmetry.common.metatileentities.multi.tank;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;

import gregtech.api.capability.impl.FluidHandlerProxy;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;

import java.util.List;

public class MetaTileEntitySuSyTankValve extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IFluidHandler> {

    public final SuSyTankType type;

    public MetaTileEntitySuSyTankValve(ResourceLocation metaTileEntityId, SuSyTankType type) {
        super(metaTileEntityId, 0);
        this.type = type;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySuSyTankValve(metaTileEntityId, type);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.PIPE_IN_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    public ICubeRenderer getBaseTexture() {
        if (getController() != null) {
            return super.getBaseTexture();
        }
        return type.baseTexture;
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && getOffsetTimer() % 5 == 0 && isAttachedToMultiBlock() &&
                getFrontFacing() == EnumFacing.DOWN) {
            TileEntity te = getNeighbor(EnumFacing.DOWN);
            if (te != null) {
                IFluidHandler sink = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.UP);
                if (sink != null) {
                    GTTransferUtils.transferFluids(fluidInventory, sink);
                }
            }
        }
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        initializeDummyInventory();
    }

    private void initializeDummyInventory() {
        this.fluidInventory = new FluidHandlerProxy(
                new FluidTankList(false, new IFluidTank[]{}),
                new FluidTankList(false, new IFluidTank[]{}));
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controller) {
        super.addToMultiBlock(controller);
        this.fluidInventory = controller.getFluidInventory();
    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controller) {
        super.removeFromMultiBlock(controller);
        initializeDummyInventory();
    }

    @Override
    protected ModularUI createUI(EntityPlayer player) {
        return null;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public boolean canPartShare() {
        return false;
    }

    @Override
    public MultiblockAbility<IFluidHandler> getAbility() {
        return MultiblockAbility.TANK_VALVE;
    }

    @Override
    public void registerAbilities(List<IFluidHandler> abilityList) {
        abilityList.add(this.getImportFluids());
    }

    @Override
    protected boolean shouldSerializeInventories() {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.tank_valve.tooltip"));
    }

    @Override
    public boolean needsSneakToRotate() {
        return true;
    }

    @Override
    public void addToolUsages(ItemStack stack, World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }
}
