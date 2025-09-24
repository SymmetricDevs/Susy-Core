package supersymmetry.common.metatileentities.storage;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import codechicken.lib.colour.ColourRGBA;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityUIFactory;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.storage.MetaTileEntityCrate;
import supersymmetry.api.capability.impl.InaccessibleHandlerDelegate;
import supersymmetry.api.sound.SusySounds;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.mixins.gregtech.MetaTileEntityCrateAccessor;

public class MetaTileEntityLockedCrate extends MetaTileEntityCrate {

    public MetaTileEntityLockedCrate(ResourceLocation metaTileEntityId, Material material, int inventorySize) {
        super(metaTileEntityId, material, inventorySize);
        ((MetaTileEntityCrateAccessor) this).setTaped(true);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        var self = (MetaTileEntityCrateAccessor) this;
        return new MetaTileEntityLockedCrate(metaTileEntityId, self.getMaterial(), self.getInventorySize());
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        // Overriding the inventory to prevent I/O
        this.itemInventory = new InaccessibleHandlerDelegate(inventory);
    }

    @Override
    public boolean keepsInventory() {
        return true;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        // Copied from super class to ensure typed overlay won't be rendered
        var self = (MetaTileEntityCrateAccessor) this;
        if (self.getMaterial().toString().contains("wood")) {
            Textures.WOODEN_CRATE.render(renderState, translation,
                    GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()), pipeline);
        } else {
            int baseColor = ColourRGBA.multiply(
                    GTUtility.convertRGBtoOpaqueRGBA_CL(self.getMaterial().getMaterialRGB()),
                    GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()));
            Textures.METAL_CRATE.render(renderState, translation, baseColor, pipeline);
        }
        // Always render the overlay texture on the TOP face only
        SusyTextures.CODE_BREACHER_OVERLAY.renderOrientedState(renderState, translation, pipeline, Cuboid6.full,
                EnumFacing.UP, false, false);
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false; // To avoid opening the GUI on the right-click in the super method
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            ItemStack heldStack = playerIn.getHeldItem(hand);
            if (heldStack.isItemEqual(SuSyMetaItems.CODE_BREACHER.getStackForm())) {
                if (getWorld() != null && !getWorld().isRemote) {
                    MetaTileEntityUIFactory.INSTANCE.openUI(getHolder(), (EntityPlayerMP) playerIn);
                }
                return true;
            } else {
                if (getWorld() != null && !getWorld().isRemote) {
                    // Send status message
                    playerIn.sendStatusMessage(new TextComponentTranslation("chat.susy.crate.requires_code_breacher"),
                            true);

                    // Play failure sound effect
                    BlockPos pos = getPos();
                    getWorld().playSound(
                            null,
                            pos,
                            SusySounds.LOCKED_CRATE,
                            SoundCategory.BLOCKS,
                            0.5F,
                            1.0F);
                }
            }
        }
        // Fall back to the super method to handle other interactions
        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public boolean canPlaceCoverOnSide(@NotNull EnumFacing side) {
        return false;
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return 1;
    }

    @Override
    public void initFromItemStackData(NBTTagCompound data) {
        super.initFromItemStackData(data);
        // Overriding Typed field to ensure it is always true
        ((MetaTileEntityCrateAccessor) this).setTaped(true);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.universal.tooltip.item_storage_capacity",
                ((MetaTileEntityCrateAccessor) this).getInventorySize()));
        // Skipping the Taped information (It's a lie)
    }
}
