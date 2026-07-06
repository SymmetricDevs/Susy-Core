package supersymmetry.common.metatileentities.storage;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import codechicken.lib.colour.ColourRGBA;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.storage.MetaTileEntityCrate;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.mixins.gregtech.MetaTileEntityCrateAccessor;

public class MetaTileEntityDroneDepositBasket extends MetaTileEntityCrate {

    private static final String TAG_ROOT = "susy";
    private static final String TAG_X = "xcoord";
    private static final String TAG_Y = "ycoord";
    private static final String TAG_Z = "zcoord";

    public MetaTileEntityDroneDepositBasket(ResourceLocation metaTileEntityId, Material material, int inventorySize) {
        super(metaTileEntityId, material, inventorySize);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        var self = (MetaTileEntityCrateAccessor) this;
        return new MetaTileEntityDroneDepositBasket(metaTileEntityId, self.getMaterial(), self.getInventorySize());
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
        SusyTextures.DRONE_BASKET_OVERLAY.renderOrientedState(renderState, translation, pipeline, Cuboid6.full,
                EnumFacing.UP, false, false);
    }

    @Override
    public boolean onRightClick(EntityPlayer player, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        if (player.isSneaking()) {
            ItemStack stack = player.getHeldItem(hand);
            if (stack.isItemEqual(SuSyMetaItems.LOCATION_CARD.getStackForm())) {
                if (getWorld() != null && !getWorld().isRemote) {
                    NBTTagCompound tag = stack.getOrCreateSubCompound(TAG_ROOT);
                    tag.setInteger(TAG_X, this.getPos().getX());
                    tag.setInteger(TAG_Y, this.getPos().getY());
                    tag.setInteger(TAG_Z, this.getPos().getZ());
                    player.sendStatusMessage(
                            new TextComponentTranslation("chat.susy.location_card.encode"), false);
                }
                return true;

            }
        } else {
            return true;
        }
        // Fall back to the super method to handle other interactions
        return super.onRightClick(player, hand, facing, hitResult);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return 1;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.universal.tooltip.item_storage_capacity",
                ((MetaTileEntityCrateAccessor) this).getInventorySize()));
    }
}
