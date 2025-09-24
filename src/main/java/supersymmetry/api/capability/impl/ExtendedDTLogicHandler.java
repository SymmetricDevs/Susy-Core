package supersymmetry.api.capability.impl;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.NotNull;

import gregtech.api.capability.IDistillationTower;
import gregtech.api.capability.impl.DistillationTowerLogicHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.util.GTLog;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ExtendedDTLogicHandler extends DistillationTowerLogicHandler {

    // This have to be an anonymous class. Just blame ceu.
    protected static final FakeTank BLACK_HOLE = new FakeTank() {};

    // Y offset from the controller to the first layer that output hatch can be placed on
    // As a function since monsters like LP Cryo DT exist
    protected final Function<Integer, Integer> offsetCounter;
    // The index of the extendable aisle repetition
    protected final int outputLayerIndex;

    public ExtendedDTLogicHandler(IDistillationTower tower, int outputLayerIndex,
                                  Function<Integer, Integer> offsetCounter) {
        super(tower);
        this.outputLayerIndex = outputLayerIndex;
        this.offsetCounter = offsetCounter;
    }

    @Override
    public void determineLayerCount(@NotNull BlockPattern structurePattern) {
        this.setLayerCount(structurePattern.formedRepetitionCount[outputLayerIndex]);
    }

    @Override
    public void determineOrderedFluidOutputs() {
        // noinspection SimplifyStreamApiCallChains
        List<MetaTileEntityMultiblockPart> fluidExportParts = tower.getMultiblockParts().stream()
                .filter(iMultiblockPart -> iMultiblockPart instanceof IMultiblockAbilityPart<?>abilityPart &&
                        abilityPart.getAbility() == MultiblockAbility.EXPORT_FLUIDS &&
                        abilityPart instanceof MetaTileEntityMultiblockPart)
                .map(iMultiblockPart -> (MetaTileEntityMultiblockPart) iMultiblockPart)
                .collect(Collectors.toList());
        // the fluidExportParts should come sorted in smallest Y first, largest Y last.
        List<IFluidHandler> orderedHandlerList = new ObjectArrayList<>();
        List<IFluidTank> tankList = new ObjectArrayList<>();

        int firstY = tower.getPos().getY() + offsetCounter.apply(getLayerCount());
        int exportIndex = 0;
        for (int y = firstY; y < firstY + this.getLayerCount(); y++) {
            if (fluidExportParts.size() <= exportIndex) {
                orderedHandlerList.add(BLACK_HOLE);
                tankList.add(BLACK_HOLE);
                continue;
            }
            MetaTileEntityMultiblockPart part = fluidExportParts.get(exportIndex);
            if (part.getPos().getY() == y) {
                List<IFluidTank> hatchTanks = new ObjectArrayList<>();
                // noinspection unchecked
                ((IMultiblockAbilityPart<IFluidTank>) part).registerAbilities(hatchTanks);
                orderedHandlerList.add(new FluidTankList(false, hatchTanks));
                tankList.addAll(hatchTanks);
                exportIndex++;
            } else if (part.getPos().getY() > y) {
                orderedHandlerList.add(BLACK_HOLE);
                tankList.add(BLACK_HOLE);
            } else {
                GTLog.logger.error(
                        "The Distillation Tower at {} had a fluid export hatch with an unexpected Y position.",
                        tower.getPos());
                tower.invalidateStructure();
                this.setOrderedFluidOutputs(new ObjectArrayList<>());
                this.setFluidTanks(new FluidTankList(false));
            }
        }
        this.setOrderedFluidOutputs(orderedHandlerList);
        this.setFluidTanks(new FluidTankList(tower.allowSameFluidFillForOutputs(), tankList));
    }
}
