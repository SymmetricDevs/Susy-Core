package supersymmetry.common.tileentities;

import java.io.IOException;
import java.util.ArrayList;

import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TickableTileEntityBase;
import gregtech.client.renderer.texture.Textures;

public class TileEntityCoverable extends TickableTileEntityBase {

    private byte coverSpots; // 0 - 63, bits going in the order of EnumFacing
    private ItemStack coverItem;
    private IBakedModel sourceModel;

    public static boolean RENDER_SWITCH = true; // false -> regular render; true -> tile rendering

    public TileEntityCoverable() {
        super();
        coverSpots = 0;
        coverItem = ItemStack.EMPTY;
        if (getWorld() != null && !getWorld().isRemote)
            setSourceModel();
    }

    public boolean isCovered(EnumFacing enumFacing) {
        return (coverSpots >> enumFacing.ordinal()) % 2 == 1;
    }

    private void setCovered(EnumFacing enumFacing, boolean cov) {
        coverSpots = cov ? (byte) (coverSpots | (1 << enumFacing.ordinal())) :
                (byte) (coverSpots & ~(1 << enumFacing.ordinal()));
        if (coverSpots == 0) {
            coverItem = ItemStack.EMPTY;
        }
        this.markDirty();
    }

    public EnumFacing[] getSides() {
        ArrayList<EnumFacing> ret = new ArrayList<>(6);
        for (EnumFacing val : EnumFacing.VALUES) {
            if (isCovered(val)) {
                ret.add(val);
            }
        }
        return ret.toArray(new EnumFacing[0]);
    }

    public ItemStack getCoverItem() {
        return coverItem;
    }

    public int getCoverCount() {
        int ret = 0;
        for (EnumFacing side : EnumFacing.VALUES) {
            if (isCovered(side)) {
                ret++;
            }
        }
        return ret;
    }

    public ItemStack placeCover(EnumFacing enumFacing, ItemStack inp, EntityPlayer player) {
        ItemStack ret = inp.copy();
        if (isCovered(enumFacing)) {
            if (inp.isEmpty()) {
                ret = coverItem.copy();
                ret.setCount(1);
                setCovered(enumFacing, false);
            } else if (inp.isItemEqual(coverItem)) {
                ret = coverItem.copy();
                if (inp.getCount() == 64) {
                    ItemStack dropped = coverItem.copy();
                    dropped.setCount(1);
                    player.dropItem(dropped, false, true);
                } else {
                    ret.setCount(inp.getCount() + 1);
                }
                setCovered(enumFacing, false);
            } else {
                ItemStack dropped = coverItem.copy();
                dropped.setCount(getSides().length);
                player.dropItem(dropped, false, true);
                coverSpots = 0;
                coverItem = inp.copy();
                coverItem.setCount(1);
                setCovered(enumFacing, true);
                ret.setCount(inp.getCount() - 1);
            }
        } else {
            if (inp.isEmpty()) {
                ret = ItemStack.EMPTY;
            } else if (inp.isItemEqual(coverItem) || coverItem.isEmpty()) {
                ret.setCount(inp.getCount() - 1);
                setCovered(enumFacing, true);
                if (coverItem.isEmpty()) {
                    coverItem = inp.copy();
                    coverItem.setCount(1);
                }
            } else {
                if (!coverItem.isEmpty()) {
                    ItemStack dropped = coverItem.copy();
                    dropped.setCount(getSides().length);
                    player.dropItem(dropped, false, true);
                    coverSpots = 0;
                }
                coverItem = inp.copy();
                coverItem.setCount(1);
                setCovered(enumFacing, true);
                ret.setCount(inp.getCount() - 1);
            }
        }
        if (!world.isRemote) {
            writeCustomData(GregtechDataCodes.UPDATE_COVERS, buff -> {
                buff.writeByte(coverSpots);
                buff.writeItemStack(coverItem);
            });
            markAsDirty();
        } else {
            scheduleRenderUpdate();
        }
        if (player.isCreative()) return inp;
        return ret;
    }

    @SideOnly(Side.CLIENT)
    public void render(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        if (world == null) {
            return;
        }
        setSourceModel();
        EnumFacing[] faces = getSides();
        for (EnumFacing face : faces) {
            coverTexture().renderSided(face, renderState, translation, pipeline);
        }
    }

    public SimpleOverlayRenderer coverTexture() {
        return Textures.FROST_PROOF_CASING;
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound compound) {
        super.readFromNBT(compound);
        coverItem = new ItemStack(compound.getCompoundTag("cover_type"));
        coverSpots = compound.getByte("spots");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("cover_type", coverItem.writeToNBT(new NBTTagCompound()));
        compound.setByte("spots", coverSpots);
        return compound;
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        packetBuffer.writeByte(coverSpots);
        packetBuffer.writeItemStack(coverItem);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        coverSpots = packetBuffer.readByte();
        try {
            coverItem = packetBuffer.readItemStack();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void receiveCustomData(int discriminator, @NotNull PacketBuffer pb) {
        if (discriminator == GregtechDataCodes.UPDATE_COVERS) {
            coverSpots = pb.readByte();
            try {
                coverItem = pb.readItemStack();
                scheduleRenderUpdate();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public World world() {
        return getWorld();
    }

    @Override
    public BlockPos pos() {
        return pos;
    }

    @Override
    public void notifyBlockUpdate() {}

    @Override
    public void markAsDirty() {
        if (getWorld() != null && getPos() != null) {
            getWorld().markChunkDirty(getPos(), this);
        }
    }

    public void addCollisionBoundingBox(ArrayList<IndexedCuboid6> boundingBox) {
        boundingBox.add(MetaTileEntity.FULL_CUBE_COLLISION);
    }

    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        setSourceModel();
        if (sourceModel == null) {
            return null;
        }
        return Pair.of(sourceModel.getParticleTexture(), 0xFFFFFF);
    }

    @SideOnly(Side.CLIENT)
    public void setSourceModel() {
        this.sourceModel = Minecraft.getMinecraft().getBlockRendererDispatcher()
                .getModelForState(getWorld().getBlockState(pos));
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return !oldState.getBlock().equals(newState.getBlock());
    }
}
