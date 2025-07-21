package supersymmetry.common.metatileentities.storage;

import codechicken.lib.vec.Cuboid6;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;

import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import codechicken.lib.colour.ColourRGBA;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityLockedCrate extends MetaTileEntity {
    private final Material material;
    private final int inventorySize;
    protected ItemStackHandler inventory;

    public static final SimpleOverlayRenderer CODE_BREACHER_OVERLAY = new SimpleOverlayRenderer(
            "susy:code_breacher_crate");

    private static final Cuboid6 FULL_CUBE = new Cuboid6(0, 0, 0, 1, 1, 1);

    public MetaTileEntityLockedCrate(ResourceLocation metaTileEntityId, Material material, int inventorySize) {
        super(metaTileEntityId);
        this.material = material;
        this.inventorySize = inventorySize;
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLockedCrate(metaTileEntityId, material, inventorySize);
    }

    @Override
    public boolean hasFrontFacing() {
        return false;
    }

    @Override
    public String getHarvestTool() {
        return ModHandler.isMaterialWood(material) ? ToolClasses.AXE : ToolClasses.WRENCH;
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.inventory = new GTItemStackHandler(this, inventorySize);
        this.itemInventory = inventory;
    }

    @Override
    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {}

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        if (ModHandler.isMaterialWood(material)) {
            return Pair.of(Textures.WOODEN_CRATE.getParticleTexture(), getPaintingColorForRendering());
        } else {
            int color = ColourRGBA.multiply(
                    GTUtility.convertRGBtoOpaqueRGBA_CL(material.getMaterialRGB()),
                    GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()));
            color = GTUtility.convertOpaqueRGBA_CLtoRGB(color);
            return Pair.of(Textures.METAL_CRATE.getParticleTexture(), color);
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        if (material.toString().contains("wood")) {
            Textures.WOODEN_CRATE.render(renderState, translation,
                    GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()), pipeline);
        } else {
            int baseColor = ColourRGBA.multiply(GTUtility.convertRGBtoOpaqueRGBA_CL(material.getMaterialRGB()),
                    GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()));
            Textures.METAL_CRATE.render(renderState, translation, baseColor, pipeline);
        }

        // Always render the overlay texture on the TOP face only
        CODE_BREACHER_OVERLAY.renderOrientedState(renderState, translation, pipeline, FULL_CUBE, EnumFacing.UP, false, false);
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        int factor = inventorySize / 9 > 8 ? 18 : 9;
        ModularUI.Builder builder = ModularUI
                .builder(GuiTextures.BACKGROUND, 176 + (factor == 18 ? 176 : 0), 8 + inventorySize / factor * 18 + 104)
                .label(5, 5, getMetaFullName());
        for (int i = 0; i < inventorySize; i++) {
            builder.slot(inventory, i, 7 * (factor == 18 ? 2 : 1) + i % factor * 18, 18 + i / factor * 18,
                    GuiTextures.SLOT);
        }
        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7 + (factor == 18 ? 88 : 0),
                18 + inventorySize / factor * 18 + 11);
        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        ItemStack heldItem = playerIn.getHeldItem(hand);
        if (heldItem.isEmpty()
                || !heldItem.getItem().getRegistryName().toString().equals("gregtech:meta_item_2")
                || heldItem.getMetadata() != 1008) {
            if (!playerIn.world.isRemote) {
                playerIn.sendMessage(new TextComponentTranslation("chat.susy.crate.requires_code_breacher"));
            }
            return true;
        }
        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return 1;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("Inventory", inventory.serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.inventory.deserializeNBT(data.getCompoundTag("Inventory"));
    }

    @Override
    public void initFromItemStackData(NBTTagCompound data) {
        super.initFromItemStackData(data);
        if (data.hasKey(TAG_KEY_PAINTING_COLOR)) {
            this.setPaintingColor(data.getInteger(TAG_KEY_PAINTING_COLOR));
        }
        if (data.hasKey("Inventory")) {
            this.inventory.deserializeNBT(data.getCompoundTag("Inventory"));
        }

        data.removeTag(TAG_KEY_PAINTING_COLOR);
        data.removeTag("Inventory");
    }

    @Override
    public void writeItemStackData(NBTTagCompound data) {
        super.writeItemStackData(data);

        if (this.isPainted()) {
            data.setInteger(TAG_KEY_PAINTING_COLOR, this.getPaintingColor());
        }

        data.setTag("Inventory", inventory.serializeNBT());
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
    }

    @Override
    protected boolean shouldSerializeInventories() {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.universal.tooltip.item_storage_capacity", inventorySize));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }
}
