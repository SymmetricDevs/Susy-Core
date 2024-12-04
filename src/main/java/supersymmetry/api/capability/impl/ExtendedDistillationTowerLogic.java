package supersymmetry.api.capability.impl;

import gregtech.api.capability.IDistillationTower;
import gregtech.api.capability.impl.DistillationTowerLogicHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.util.GTLog;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import java.util.List;
import java.util.stream.Collectors;

public class ExtendedDistillationTowerLogic extends DistillationTowerLogicHandler {
    private int yOffset;
    public ExtendedDistillationTowerLogic(IDistillationTower tower, int yOffset) {
        super(tower);
        this.yOffset = yOffset;
    }


    public void determineOrderedFluidOutputs() {
        List<MetaTileEntityMultiblockPart> fluidExportParts = this.tower.getMultiblockParts().stream().filter((iMultiblockPart) -> {
            if (iMultiblockPart instanceof IMultiblockAbilityPart) {
                IMultiblockAbilityPart<?> abilityPart = (IMultiblockAbilityPart)iMultiblockPart;
                if (abilityPart.getAbility() == MultiblockAbility.EXPORT_FLUIDS && abilityPart instanceof MetaTileEntityMultiblockPart) {
                    return true;
                }
            }

            return false;
        }).map((iMultiblockPart) -> (MetaTileEntityMultiblockPart)iMultiblockPart).collect(Collectors.toList());
        List<IFluidHandler> orderedHandlerList = new ObjectArrayList();
        List<IFluidTank> tankList = new ObjectArrayList();
        int firstY = this.tower.getPos().getY() + yOffset;
        int exportIndex = 0;

        for(int y = firstY; y < firstY + this.getLayerCount(); ++y) {
            if (fluidExportParts.size() <= exportIndex) {
                orderedHandlerList.add(FakeTank.INSTANCE);
                tankList.add(FakeTank.INSTANCE);
            } else {
                MetaTileEntityMultiblockPart part = fluidExportParts.get(exportIndex);
                if (part.getPos().getY() == y) {
                    List<IFluidTank> hatchTanks = new ObjectArrayList();
                    ((IMultiblockAbilityPart)part).registerAbilities(hatchTanks);
                    orderedHandlerList.add(new FluidTankList(false, hatchTanks));
                    tankList.addAll(hatchTanks);
                    ++exportIndex;
                } else if (part.getPos().getY() > y) {
                    orderedHandlerList.add(FakeTank.INSTANCE);
                    tankList.add(FakeTank.INSTANCE);
                } else {
                    GTLog.logger.error("The Distillation Tower at {} had a fluid export hatch with an unexpected Y position.", this.tower.getPos());
                    this.tower.invalidateStructure();
                    this.setOrderedFluidOutputs(new ObjectArrayList());
                    this.setFluidTanks(new FluidTankList(false));
                }
            }
        }

        this.setOrderedFluidOutputs(orderedHandlerList);
        this.setFluidTanks(new FluidTankList(this.tower.allowSameFluidFillForOutputs(), tankList));
    }

    protected static class FakeTank implements IFluidHandler, IFluidTank {
        protected static final FakeTank INSTANCE = new FakeTank();
        public static final FluidTankInfo FAKE_TANK_INFO = new FluidTankInfo((FluidStack)null, Integer.MAX_VALUE);
        public static final IFluidTankProperties FAKE_TANK_PROPERTIES = new FluidTankProperties((FluidStack)null, Integer.MAX_VALUE, true, false);
        public static final IFluidTankProperties[] FAKE_TANK_PROPERTIES_ARRAY;

        protected FakeTank() {
        }

        public IFluidTankProperties[] getTankProperties() {
            return FAKE_TANK_PROPERTIES_ARRAY;
        }

        public FluidStack getFluid() {
            return null;
        }

        public int getFluidAmount() {
            return 0;
        }

        public int getCapacity() {
            return Integer.MAX_VALUE;
        }

        public FluidTankInfo getInfo() {
            return FAKE_TANK_INFO;
        }

        public int fill(FluidStack resource, boolean doFill) {
            return resource.amount;
        }

        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return null;
        }

        public FluidStack drain(int maxDrain, boolean doDrain) {
            return null;
        }

        static {
            FAKE_TANK_PROPERTIES_ARRAY = new IFluidTankProperties[]{FAKE_TANK_PROPERTIES};
        }
    }
}
