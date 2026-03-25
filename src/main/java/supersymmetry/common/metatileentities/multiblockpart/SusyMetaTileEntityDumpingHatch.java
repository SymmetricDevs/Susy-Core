package supersymmetry.common.metatileentities.multiblockpart;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;

public class SusyMetaTileEntityDumpingHatch extends MetaTileEntityMultiblockPart {

    private boolean frontFaceFree;

    public SusyMetaTileEntityDumpingHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 1);
        this.frontFaceFree = false;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new SusyMetaTileEntityDumpingHatch(metaTileEntityId);
    }

    @Override
    public void update() {
        super.update();

        if (!getWorld().isRemote) {
            if (getOffsetTimer() % 10 == 0)
                this.frontFaceFree = checkFrontFaceFree();
        }

        MultiblockWithDisplayBase controller = (MultiblockWithDisplayBase) getController();
        if (getWorld().isRemote && controller != null && controller.isActive())
            dumpingParticles();
    }

    /**
     * @return true if front face is free and contains only air blocks in 1x1 area
     */
    public boolean isFrontFaceFree() {
        return frontFaceFree;
    }

    private boolean checkFrontFaceFree() {
        BlockPos frontPos = getPos().offset(getFrontFacing());
        IBlockState blockState = getWorld().getBlockState(frontPos);

        return blockState.getBlock().isAir(blockState, getWorld(), frontPos);
    }

    @SideOnly(Side.CLIENT)
    public void dumpingParticles() {
        BlockPos pos = this.getPos();
        EnumFacing facing = this.getFrontFacing();
        float xPos = facing.getXOffset() * 0.76F + pos.getX() + 0.25F;
        float yPos = facing.getYOffset() * 0.76F + pos.getY() + 0.25F;
        float zPos = facing.getZOffset() * 0.76F + pos.getZ() + 0.25F;

        float ySpd = -0.3F - 0.05F * GTValues.RNG.nextFloat();
        float xSpd = facing.getXOffset() * 0.3F + 0.05F * GTValues.RNG.nextFloat();
        float zSpd = facing.getZOffset() * 0.3F + 0.05F * GTValues.RNG.nextFloat();

        if (getController() instanceof MultiblockWithDisplayBase)
            getWorld().spawnParticle(EnumParticleTypes.WATER_DROP, xPos, yPos, zPos, xSpd, ySpd, zSpd);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay())
            Textures.MUFFLER_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }
}
