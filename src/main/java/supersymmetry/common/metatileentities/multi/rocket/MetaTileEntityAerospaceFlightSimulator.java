package supersymmetry.common.metatileentities.multi.rocket;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.common.ConfigHolder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.api.util.DataStorageLoader;
import supersymmetry.common.item.SuSyMetaItems;

import java.util.ArrayList;

public class MetaTileEntityAerospaceFlightSimulator extends MultiblockWithDisplayBase implements IControllable, IWorkable {

    private IEnergyContainer energyContainer;
    private boolean isActive = false;
    private boolean isWorkingEnabled = true;
    protected boolean hasNotEnoughEnergy;
    private int progress = 0;

    public DataStorageLoader rocketBlueprintSlot = new DataStorageLoader(this, item -> SuSyMetaItems.isMetaItem(item) == SuSyMetaItems.DATA_CARD_MASTER_BLUEPRINT.metaValue && item.getTagCompound() != null && !item.getTagCompound().getBoolean("buildstat"));

    public MetaTileEntityAerospaceFlightSimulator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.energyContainer = new EnergyContainerList(new ArrayList<>());
    }

    protected int getEnergyToConsume() {
        return 1000;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
        this.progress = 0;
    }

    @Override
    public int getProgress() {
        return this.progress;
    }

    @Override
    public int getMaxProgress() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isActive() {
        return super.isActive() && this.isActive;
    }

    public void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            markDirty();
            World world = getWorld();
            if (world != null && !world.isRemote) {
                writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(active));
            }
        }
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.isWorkingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        this.isWorkingEnabled = isWorkingAllowed;
        markDirty();
        World world = getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(isWorkingEnabled));
        }
    }

    @Override
    protected void updateFormedValid() {
        int energyToConsume = this.getEnergyToConsume();
        boolean hasMaintenance = ConfigHolder.machines.enableMaintenance && hasMaintenanceMechanics();
        if (hasMaintenance) {
            energyToConsume += getNumMaintenanceProblems() * energyToConsume / 10;
        }

        if (this.hasNotEnoughEnergy && energyContainer.getInputPerSec() > 19L * energyToConsume) {
            this.hasNotEnoughEnergy = false;
        }

        if (this.energyContainer.getEnergyStored() >= energyToConsume) {
            if (!hasNotEnoughEnergy) {
                long consumed = this.energyContainer.removeEnergy(energyToConsume);
                if (consumed == -energyToConsume) {
                    setActive(true);
                } else {
                    this.hasNotEnoughEnergy = true;
                    setActive(false);
                }
            }
        } else {
            this.hasNotEnoughEnergy = true;
            setActive(false);
        }

    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.energyContainer = new EnergyContainerList(new ArrayList<>());
        this.progress = 0;
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CCCCC", "CCCCC", "AAAAA", "AAAAA", "AAAAA", "CCCCC")
                .aisle("CCCCC", "CCCCC", "PPPPP", "PPPPP", "PPPPP", "CCCCC")
                .aisle("CCCCC", "CCCCC", "PPPPP", "PPPPP", "PPPPP", "CCCCC")
                .aisle("CCCCC", "CCCCC", "PPPPP", "PPPPP", "PPPPP", "CCCCC")
                .aisle("CCSMC", "CCCCC", "AAAAA", "AAAAA", "AAAAA", "CCCCC")
                .where('M', maintenancePredicate())
                .where('S', selfPredicate())
                .where('A', air())
                .where('C', states(getCasingState()))
                .where('P', states(getComputerState()))
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    public IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(
                MetalCasingType.STEEL_SOLID); // replace with real values later pls
    }

    public IBlockState getComputerState() {
        return MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityAerospaceFlightSimulator(metaTileEntityId);
    }
}
