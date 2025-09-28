package supersymmetry.common.tile;

import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
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
    private ItemStack coverType;
    private IBakedModel sourceModel;

    public static boolean RENDER_SWITCH = true; // false -> regular render; true -> tile rendering

    public TileEntityCoverable() {
        super();
        coverSpots = 0;
        coverType = ItemStack.EMPTY;
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
            coverType = ItemStack.EMPTY;
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

    public ItemStack getCoverType() {
        return coverType;
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
                ret = coverType.copy();
                ret.setCount(1);
                setCovered(enumFacing, false);
            } else if (inp.isItemEqual(coverType)) {
                ret = coverType.copy();
                if (inp.getCount() == 64) {
                    ItemStack dropped = coverType.copy();
                    dropped.setCount(1);
                    player.dropItem(dropped, false, true);
                } else {
                    ret.setCount(inp.getCount() + 1);
                }
                setCovered(enumFacing, false);
            } else {
                ItemStack dropped = coverType.copy();
                dropped.setCount(getSides().length);
                player.dropItem(dropped, false, true);
                coverSpots = 0;
                coverType = inp.copy();
                coverType.setCount(1);
                setCovered(enumFacing, true);
                ret.setCount(inp.getCount() - 1);
            }
        } else {
            if (inp.isEmpty()) {
                ret = ItemStack.EMPTY;
            } else if (inp.isItemEqual(coverType) || coverType.isEmpty()) {
                ret.setCount(inp.getCount() - 1);
                setCovered(enumFacing, true);
                if (coverType.isEmpty()) {
                    coverType = inp.copy();
                    coverType.setCount(1);
                }
            } else {
                if (!coverType.isEmpty()) {
                    ItemStack dropped = coverType.copy();
                    dropped.setCount(getSides().length);
                    player.dropItem(dropped, false, true);
                    coverSpots = 0;
                }
                coverType = inp.copy();
                coverType.setCount(1);
                setCovered(enumFacing, true);
                ret.setCount(inp.getCount() - 1);
            }
        }
        if (!world.isRemote) {
            writeCustomData(GregtechDataCodes.UPDATE_COVERS, buff -> {
                buff.writeByte(coverSpots);
                buff.writeItemStack(coverType);
            });
            markAsDirty();
        } else {
            scheduleRenderUpdate();
        }

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
            Textures.FROST_PROOF_CASING.renderSided(face, renderState, translation, pipeline);
        }
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound compound) {
        super.readFromNBT(compound);
        coverType = new ItemStack(compound.getCompoundTag("cover_type"));
        coverSpots = compound.getByte("spots");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("cover_type", coverType.writeToNBT(new NBTTagCompound()));
        compound.setByte("spots", coverSpots);
        return compound;
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        packetBuffer.writeByte(coverSpots);
        packetBuffer.writeItemStack(coverType);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        coverSpots = packetBuffer.readByte();
        try {
            coverType = packetBuffer.readItemStack();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void receiveCustomData(int discriminator, @NotNull PacketBuffer pb) {
        if (discriminator == GregtechDataCodes.UPDATE_COVERS) {
            coverSpots = pb.readByte();
            try {
                coverType = pb.readItemStack();
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
}
