package supersymmetry.common.metatileentities.multiblockpart;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.PhantomSlotWidget;
import gregtech.api.gui.widgets.SimpleTextWidget;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.client.particle.VanillaParticleEffects;
import gregtech.client.renderer.texture.Textures;
import supersymmetry.client.renderer.handler.BlockSkinRenderer;

public class MetaTileEntityActiveMuffler extends MetaTileEntity {

    public static final int UPDATE_STORED_BLOCK = GregtechDataCodes.assignId();

    private final GTItemStackHandler blockSlot;

    @Nullable private Block lastSyncedBlock;
    private int lastSyncedMeta;

    public MetaTileEntityActiveMuffler(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.blockSlot = new GTItemStackHandler(this, 1) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return !stack.isEmpty() && stack.getItem() instanceof ItemBlock;
            }
        };
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityActiveMuffler(metaTileEntityId);
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return true;
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return blockSlot;
    }

    @Override
    protected ModularUI createUI(EntityPlayer player) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 166)
                .label(6, 6, getMetaFullName())
                .widget(new PhantomSlotWidget(blockSlot, 0, 80, 40)
                        .setClearSlotOnRightClick(true)
                        .setBackgroundTexture(GuiTextures.SLOT))
                .widget(new SimpleTextWidget(89, 72, "", () -> "Place block here"));
        builder.bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 84);
        return builder.build(getHolder(), player);
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            syncBlockSlot();
        }
        if (getWorld().isRemote) {
            VanillaParticleEffects.mufflerEffect(this, EnumParticleTypes.SMOKE_LARGE);
        }
    }

    private void syncBlockSlot() {
        ItemStack stack = blockSlot.getStackInSlot(0);
        Block newBlock;
        int newMeta;
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemBlock)) {
            newBlock = null;
            newMeta = 0;
        } else {
            newBlock = Block.getBlockFromItem(stack.getItem());
            newMeta = stack.getMetadata();
        }
        if (newBlock != lastSyncedBlock || newMeta != lastSyncedMeta) {
            lastSyncedBlock = newBlock;
            lastSyncedMeta = newMeta;
            writeCustomData(UPDATE_STORED_BLOCK, buf -> {
                buf.writeBoolean(newBlock != null);
                if (newBlock != null) {
                    buf.writeResourceLocation(Block.REGISTRY.getNameForObject(newBlock));
                    buf.writeVarInt(newMeta);
                }
            });
        }
    }

    @Nullable
    private Block getStoredBlock() {
        if (getWorld() != null && !getWorld().isRemote) {
            ItemStack stack = blockSlot.getStackInSlot(0);
            if (stack.isEmpty() || !(stack.getItem() instanceof ItemBlock)) return null;
            return Block.getBlockFromItem(stack.getItem());
        }
        return lastSyncedBlock;
    }

    private int getStoredMeta() {
        if (getWorld() != null && !getWorld().isRemote) {
            ItemStack stack = blockSlot.getStackInSlot(0);
            if (stack.isEmpty() || !(stack.getItem() instanceof ItemBlock)) return 0;
            return stack.getMetadata();
        }
        return lastSyncedMeta;
    }

    @Override
    public boolean canRenderInLayer(BlockRenderLayer layer) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        Block block = getStoredBlock();
        if (block != null) {
            try {
                IBlockState state = block.getStateFromMeta(getStoredMeta());
                IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
                return Pair.of(model.getParticleTexture(), getPaintingColorForRendering());
            } catch (Exception ignored) {}
        }
        return Pair.of(Textures.MUFFLER_OVERLAY.getParticleSprite(), getPaintingColorForRendering());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Block block = getStoredBlock();
        if (block == null) {
            Textures.VOLTAGE_CASINGS[0].render(renderState, translation, pipeline);
        } else {
            try {
                IBlockState state = BlockSkinRenderer.resolveBlockState(block, getStoredMeta());
                BlockSkinRenderer.renderBlockSkin(renderState, translation, pipeline, state, getWorld(), getPos());
            } catch (Exception e) {
                Textures.VOLTAGE_CASINGS[0].render(renderState, translation, pipeline);
            }
        }
        Textures.MUFFLER_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        Block block = getStoredBlock();
        buf.writeBoolean(block != null);
        if (block != null) {
            buf.writeResourceLocation(Block.REGISTRY.getNameForObject(block));
            buf.writeVarInt(getStoredMeta());
        }
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        boolean hasBlock = buf.readBoolean();
        if (hasBlock) {
            this.lastSyncedBlock = Block.getBlockFromName(buf.readResourceLocation().toString());
            this.lastSyncedMeta = buf.readVarInt();
        } else {
            this.lastSyncedBlock = null;
            this.lastSyncedMeta = 0;
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_STORED_BLOCK) {
            boolean hasBlock = buf.readBoolean();
            if (hasBlock) {
                this.lastSyncedBlock = Block.getBlockFromName(buf.readResourceLocation().toString());
                this.lastSyncedMeta = buf.readVarInt();
            } else {
                this.lastSyncedBlock = null;
                this.lastSyncedMeta = 0;
            }
            scheduleRenderUpdate();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("susy.machine.muffler_active.tooltip"));
    }
}
