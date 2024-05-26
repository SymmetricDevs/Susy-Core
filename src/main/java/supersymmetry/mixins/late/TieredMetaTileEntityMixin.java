package supersymmetry.mixins.late;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.client.utils.RenderUtil;
import gregtech.common.ConfigHolder;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import supersymmetry.mixins.Interfaces.IWaterProof;


@Mixin(value = TieredMetaTileEntity.class, priority = 500, remap = false)
public abstract class TieredMetaTileEntityMixin extends MetaTileEntity implements IWaterProof {

    @Unique private static final int UPDATE_WATERPROOFNESS = 114514; // TODO: remove this magic number

    @Unique private boolean supersymmetry$isWaterProof = false;

    @Shadow
    public abstract int getTier();

    public TieredMetaTileEntityMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public void checkWeatherOrTerrainExplosion(float explosionPower, double additionalFireChance,
                                               IEnergyContainer energyContainer) {
        World world = getWorld();
        if (!world.isRemote && ConfigHolder.machines.doTerrainExplosion && !getIsWeatherOrTerrainResistant() &&
                energyContainer.getEnergyStored() != 0) {
            if (GTValues.RNG.nextInt(1000) == 0) {
                for (EnumFacing side : EnumFacing.VALUES) {
                    Block block = getWorld().getBlockState(getPos().offset(side)).getBlock();
                    if (block == Blocks.FIRE || block == Blocks.LAVA || block == Blocks.FLOWING_LAVA ||
                            (!supersymmetry$isWaterProof && (block == Blocks.WATER || block == Blocks.FLOWING_WATER))) {
                        doExplosion(explosionPower);
                        return;
                    }
                }
            }
            if (!supersymmetry$isWaterProof && GTValues.RNG.nextInt(1000) == 0) {
                if (world.isRainingAt(getPos()) || world.isRainingAt(getPos().east()) ||
                        world.isRainingAt(getPos().west()) || world.isRainingAt(getPos().north()) ||
                        world.isRainingAt(getPos().south())) {
                    if (world.isThundering() && GTValues.RNG.nextInt(3) == 0) {
                        doExplosion(explosionPower);
                    } else if (GTValues.RNG.nextInt(10) == 0) {
                        doExplosion(explosionPower);
                    } else setOnFire(additionalFireChance);
                }
            }
        }
    }

    @Override
    public boolean supersymmetry$isWaterProof() {
        return supersymmetry$isWaterProof;
    }

    @Override
    public void supersymmetry$setWaterProof(boolean waterProof) {
        supersymmetry$isWaterProof = waterProof;
        if (getWorld() != null && !getWorld().isRemote) {
            notifyBlockUpdate();
            markDirty();
            writeCustomData(UPDATE_WATERPROOFNESS, buf -> buf.writeBoolean(supersymmetry$isWaterProof));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("WaterProof", supersymmetry$isWaterProof);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        supersymmetry$isWaterProof = data.getBoolean("WaterProof");
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_WATERPROOFNESS) {
            supersymmetry$isWaterProof = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(supersymmetry$isWaterProof);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        supersymmetry$isWaterProof = buf.readBoolean();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getPaintingColorForRendering() {
        if (supersymmetry$isWaterProof) {
            return RenderUtil.interpolateColor(super.getPaintingColorForRendering(), 0x00AFDF, 0.1f);
        }
        return super.getPaintingColorForRendering();
    }
}
