package supersymmetry.common.metatileentities.single.railinterfaces;

import cam72cam.immersiverailroading.entity.EntityRollingStock;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import supersymmetry.api.stockinteraction.IStockInteractor;
import supersymmetry.api.stockinteraction.StockHelperFunctions;

import java.util.List;


public class MetaTileEntityLocomotiveController  extends MetaTileEntity implements IStockInteractor
{
    public int ticksAlive;

    public boolean active;
    public static final int filterIndex = 1;

    //control settings
    public float activeBreak;
    public float activeThrottle;
    public float inactiveBreak;
    public float inactiveThrottle;

    public final Vec3d detectionArea = new Vec3d(5, 0, 5);

    public MetaTileEntityLocomotiveController(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }
    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityStockReader(this.metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    //#fix# should have comparitor interaction maybe
    public int getActualComparatorValue() {
        return 1;
    }

    public boolean isOpaqueCube() {
        return true;
    }

    //#fix# pickaxe not it maybe
    public String getHarvestTool() {
        return "wrench";
    }

    public boolean hasFrontFacing() {
        return true;
    }

    @Override
    public Vec3d getInteractionArea() {
        return detectionArea;
    }

    public void writeStatsToBuffer(PacketBuffer buf) {

        buf.writeFloat(this.activeBreak);
        buf.writeFloat(this.activeThrottle);
        buf.writeFloat(this.inactiveBreak);
        buf.writeFloat(this.inactiveThrottle);
    }

    public void readStatsFromBuffer(PacketBuffer buf) {
        this.activeBreak = buf.readFloat();
        this.activeThrottle = buf.readFloat();
        this.inactiveBreak = buf.readFloat();
        this.inactiveThrottle = buf.readFloat();
    }


    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.active);
        this.writeStatsToBuffer(buf);
    }

    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.active = buf.readBoolean();
        this.readStatsFromBuffer(buf);

        this.scheduleRenderUpdate();
    }

    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if((dataId & 0b1) > 0) {
            this.active = buf.readBoolean();
        }
        if((dataId & 0b10) > 0) {
            this.readStatsFromBuffer(buf);
        }
    }

    public void update() {
        super.update();

        if(this.getWorld().isRemote)
            return;

        if(this.ticksAlive % 20 == 0)
        {
            List<EntityRollingStock> stocks = StockHelperFunctions.GetStockInArea(getFilterIndex(), this.getFrontFacing(), this, this.getWorld());

            if(stocks.size() > 0) {
                this.signal = calculateOutputSignal(stocks.get(0));
            }

            this.writeCustomData(0b10, (buf) -> buf.writeByte(this.signal));
        }

        this.ticksAlive++;
    }

    @Override
    public void cycleFilter(boolean up) {

    }

    @Override
    public void cycleFilterUp() {

    }

    @Override
    public byte getFilterIndex() {
        return filterIndex;
    }

    @Override
    public Class getFilter() {
        return StockHelperFunctions.ClassMap[filterIndex];
    }

    @Override
    public MetaTileEntity GetMetaTileEntity() {
        return this;
    }
}
